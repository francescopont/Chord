package chord.network;

import chord.Messages.Message;
import chord.model.NodeInfo;
import chord.model.Threads;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

public class SocketNode implements Runnable {
    //the port is an unique identifier for the node
    private int port;
    //the socket where I listen for new connections
    private ServerSocket serverSocket;
    //to delete the node
    private boolean terminate;
    // to keep memory on the connection open
    private HashMap<NodeInfo,SocketHandler> activeconnections;
    //PROBLEMA: AGGIORNARE QUESTA LISTA TOGLIENDO LE CONNESSIONI CHE SONO STATE CHIUSE
    // soluzione artigianale: se quando mando un messaggio non riesco, allora sicuramente la connessione ha un problema

    //porta effettivamente in uso
    private int actual_port;


    //constructor
    public SocketNode(int port) {
        this.terminate = false;
        this.activeconnections = new HashMap<>();
        this.port=port;
        this.actual_port=-1;
        System.out.println("Sto creando un nodo con la porta "+ port);
    }

    @Override
    public void run() {
        try {
            this.serverSocket= new ServerSocket(port);
            this.actual_port= port;
        } catch (IOException e) {
            try {
                this.serverSocket= new ServerSocket(0);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            this.actual_port= serverSocket.getLocalPort();
        }
        while (!terminate) {

            try {
                // I got a new connection!!!
                Socket clientSocket = serverSocket.accept();

                //a new handler for this connection
                SocketHandler handler = new SocketHandler(this.actual_port, clientSocket);

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
                this.activeconnections.put(nodeInfo, handler);
            } catch (IOException e) {
                System.out.println("errore nella socketnode");
                e.printStackTrace();
            }
        }
    }

    //to delete this socketnode
    public void terminate(){
        for (SocketHandler handler: this.activeconnections.values()) {
            handler.terminate();
            try{
                serverSocket.close();
            }catch (IOException e){
                e.printStackTrace();
            }
        }
        this.terminate = true;
    }

    //used by Router Class, it may be deleted in future improvements of code
    public int getPort() {
        return port;
    }

    //to send a message
    public void sendMessage(Message message){
        boolean yetsend = false;
        NodeInfo nodeInfo = message.getDestination();

        // we check if we already have an active connection open with the receiver
        if ( this.activeconnections.containsKey(nodeInfo)){
            try{
                //send the message
                this.activeconnections.get(nodeInfo).sendMessage(message);
                yetsend = true;
            }
            catch (IOException e ){
                this.activeconnections.remove(nodeInfo);
                e.printStackTrace();
            }
        }

        if (!yetsend){
            Socket socket = null;
            try {
                socket = new Socket(nodeInfo.getIPAddress(),nodeInfo.getPort());

                //REPEAT THE CODE AS ABOVE
                //a new handler for this connection
                SocketHandler handler = new SocketHandler(this.actual_port,socket);

                //add the new connection to the list of active connections
                this.activeconnections.put(nodeInfo,handler);

                //handle the new connection!!
                Threads.executeImmediately(handler);
                //finally send the message
                handler.sendMessage(message);
            }catch(IOException e){
                e.printStackTrace();

            }/*finally {
                try{
                    socket.close();
                }catch (IOException e){
                    e.printStackTrace();
                }
            }*/

                //andrebbe rilanciato??? gestito diversamente??

        }
    }

    public int getActual_port() {
        return actual_port;
    }

}

