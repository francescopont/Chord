package chord.network;

import chord.Messages.Message;
import chord.SocketTester.SocketTester;


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

        while(!terminate){
            try {

                //read the message from the buffer
                Message message= (Message) in.readObject();

                //deliver the message to the above layer
                //note: since we do not have actors like in Erlang, when we get something from another layer
                //it's recommended to handle it on a separate thread
                SocketTester.deliverMessage(this.port, message);
                //note: the socket layer does not care about the content of the message
            }catch (IOException | ClassNotFoundException e) {
                //do something
                //e.printStackTrace();
            }
        }

        try{
            socket.close();
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







}

