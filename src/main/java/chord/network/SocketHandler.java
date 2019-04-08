package chord.network;

import chord.model.Node;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class SocketHandler implements Runnable{
    private Node node;
    private Socket socket;
    private ObjectInputStream in;
    private ObjectOutputStream out;

    public SocketHandler(Node node, Socket socket){
        this.node=node;
        this.socket=socket;
    }


    @Override
    public void run() {
        try {
            in=new ObjectInputStream(socket.getInputStream());
            Message message= (Message) in.readObject();
            System.out.println("eccociiii");
            new Thread(new MessageHandler(message)).start();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

}

