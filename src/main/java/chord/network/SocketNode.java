package chord.network;

import chord.Messages.Message;
import chord.model.NodeInfo;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Hashtable;

public class SocketNode implements Runnable {
    //the port is an unique identifier for the node
    private int port;
    //the socket where I listen for new connections
    ServerSocket serverSocket;
    //to delete the node
    private boolean terminate;
    // to keep memory on the connection open
    private Hashtable<NodeInfo,SocketHandler> activeconnections;

    //PROBLEMA: AGGIORNARE QUESTA LISTA TOGLIENDO LE CONNESSIONI CHE SONO STATE CHIUSE
    // soluzione artigianale: se quando mando un messaggio non riesco, allora sicuramente la connessione ha un problema


    //constructor
    public SocketNode(int port)throws IOException{
        this.terminate = false;
        this.activeconnections = new Hashtable<>();
        this.serverSocket = new ServerSocket(port);
        this.port = serverSocket.getLocalPort();
        System.out.println("port:" + this.port);
    }

    @Override
    public void run() {
        try {
            while (!terminate) {
                // I got a new connection!!!
                Socket clientSocket = serverSocket.accept();
                //all the info about the node which is connecting to me
                int port = clientSocket.getPort();
                String IP = clientSocket.getInetAddress().toString();
                NodeInfo nodeInfo = new NodeInfo(IP,port);

                //a new handler for this connection
                SocketHandler handler = new SocketHandler(this.port,clientSocket);

                //add the new connection to the list of active connections
                this.activeconnections.put(nodeInfo,handler);

                //handle the new connection!!
                new Thread(handler).start();

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //to delete this socketnode
    public void terminate(){
        for (SocketHandler handler: this.activeconnections.values()) {
            handler.terminate();
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

            }
        }

        if (!yetsend){

            try (Socket socket = new Socket(nodeInfo.getIPAddress(),nodeInfo.getPort())){

                //REPEAT THE CODE AS ABOVE
                //a new handler for this connection
                SocketHandler handler = new SocketHandler(this.port,socket);

                //add the new connection to the list of active connections
                this.activeconnections.put(nodeInfo,handler);

                //handle the new connection!!
                new Thread(handler).start();
                //finally send the message
                handler.sendMessage(message);
            }catch (IOException e){

                //andrebbe rilanciato??? gestito diversamente??
            }
        }
    }
}

