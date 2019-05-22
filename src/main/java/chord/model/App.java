package chord.model;

import chord.Exceptions.PortException;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Hello world!
 *
 */
public class App 
{
    //piccolo test
    public static void main( String[] args )
    {

        int actual_port = 1000;
        try {
            Chord.create("127.0.0.1", 1000);
        } catch (PortException e) {
            actual_port = e.getPort();
        }

        try{
            Chord.join("127.0.0.1",10001, "127.0.0.1", actual_port );
        }catch (Exception e){
            e.printStackTrace();
        }


        try{
            Chord.join("127.0.0.1",1002, "127.0.0.1", actual_port );
        }catch (Exception e){
            e.printStackTrace();
        }
        Timer timer=new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                System.out.println("print cord");
                Chord.printChord();

            }
        }, 60000);
    }
}
