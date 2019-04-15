package chord.network;

import chord.Messages.Message;
import chord.model.Chord;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class SocketHandler implements Runnable{
    private int port;
    private Socket socket;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private boolean terminate = false;

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
        try {

            while(!terminate){
                //read the message from the buffer
                Message message= (Message) in.readObject();

                //print out a message for fun
                System.out.println("eccociiii");

                //deliver the message to the above layer
                //note: since we do not have actors like in Erlang, when we get something from another layer
                //it's recommended to handle it on a separate thread
                Chord.deliverMessage(this.port, message);
                //note: the socket layer does not care about the content of the message
            }

            socket.close();


        } catch (IOException | ClassNotFoundException e) {
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







}

