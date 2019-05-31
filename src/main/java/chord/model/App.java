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
    public static void main( String[] args ) {
        /*Scanner scanner= new Scanner(System.in);

        System.out.println("Selezionare un'opzione:\n c for Create \n j for Join\n p for publish\ng for get a file\n d for delete ");
        {
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
                catch (NotInitializedException e){
                    e.printStackTrace();
                }
            }
            else if(input.equals("p")){
                System.out.println("Inserisci i dati del file: ");
                String info= scanner.nextLine();
                Data data= new Data(info);
                System.out.println("Inserisci la tua porta: ");
                int port=scanner.nextInt();
                Chord.publish(data,port);
                //alternativa in cui calcolo la chiave nell'oggetto e lo passo come argomento (fa più cagare secodno me)
                //Chord.publish(data,data.getKey(),port);
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

        for (int i=1; i<5; i++){
            try{
                Chord.join("127.0.0.1",1000 + i, "127.0.0.1", actual_port );
            }catch (Exception e){
                e.printStackTrace();
            }
        }

        System.out.println("finita la computazione");
        /*Data data= new Data("sono una fugaaaaaa");
        Data data1= new Data("giorgio ti amoooo");
        Timer timer=new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                String key= Chord.publish(data,1000);
                System.out.println(key);
                String key1= Chord.publish(data1,1004);
                System.out.println(key1);
                System.out.println("print cord");
                Chord.printChord();
                System.out.println("Pinta di birra");
                Router.printRouter();
                System.out.println("Mungi la vacca");
                Chord.getFile(key,1000);
                Chord.getFile(key1,1000);
                Chord.getFile("000",1000);
                Chord.getFile(key,1001);
                Chord.getFile(key1,1002);
                Chord.getFile(key,1003);
                Chord.deleteFile(key,1000);
                Chord.getFile(key,1002);

            }
        }, 10000, 60000);

        */

        Timer timer1 = new Timer();
        timer1.schedule(new TimerTask() {
            @Override
            public void run() {
                Utilities.setTesting(true);
                Chord.deleteNode(1000);
                Chord.deleteNode(1004);
                Chord.deleteNode(1002);
                Chord.deleteNode(1003);
            }
        }, 30000);


    }


}
