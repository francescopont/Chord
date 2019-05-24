package chord.network;

import chord.Messages.Message;
import chord.model.NodeInfo;
import chord.model.Threads;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;

public class SocketNode implements Runnable {
    //the port is an unique identifier for the node
    private int port;
    //the socket where I listen for new connections
    private ServerSocket serverSocket;
    //to delete the node
    private boolean terminate;

    private List<SocketHandler> handlers;
    //PROBLEMA: AGGIORNARE QUESTA LISTA TOGLIENDO LE CONNESSIONI CHE SONO STATE CHIUSE
    // soluzione artigianale: se quando mando un messaggio non riesco, allora sicuramente la connessione ha un problema

    //porta effettivamente in uso



    //constructor
    public SocketNode(int port) throws IOException {
        this.terminate = false;
        this.handlers = new LinkedList<>();
        this.serverSocket= new ServerSocket(port);
        this.port=serverSocket.getLocalPort();
        System.out.println("Sto creando un nodo con la porta "+ port);
    }

    @Override
    public void run() {

        while (!terminate) {
            try {
                // I got a new connection!!!
                    Socket clientSocket = serverSocket.accept();

                    synchronized (this.handlers){
                        //a new handler for this connection
                        SocketHandler handler = new SocketHandler(this.port, clientSocket);
                        //handle the new connection!!
                        Threads.executeImmediately(handler);

                        //add the new connection to the list of active connections
                        boolean got = false;
                        NodeInfo nodeInfo = null;
                        while (!got) {
                            try {
                                nodeInfo = handler.getEndpoint();
                                got = true;
                            } catch (Exception e) {
                                //do nothing
                            }
                        }

                        //add the new connection to the existing ones
                        this.handlers.add(handler);
                    }

            } catch (IOException e) {
                System.out.println("errore nella socketnode");
                e.printStackTrace();
            }
        }
    }

    //to delete this socketnode
    public void terminate(){
        for (SocketHandler handler : this.handlers){
            handler.terminate();
        }
        this.terminate = true;
        try{
            serverSocket.close();
        }catch (IOException e){
            e.printStackTrace();
        }

    }

    //used by Router Class, it may be deleted in future improvements of code
    public int getPort() {
        return port;
    }

    //to send a message
    public  void sendMessage(Message message){
        boolean yetsend = false;
        NodeInfo nodeInfo = message.getDestination();

        // we check if we already have an active connection open with the receiver
        synchronized (this.handlers){
            for (SocketHandler handler: this.handlers){
                try{
                    if (nodeInfo.equals(handler.getEndpoint())){
                        handler.sendMessage(message);
                        yetsend = true;
                    }
                }catch (Exception e ){
                    handler.terminate();
                    this.handlers.remove(handler);
                    e.printStackTrace();
                }
            }
            if (!yetsend){
                Socket socket = null;
                try {
                    socket = new Socket(nodeInfo.getIPAddress(),nodeInfo.getPort());

                    //REPEAT THE CODE AS ABOVE
                    //a new handler for this connection
                    SocketHandler handler = new SocketHandler(this.port,socket);

                    //add the new connection to the list of active connections
                    this.handlers.add(handler);


                    //handle the new connection!!
                    Threads.executeImmediately(handler);
                    //finally send the message
                    handler.sendMessage(message);
                }catch(IOException e){
                    e.printStackTrace();

                }


        }

                //andrebbe rilanciato??? gestito diversamente??

        }
    }

    public void printSOcketNode(){
        System.out.println("It's socketME! "+ this.port +  " and I've " + this.handlers.size() + " connections");
        for (SocketHandler socketHandler: this.handlers){
            try{
                System.out.println(socketHandler.getEndpoint().getPort());
            }catch (Exception e){
                System.out.println("porta sconosciuta");
                //e.printStackTrace();
            }
        }
        System.out.println("---------------------");
    }



}

