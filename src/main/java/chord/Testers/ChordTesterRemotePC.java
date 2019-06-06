package chord.Testers;

import chord.Exceptions.NotInitializedException;
import chord.Exceptions.PortException;
import chord.model.Chord;
import chord.model.Data;

public class ChordTesterRemotePC implements Runnable {
    @Override
    public synchronized  void run() {
        try {
            wait(20000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        try {
            int port = 1003;
            long previous1 = System.currentTimeMillis();
            Chord.join("192.168.43.70", port, "192.168.43.70", 1001);
            long after1 = System.currentTimeMillis();
            System.out.println("time for join: "+ (after1-previous1));
            long previuos = System.currentTimeMillis();
            Data data = new Data("ffffffff");
            String key = Chord.publish(data, port);
            long after = System.currentTimeMillis();
            System.out.println("Time for publish: " + (after - previuos));
            long prima = System.currentTimeMillis();
            String rsult = Chord.lookup(key, port);
            long dopo = System.currentTimeMillis();
            System.out.println("Time for lookup : " + (dopo - prima));
            System.out.println(rsult);
            long before = System.currentTimeMillis();
            Chord.deleteFile(key, port);
            long later = System.currentTimeMillis();
            System.out.println("Time for delete : " + (later - before));
            rsult = Chord.lookup(key, port);
            System.out.println(rsult);
            long prec = System.currentTimeMillis();
            Chord.deleteNode(port);
            long succ = System.currentTimeMillis();
            System.out.println("Time for terminate : " + (succ - prec));
        } catch (NotInitializedException e) {
            System.out.println(e.getMessage());
        } catch (PortException e) {
            e.printStackTrace();
        }

    }


    public static void main(String[] args){
        new Thread( new ChordTesterRemotePC()).start();
    }
}
