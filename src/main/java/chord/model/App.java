package chord.model;

import chord.Exceptions.PortException;
import chord.network.Router;

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
            System.out.println(actual_port);
        }
        try{
            Chord.join("127.0.0.1",10001, "127.0.0.1", actual_port );
        }catch (Exception e){
            e.printStackTrace();
        }

        for (int i=1; i<8; i++){
            try{
                Chord.join("127.0.0.1",1000 + i, "127.0.0.1", actual_port );
            }catch (Exception e){
                e.printStackTrace();
            }
        }


        System.out.println("finita la computazione");
        Timer timer=new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                System.out.println("print cord");
                Chord.printChord();
                System.out.println("Pinta di birra");
                Router.printRouter();

            }
        }, 120000, 60000);
    }


}
