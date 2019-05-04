package chord.Testers;

import chord.Exceptions.PortException;
import chord.model.Chord;

public class NodeTester {

    public static void main(String args[]){
        int actualPort=1000;
        try {
            Chord.create("127.0.0.1",actualPort);
        } catch (PortException e) {
            actualPort=e.getPort();
        }

        try {
            Chord.join("127.0.0.1",1001,"127.0.0.1",actualPort);
        } catch (PortException e) {
            e.printStackTrace();
        }

        Chord.printNode();
    }

}
