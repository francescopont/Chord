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
    ObjectInputStream in = null;
    ObjectOutputStream out = null;

    public SocketHandler(int port, Socket socket) throws IOException{
        this.port = port;
        this.socket = socket;
        this.out = new ObjectOutputStream(socket.getOutputStream());
        this.in = new ObjectInputStream(socket.getInputStream());

    }

    //PROBLEMA: COME FARLO TERMINARE SENZA SIDE EFFECTS ( CIOè AVVISANDO IL SOCKET NODE CHE è TERMINATO)
    //per esempio quando mi accorgo che l'altro endpoint della connessione è morto????
    //questa cosa potrebbe essere chatchata in un'eccezione

    @Override
    public void run() {

        while(!terminate){
            try {

                //read the message from the buffer
                Message message= (Message) in.readObject();
                System.out.println("I'm: "+ port + " and I've received a message from: "+ message.getSender().getPort());
                if (this.endpoint == null){
                    this.endpoint = message.getSender();
                    System.out.println("ho settato l'endpoint");
                }

                //deliver the message to the above layer
                //note: since we do not have actors like in Erlang, when we get something from another layer
                //it's recommended to handle it on a separate thread
                Chord.deliverMessage(this.port, message);
                //note: the socket layer does not care about the content of the message
            }catch (IOException | ClassNotFoundException e) {
                //do something
                //e.printStackTrace();
            }
        }

        System.out.println("sto cercando di terminare");
        try{
            in.close();
        }catch (IOException e){
            e.printStackTrace();
        }

    }

    public synchronized void sendMessage(Message message)throws IOException{
        out.writeObject(message);
        out.flush();
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
}

