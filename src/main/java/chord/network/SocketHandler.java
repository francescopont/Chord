package chord.network;

import chord.Messages.Message;
import chord.model.Chord;
import chord.model.NodeInfo;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;


/**
 * Class for handle the different socket connections of each node
 */
public class SocketHandler implements Runnable{
    /**
     * port associated to the socket connection
     */
    private int port;
    /**
     * information of the node to which the socket connection is associated
     */
    private NodeInfo endpoint = null;
    /**
     * socket connection associated to a node (to a port)
     */
    private Socket socket;
    /**
     * Socket stream in input
     */
    private ObjectInputStream in = null;
    /**
     * Socket stream in output
     */
    private ObjectOutputStream out = null;
    /**
     * Boolean value that indicates if the node is terminated or not
     */
    private boolean terminate = false;
    /**
     * Boolean value that indicates if the socket connection is still in use or not
     */
    private boolean usedRecently;

    public SocketHandler(int port, Socket socket) throws IOException{
        this.port = port;
        this.socket = socket;
        this.out = new ObjectOutputStream(socket.getOutputStream());
        this.in = new ObjectInputStream(socket.getInputStream());
        this.usedRecently = true;
    }

    /**
     * Receive the message in input and forward to the above layer (Chord class)
     * The socket layer does not care about the content of the message
     */
    @Override
    public void run() {
        while(!terminate){
            try {
                //read the message from the buffer
                Message message= (Message) in.readObject();
                setUsedRecently(true);
                //set the endpoint if not done yet
                if (this.endpoint == null){
                    this.endpoint = message.getSender();
                }
                //deliver the message to the above layer
                Chord.deliverMessage(this.port, message);
                //note: the socket layer does not care about the content of the message
            }catch (IOException | ClassNotFoundException e) {
                //do nothing
            }
        }
        try{
            in.close();
            out.close();
            socket.close();
        }catch (IOException e){
            //do nothing
        }
    }

    /**
     * Receive a message from the under layer (Socket node) and write it on the output stream
     * @param message to send on the connection
     * @throws IOException Exception thrown when the input or the output are not correct
     */
    public void sendMessage(Message message)throws IOException{
        //set the endpoint ( if not already set)
        if (this.endpoint == null) {
            this.endpoint = message.getDestination();
        }
        out.writeObject(message);
        out.flush();
        setUsedRecently(true);
    }

    public void terminate(){
        this.terminate = true;
    }
    public NodeInfo getEndpoint () throws Exception {
        if (endpoint == null){
            throw new Exception();
        }
        return endpoint;
    }

    public int getPort() {
        return port;
    }

    public boolean isUsedRecently() {
        return usedRecently;
    }

    public void setUsedRecently(boolean usedRecently) {
        this.usedRecently = usedRecently;
    }
}

