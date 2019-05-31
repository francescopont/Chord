package chord.network;

import chord.Messages.Message;
import chord.model.NodeInfo;
import chord.model.Threads;
import chord.model.Utilities;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;

public class SocketNode implements Runnable {
    //the port is an unique identifier for the node
    private int port;
    //the socket where I listen for new connections
    private ServerSocket serverSocket;
    //to delete the node
    private boolean terminated;

    private ScheduledFuture terminate;

    private final List<SocketHandler> handlers;
    //PROBLEMA: AGGIORNARE QUESTA LISTA TOGLIENDO LE CONNESSIONI CHE SONO STATE CHIUSE
    // soluzione artigianale: se quando mando un messaggio non riesco, allora sicuramente la connessione ha un problema

    //porta effettivamente in uso

    //constructor
    public SocketNode(int port) throws IOException {
        this.terminated = false;
        this.handlers = new LinkedList<>();
        this.serverSocket= new ServerSocket(port);
        this.port=serverSocket.getLocalPort();
    }

    @Override
    public void run() {

        while (!terminated) {
            try {
                // I got a new connection!!!
                Socket clientSocket = serverSocket.accept();
                synchronized (this.handlers){
                    //a new handler for this connection
                    SocketHandler handler = new SocketHandler(this.port, clientSocket);
                    //handle the new connection!!
                    Threads.executeImmediately(handler);
                    //add the new connection to the existing ones
                    this.handlers.add(handler);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try{
            serverSocket.close();
        }catch (IOException e){
            //do nothing
        }
    }

    //to delete this socketnode
    public void terminate(){
        this.terminate.cancel(true);
        for (SocketHandler handler : this.handlers){
            handler.terminate();
        }
        this.terminated = true;
    }

    //to check the actual use of active connections
    public void initialize(){
        this.terminate = Threads.executeRarely(() -> {
            synchronized (handlers){
                List<SocketHandler> removedHandlers = new LinkedList<>();
                for (SocketHandler handler: handlers){
                    if (!handler.isUsedRecently()){
                        removedHandlers.add(handler);
                        handler.terminate();
                    }else{
                        handler.setUsedRecently(false);
                    }
                }
                for (SocketHandler handler: removedHandlers){
                    handlers.remove(handler);
                }
            }
        });
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
            List<SocketHandler> removedHandlers = new LinkedList<>();
            for (SocketHandler handler: this.handlers){
                try{
                    if (nodeInfo.equals(handler.getEndpoint())){
                        handler.sendMessage(message);
                        yetsend = true;
                    }
                }catch (IOException e ){
                    removedHandlers.add(handler);
                    handler.terminate();
                }
                catch(Exception e){
                    //do nothing
                }
            }

            if (!removedHandlers.isEmpty()){
                for (SocketHandler handler: removedHandlers){
                    handlers.remove(handler);
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
                    if (Utilities.isTesting() && message.getSender().getHash().equals("fc6e")){
                        System.out.println("excpetion");
                    }
                    //do nothing
                }
            }
        }
    }

    //useful for testing
    public void printSocketNode(){
        System.out.println("It's socketME! "+ this.port +  " and I've " + this.handlers.size() + " connections");
        for (SocketHandler socketHandler: this.handlers){
            try{
                System.out.println(socketHandler.getEndpoint().getPort());
            }catch (Exception e){
                System.out.println("porta sconosciuta");
            }
        }
        System.out.println("---------------------");
    }



}

