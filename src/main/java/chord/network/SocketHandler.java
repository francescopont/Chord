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
    private ObjectInputStream in;
    private ObjectOutputStream out;

    public SocketHandler(int port, Socket socket) throws IOException{
        this.port = port;
        this.out = new ObjectOutputStream(socket.getOutputStream());
        this.in = new ObjectInputStream(socket.getInputStream());

    }


    @Override
    public void run() {
        try {
            Message message= (Message) in.readObject();
            System.out.println("eccociiii");
            Chord.deliverMessage(this.port, message);
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public synchronized void sendMessage(Message message)throws IOException{
        out.writeObject(message);
        out.flush();
    }







}

