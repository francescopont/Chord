package chord.Testers;

import chord.Exceptions.PortException;
import chord.Exceptions.TimerExpiredException;
import chord.Messages.Message;
import chord.Messages.PingRequestMessage;
import chord.model.Chord;
import chord.model.Node;
import chord.model.NodeDispatcher;
import chord.model.NodeInfo;
import chord.network.Router;

public class NodeDispatcherTester {
    public static void main(String[] args){
        //in order to allow testing, MassageHandler has been modified to avoid including testing of methods of class Node

        int actual_port_1 = 10000;
        int actual_port_2 = 10001;
        try {
            Router.addnode(10000);
        } catch (PortException e) {
            actual_port_1 = e.getPort();
        }
        try{
            Router.addnode(10000);
        }catch( PortException e){
            actual_port_2 = e.getPort();
        }

        NodeInfo node_info1 = new NodeInfo("127.0.0.1", actual_port_1);
        NodeInfo node_info2 = new NodeInfo("127.0.0.1", actual_port_2);

        Node node1 = new Node(node_info1);
        Node node2 = new Node(node_info2);
        Chord.addNodeTesting(node1);
        Chord.addNodeTesting(node2);

        try {
            node1.getDispatcher().sendPing(node_info2, node_info1);
            node2.getDispatcher().sendPing(node_info1, node_info2);
        } catch (TimerExpiredException e) {
            e.printStackTrace();
            System.out.println("timer expired");
        }




    }
}
