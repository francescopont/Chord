package chord.network;

import chord.model.Node;
import chord.model.NodeInfo;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class SocketNode implements Runnable {
    private chord.model.NodeInfo info;
    private Node node;

    public SocketNode(NodeInfo info, Node node){
        this.info=info;
        this.node=node;
    }

    @Override
    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(info.getPort())) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                new Thread(new SocketHandler(node,clientSocket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

