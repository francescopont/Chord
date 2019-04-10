package chord.network;

import java.util.LinkedList;
import java.util.List;

public class Router {
    private static List<SocketNode> nodes;

    public static void addnode(int port){
        synchronized (nodes){
            if (nodes == null){
                nodes = new LinkedList<>();
            }

        }
        SocketNode node = new SocketNode(port);
        nodes.add(node);
        new Thread(node).start();

        //problema: sincronizzazione ( cosa succede se ci metto in mezzo altre istruzioni che per esempio iterano sui nodi?)

    }

    //returns a ticket for that message
    public static int sendMessage(int port, Message message){
        int ticket = Ticket.getTicket();
        message.setId(ticket);

        //not implemented yet (how to send the message?)
        //the problem here is how to discover if I have already open a connection with the other node
        //in that case it's better to use that connection, instead of opening a new one
        return ticket;
    }

    public static void terminate(int port){
        for (SocketNode node: nodes){
            if (node.getPort() == port){
                node.terminate();
            }
        }
    }
}
