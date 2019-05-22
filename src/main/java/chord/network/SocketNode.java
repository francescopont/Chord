package chord.network;

import chord.Messages.Message;
import chord.model.NodeInfo;
import chord.model.Threads;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class SocketNode implements Runnable {
    //the port is an unique identifier for the node
    private int port;
    //the socket where I listen for new connections
    private ServerSocket serverSocket;
    //to delete the node
    private boolean terminate;
    // to keep memory on the connection open
    private HashMap<NodeInfo,SocketHandler> activeconnections;

    private List<Message> messages;
    //PROBLEMA: AGGIORNARE QUESTA LISTA TOGLIENDO LE CONNESSIONI CHE SONO STATE CHIUSE
    // soluzione artigianale: se quando mando un messaggio non riesco, allora sicuramente la connessione ha un problema


    //constructor
    public SocketNode(int port)throws IOException{
        this.terminate = false;
        this.activeconnections = new HashMap<>();
        this.serverSocket = new ServerSocket(port);
        this.port = serverSocket.getLocalPort();
        System.out.println("Sto creando un nodo con la porta "+ port);
        this.messages = new LinkedList<>();
    }

    @Override
    public void run() {
        while (!terminate) {
            while (!this.messages.isEmpty()) {
                System.out.println("ho appena un messaggio da mandare");
                Message message = this.messages.remove(0);
                boolean yetsend = false;
                NodeInfo nodeInfo1 = message.getDestination();
                System.out.println("I'm: " + this.port + " and I'm trying to send a message to: " + message.getDestination().getPort());
                // we check if we already have an active connection open with the receiver
                if (this.activeconnections.containsKey(nodeInfo1)) {
                    System.out.println("già ho la connessione con questo nodo");
                    try {
                        //send the message
                        this.activeconnections.get(nodeInfo1).sendMessage(message);
                        yetsend = true;
                        System.out.println("yetsend = true");
                    } catch (IOException e) {
                        this.activeconnections.remove(nodeInfo1);
                        e.printStackTrace();
                    }
                }

                if (!yetsend) {
                    Socket socket = null;
                    try {
                        socket = new Socket(nodeInfo1.getIPAddress(), nodeInfo1.getPort());


                        System.out.println("sto creando un nuovo handler perchè voglio inviare un messggio ad un nodo nuovo e ho inviato il messaggio");
                        //REPEAT THE CODE AS ABOVE
                        //a new handler for this connection
                        SocketHandler handler1 = new SocketHandler(this.port, socket);

                        //add the new connection to the list of active connections
                        this.activeconnections.put(nodeInfo1, handler1);

                        //handle the new connection!!
                        Threads.executeImmediately(handler1);
                        //finally send the message
                        handler1.sendMessage(message);
                    } catch (IOException e) {
                        e.printStackTrace();

                    } finally {
                        try {
                            socket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    //andrebbe rilanciato??? gestito diversamente??

                }
            }

            System.out.println("sto facendo partire una cosa");
            try {
                // I got a new connection!!!
                Socket clientSocket = serverSocket.accept();

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

                System.out.println("I'm: " + port + " and I'm adding a new routing element " + nodeInfo.getPort());
                this.activeconnections.put(nodeInfo, handler);
            } catch (IOException e) {
                e.printStackTrace();
            }
            synchronized (this) {
                if (this.messages.isEmpty()) {
                    System.out.println("è vuota");
                }

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
    public synchronized void sendMessage(Message message){
        this.messages.add(message);
    }
}

