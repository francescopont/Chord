package chord.network;

import chord.model.Node;
import chord.model.NodeInfo;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Hashtable;

public class SocketNode implements Runnable {
    //the port is an unique identifier for the node
    private int port;
    //to delete the node
    private boolean terminate;
    // to keep memory on the connection open
    private Hashtable<NodeInfo,SocketHandler> activeconnections;

    //PROBLEMA: AGGIORNARE QUESTA LISTA TOGLIENDO LE CONNESSIONI CHE SONO STATE CHIUSE
    // soluzione artigianale: se quando mando un messaggio non riesco, allora sicuramente la connessione ha un problema


    //constructor
    public SocketNode(int port){
        this.port = port;
        this.terminate = false;
        this.activeconnections = new Hashtable<>();
    }

    @Override
    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
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
                new Thread(new SocketHandler(port,clientSocket)).start();

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
        NodeInfo nodeInfo = message.getNodeInfo();

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
                new Thread(new SocketHandler(port,socket)).start();

                //finally send the message
                handler.sendMessage(message);
            }catch (IOException e){

                e.printStackTrace();
                //andrebbe rilanciato??? gestito diversamente??
            }
        }
    }
}

