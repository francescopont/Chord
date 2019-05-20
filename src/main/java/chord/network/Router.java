package chord.network;

import chord.Exceptions.PortException;
import chord.Messages.Message;
import chord.model.Chord;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class Router {
    private static List<SocketNode> nodes = new LinkedList<>();

    //don't let anyone instantiate this class
    private Router(){};

    //se la porta è già in uso lancia un'eccezione con la porta effettiva libera che è riuscito ad usare
    public static void addnode(int port) throws PortException {
        synchronized (nodes){
            SocketNode node;
            try{
                node = new SocketNode(port);
                nodes.add(node);
                new Thread(node).start();
            }catch (IOException e){
                try {
                    node = new SocketNode(0);
                    int actual_port = node.getPort();
                    nodes.add(node);
                    new Thread(node).start();
                    throw new PortException(actual_port);
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
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
        for (SocketNode node: nodes){
            if (node.getPort() == message.getDestination().getPort()){
                Chord.deliverMessage(node.getPort(),message);
                delivered = true;
            }
        }
        if (!delivered){
            System.out.println("devo inviare sulla rete");
            for (SocketNode node: nodes){
                if (node.getPort() == port){
                    node.sendMessage(message);
                }
            }
        }
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
