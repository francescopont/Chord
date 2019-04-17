package chord.network;

import chord.Messages.Message;

import java.util.LinkedList;
import java.util.List;

public class Router {
    private static List<SocketNode> nodes;

    //don't let anyone instantiate this class
    private Router(){};

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

    //returns the ticket for that message ( which is basically an incremental identifier)
    //porta= porta da cui mando
    public static int sendMessage(int port, Message message){
        int ticket = Ticket.getTicket();
        message.setId(ticket);
        for (SocketNode node: nodes){
            if (node.getPort() == port){
                node.sendMessage(message);
            }
        }

        //not implemented yet (how to send the message?)
        //the problem here is how to discover if I have already open a connection with the other node
        //in that case it's better to use that connection, instead of opening a new one
        return ticket;
    }

    public static void sendAnswer(int port, Message message){
        for (SocketNode node: nodes){
            if (node.getPort() == port){
                node.sendMessage(message);
            }
        }
    }

    public static void terminate(int port){
        for (SocketNode node: nodes){
            if (node.getPort() == port){
                node.terminate();
            }
        }
    }
}
