package chord.network;

import chord.Exceptions.PortException;
import chord.Messages.Message;
import chord.model.Chord;
import chord.model.Threads;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * Class that is used for forwarding messages on the network.
 * Class of the network layer visible from the library, the methods are called by the node dispatcher when it has to send messages to other nodes
 */
public class Router {
    /**
     * list of socket nodes connected on the network (??)
     */
    private static List<SocketNode> nodes = new LinkedList<>();
    private static String IPAddress = null;

    private Router(){};

    /**
     * Create a new socket node associated to a new node (created by a create or a join), with the port passed as parameter if possible
     * Called from Chord class
     * @param port on which the socket node will accept new connections
     * @throws PortException Exception thrown if the port is already in use and return the free port that will be use by socket node
     */
    public static void addnode(int port) throws PortException {
        synchronized (nodes){
            SocketNode node;
            try{
                node = new SocketNode(port);
                nodes.add(node);
                Threads.executeImmediately(node);
                node.initialize();
            }catch (IOException e){
                try{
                    node = new SocketNode(0);
                    nodes.add(node);
                    throw new PortException(node.getPort());
                }catch (IOException e1){
                    // do nothing
                }
            }
        }

    }

    /**
     * Send the message on the network and return the ticket associated (unique number that identifies the message and its reply)
     * Called from NodeDispatcher
     * @param port of the sender of the message
     * @param message to send
     * @return ticket ( which is basically an incremental identifier) of the message
     */
    public static int sendMessage(int port, Message message) {
        int ticket = Ticket.getTicket();
        message.setId(ticket);
        boolean delivered = false;

        if (message.getDestination().getHash().equals(message.getSender().getHash())){
            delivered = true;
            Chord.deliverMessage(message.getDestination().getPort(),message);
        }
        //code optmization we do not use in early releases
        /*if (message.getDestination().getIPAddress().equals(IPAddress)){
            delivered = true;
            Chord.deliverMessage(message.getDestination().getPort(), message);
        }*/ //decommentiamo?
        if (!delivered){
            for (SocketNode node: nodes){
                if (node.getPort() == port){
                    node.sendMessage(message);
                }
            }
        }
        return ticket;
    }

    /**
     * Send an answer to a received message
     * @param port of the sender
     * @param message to be delivered
     */
    public static void sendAnswer(int port, Message message){
        boolean delivered = false;
        if (message.getDestination().getHash().equals(message.getSender().getHash())){
            delivered = true;
            Chord.deliverMessage(message.getDestination().getPort(),message);
        }
        /*if (message.getDestination().getIPAddress().equals(IPAddress)){
            delivered = true;
            Chord.deliverMessage(message.getDestination().getPort(), message);
        }*/
        if (!delivered){
            for (SocketNode node: nodes){
                if (node.getPort() == port){
                    node.sendMessage(message);

                }
            }
        }
    }

    /**
     * Delete the socket node associated to the passed port and free the port
     * @param port of the node that want to exit Chord
     */
    public static void terminate(int port){
        SocketNode removedNode=null;
        for (SocketNode node: nodes){
            if (node.getPort() == port){
                node.terminate();
                removedNode= node;
            }
        }
        nodes.remove(removedNode);
    }

    public static void setIPAddress( String newIPAddress){
        IPAddress = newIPAddress;
    }

    /**
     * Print the status of the Router (cancelliamo??)
     */
    public static void printRouter(){
        for(SocketNode socketNode: nodes){
            socketNode.printSocketNode();
        }
    }

}
