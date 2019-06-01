package chord.network;

import chord.Exceptions.PortException;
import chord.Messages.Message;
import chord.model.Chord;
import chord.model.Threads;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class Router {
    private static List<SocketNode> nodes = new LinkedList<>();
    private static String IPAddress = null;

    //don't let anyone instantiate this class
    private Router(){};

    //se la porta è già in uso lancia un'eccezione con la porta effettiva libera che è riuscito ad usare
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

    //returns the ticket for that message ( which is basically an incremental identifier)
    //porta= porta da cui mando
    public static int sendMessage(int port, Message message) {
        int ticket = Ticket.getTicket();
        message.setId(ticket);
        boolean delivered = false;

        if (message.getDestination().getHash().equals(message.getSender().getHash())){
            System.out.println("il destinatario e il mittente coincidono!" + message.getSender().getHash() + " " + message.getType());
            delivered = true;
            Chord.deliverMessage(message.getDestination().getPort(),message);
        }

        //code optmization we do not use in early releases
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
        return ticket;
    }

    //when we send an answe
    public static void sendAnswer(int port, Message message){
        boolean delivered = false;
        if (message.getDestination().getHash().equals(message.getSender().getHash())){
            System.out.println("il destinatario e il mittente coincidono!");
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

    public static void printRouter(){
        for(SocketNode socketNode: nodes){
            socketNode.printSocketNode();
        }
    }

}
