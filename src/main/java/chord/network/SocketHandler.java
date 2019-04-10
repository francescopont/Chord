package chord.network;

import chord.model.Chord;
import chord.model.Node;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class SocketHandler implements Runnable{
    private int port;
    private Socket socket;
    private ObjectInputStream in;
    private ObjectOutputStream out;

    public SocketHandler(int port, Socket socket){
        this.port = port;
        this.socket=socket;
    }


    @Override
    public void run() {
        try {
            in=new ObjectInputStream(socket.getInputStream());
            Message message= (Message) in.readObject();
            System.out.println("eccociiii");
            Chord.deliverMessage(this.port, message);
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }



}

