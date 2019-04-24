package chord.SocketTester;

import chord.Messages.Message;
import chord.Exceptions.PortAlreadyInUseException;
import chord.model.NodeInfo;
import chord.network.Router;

public class SocketTester {
    public static void main(String[] args) {
        int actual_port_1 = 10000;
        int actual_port_2 = 10001;
        try {
            Router.addnode(10000);
        } catch (PortAlreadyInUseException e) {
            actual_port_1 = e.getPort();
        }
        try{
            Router.addnode(10000);
        }catch( PortAlreadyInUseException e){
            actual_port_2 = e.getPort();
        }

        NodeInfo node1 = new NodeInfo("127.0.0.1", actual_port_1);
        NodeInfo node2 = new NodeInfo("127.0.0.1", actual_port_2);
        System.out.println("node1: "+ actual_port_1);
        System.out.println("node2: "+ actual_port_2);

        Message message = new Message(2, false, node2, node1);
        int ticket = Router.sendMessage(node1.getPort(), message);
        System.out.println("main thread, sender: " + node1.getPort() +" id of message sent: "+ ticket + " destination of the message "+ message.getDestination().getPort());
        Message message1 = new Message(3,false, node1,node2);
        int ticket1 = Router.sendMessage(node2.getPort(),message1);
        System.out.println("main thread, sender: " + node2.getPort() +" id of message sent: "+ ticket1 + " destination of the message "+ message1.getDestination().getPort());

        Message message2 = new Message(78, false, node1, node2);
        message2.setId(2);
        Router.sendAnswer(node2.getPort(), message2);
    }

    public static void deliverMessage(int port, Message message){
        System.out.println("destination: " + port + " message id[recevied]: " +message.getId());
    }

}


