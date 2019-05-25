package chord.model;

import chord.Exceptions.PortException;
import chord.network.Router;

import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Hello world!
 *
 */
public class App

{

    //piccolo test
    public static void main( String[] args ) {
        Scanner scanner= new Scanner(System.in);

/*
        System.out.println("Selezionare un'opzione: \n c for Create \n j for Join ");{
            String input= scanner.nextLine();
            if(input.equals("c")){
                System.out.println("Insert IP Address: ");
                String ip=scanner.nextLine();
                System.out.println("Insert port Number: ");
                int port= scanner.nextInt();
                try {
                    Chord.create(ip,port);
                } catch (PortException e) {
                    e.printStackTrace();
                }
            }

            else if(input.equals("j")){
                System.out.println("Insert your IP Address: ");
                String ip=scanner.nextLine();
                System.out.println("Insert the IP address of your friend: ");
                String friendIp=scanner.nextLine();
                System.out.println("Insert your port Number: ");
                int port=scanner.nextInt();
                System.out.println("Insert the port Number of your BFF: ");
                int friendPort= scanner.nextInt();
                try {
                    Chord.join(ip,port,friendIp,friendPort);
                } catch (PortException e) {
                    e.printStackTrace();
                }
            }

    }*/

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
        }, 10000, 60000);

        Timer timer1 = new Timer();
        timer1.schedule(new TimerTask() {
            @Override
            public void run() {
                Chord.deleteNode(1000);
                Chord.deleteNode(1004);
            }
        }, 30000);


    }


}
