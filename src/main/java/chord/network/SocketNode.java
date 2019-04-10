package chord.network;

import chord.model.Node;
import chord.model.NodeInfo;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class SocketNode implements Runnable {
    private int port;
    private boolean terminate;

    public SocketNode(int port){
        this.port = port;
        this.terminate = false;
    }

    @Override
    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            while (!terminate) {
                Socket clientSocket = serverSocket.accept();
                new Thread(new SocketHandler(port,clientSocket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void terminate(){
        this.terminate = true;
    }

    public int getPort() {
        return port;
    }
}

