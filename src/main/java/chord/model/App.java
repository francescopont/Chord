package chord.model;

import chord.Exceptions.NotInitializedException;
import chord.Exceptions.PortException;

import java.util.Scanner;

/**
 * Hello world!
 *
 */
public class App

{
    //piccolo test
    public static void main( String[] args ) {
        Boolean exit=false;
        Scanner scanner= new Scanner(System.in);

        while(!exit) {
            System.out.println("Selezionare un'opzione:\n c for Create \n j for Join\n p for publish\n g for get a file\n d for delete a file\n t for terminate a node\n ps for print the Chord status\n e for exit ");
                String input = scanner.nextLine();
                if (input.equals("c")) {
                    System.out.println("Insert IP Address: ");
                    String ip = scanner.nextLine();
                    System.out.println("Insert port Number: ");
                    int port = scanner.nextInt();
                    try {
                        long previous = System.currentTimeMillis();
                        Chord.create(ip, port);
                        long after = System.currentTimeMillis();
                        System.out.println("time: " + (after - previous));
                    } catch (PortException e) {
                        e.printStackTrace();
                    }
                } else if (input.equals("j")) {
                    System.out.println("Insert your IP Address: ");
                    String ip = scanner.nextLine();
                    System.out.println("Insert the IP address of your friend: ");
                    String friendIp = scanner.nextLine();
                    System.out.println("Insert your port Number: ");
                    int port = scanner.nextInt();
                    System.out.println("Insert the port Number of your BFF: ");
                    int friendPort = scanner.nextInt();
                    try {
                        Chord.join(ip, port, friendIp, friendPort);
                    } catch (PortException e) {
                        e.printStackTrace();
                    } catch (NotInitializedException e) {
                        e.printStackTrace();
                    }
                }
                else if (input.equals("p")) {
                    System.out.println("Insert file data : ");
                    String info = scanner.nextLine();
                    Data data = new Data(info);
                    System.out.println("Insert your port number : ");
                    int port = scanner.nextInt();
                    String key= null;
                    try {
                        key = Chord.publish(data, port);
                        System.out.println("The file key is: "+ key);
                    } catch (NotInitializedException e) {
                        System.out.println(e.getMessage());
                    }
                }
                else if(input.equals("g")){
                    System.out.println("Insert the file key :");
                    String key= scanner.nextLine();
                    if(key.length()==4){
                        System.out.println("Insert your port number : ");
                        int port=scanner.nextInt();
                        try {
                            String file = Chord.lookup(key,port);
                            if(file!=null){
                                System.out.println(file);
                            }
                            else{
                                System.out.println("This file does not exist");
                            }
                        } catch (NotInitializedException e) {
                            System.out.println(e.getMessage());
                        }
                    }
                    else{
                        System.out.println("The key must be of 4 characters");
                    }

                }
                else if(input.equals("d")){
                    System.out.println("Insert the file key :");
                    String key= scanner.nextLine();
                    System.out.println("Insert your port number :");
                    int port= scanner.nextInt();
                    try {
                        Chord.deleteFile(key,port);
                    } catch (NotInitializedException e) {
                        System.out.println(e.getMessage());
                    }
                }
                else if(input.equals("t")){
                    System.out.println("Insert your port number : ");
                    int port= scanner.nextInt();
                    try {
                        Chord.deleteNode(port);
                    } catch (NotInitializedException e) {
                        e.printStackTrace();
                    }
                }
                else if(input.equals("ps")){
                    Chord.printChord();
                }
                else if(input.equals("e")){
                    exit=true;
                }
                else{
                    System.out.println("Wrong input");
                }
        }

        System.out.println("Program terminate");

        /*int actual_port = 1000;
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
        Data data= new Data("sono una fugaaaaaa");
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
                Chord.lookup(key,1000);
                Chord.lookup(key1,1000);
                Chord.lookup("000",1000);
                Chord.lookup(key,1001);
                Chord.lookup(key1,1002);
                Chord.lookup(key,1003);
                Chord.deleteFile(key,1000);
                Chord.lookup(key,1002);

            }
        }, 10000, 60000);



        Timer timer1 = new Timer();
        timer1.schedule(new TimerTask() {
            @Override
            public void run() {
                Utilities.setTesting(true);
                for (int i=1; i<5; i++){
                    Chord.deleteNode(1000+i);
                }
                try{
                    Chord.join("127.0.0.1",1100, "127.0.0.1", 10001 );
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }, 10000);


        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                Chord.printChord();
            }
        }, 30000, 30000);

        */
    }


}
