package chord.network;

import chord.Messages.Message;
import chord.model.Chord;
import chord.model.NodeInfo;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class SocketHandler implements Runnable{
    private int port;
    private NodeInfo endpoint = null;
    private Socket socket;
    private boolean terminate = false;
    private boolean usedRecently;
    ObjectInputStream in = null;
    ObjectOutputStream out = null;

    public SocketHandler(int port, Socket socket) throws IOException{
        this.port = port;
        this.socket = socket;
        this.out = new ObjectOutputStream(socket.getOutputStream());
        this.in = new ObjectInputStream(socket.getInputStream());
        this.usedRecently = true;
    }

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

