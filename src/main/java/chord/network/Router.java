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
            node = new SocketNode(port);
            nodes.add(node);
            Threads.executeImmediately(node);
            while (node.getActual_port()==-1){
                //
            }
            if(node.getActual_port()!=port){
                throw new PortException(node.getActual_port());
            }
        }

    }

    //returns the ticket for that message ( which is basically an incremental identifier)
    //porta= porta da cui mando
    //forse qua è meglio controllare che la porta da cui sto mandando corrisponda alla porta del sender contenuto nel messaggio
    public static int sendMessage(int port, Message message) {
        int ticket = Ticket.getTicket();
        message.setId(ticket);
        if (message.getDestination().getHash().equals(message.getSender().getHash())){
            System.out.println("il destinatario e il mittente coincidono!");
        }
        boolean delivered = false;
        /*if (message.getDestination().getIPAddress().equals(IPAddress)){
            delivered = true;
            Chord.deliverMessage(message.getDestination().getPort(), message);
        }*/

        if (!delivered){
            System.out.println(message.getDestination().getIPAddress() + " e invece " + IPAddress);
            for (SocketNode node: nodes){
                if (node.getActual_port() == port){
                    node.sendMessage(message);
                }
            }
        }
        return ticket;
    }

    public static void sendAnswer(int port, Message message){
        boolean delivered = false;
        if (message.getDestination().getIPAddress().equals(IPAddress)){
            delivered = true;
            Chord.deliverMessage(message.getDestination().getPort(), message);
        }

        if (!delivered){
            for (SocketNode node: nodes){
                if (node.getActual_port() == port){
                    node.sendMessage(message);

                }
            }
        }
    }

    public static void terminate(int port){
        for (SocketNode node: nodes){
            if (node.getActual_port() == port){
                node.terminate();
            }
        }
    }

    public static void setIPAddress( String newIPAddress){
        IPAddress = newIPAddress;
    }
}
