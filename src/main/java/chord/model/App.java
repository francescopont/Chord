package chord.model;

import chord.Exceptions.NotInitializedException;
import chord.Exceptions.PortException;

import java.net.UnknownHostException;
import java.util.Scanner;
import java.net.InetAddress;
/**
 * Hello world!
 *
 */
public class App {
    //piccolo test
    public static void main(String[] args) {
        Boolean exit=false;
        Scanner scanner=new Scanner(System.in);
        while(!exit) {
            System.out.println("Selezionare un'opzione:\n c for Create \n j for Join\n p for publish\n g for get a file\n d for delete a file\n t for terminate a node\n ps for print the Chord status\n e for exit ");
            String input = scanner.nextLine();
            if (input.equals("c")) {
                String ip=null;
                try {
                    InetAddress me = InetAddress.getLocalHost();
                    ip= me.getHostAddress();
                    System.out.println("Your IP address is: "+ ip);
                }
                catch (UnknownHostException e){
                    e.printStackTrace();
                }
                System.out.println("Insert port Number: ");
                int port = scanner.nextInt();
                try {
                    long previous = System.currentTimeMillis();
                    Chord.create(ip, port);
                    long after = System.currentTimeMillis();
                    System.out.println("time: " + (after - previous));
                } catch (PortException e) {
                    System.out.println("The chosen port is already in use. Your new port is: " +e.getPort());
                }
                scanner.skip("\n");
            }
            else if (input.equals("j")) {
                String ip=null;
                try {
                    InetAddress me = InetAddress.getLocalHost();
                    ip= me.getHostAddress();
                    System.out.println("Your IP address is: "+ ip);
                }
                catch (UnknownHostException e){
                    e.printStackTrace();
                }
                System.out.println("Insert the IP address of your friend: ");
                String friendIp = scanner.nextLine();
                System.out.println("Insert your port Number: ");
                int port = scanner.nextInt();
                System.out.println("Insert the port Number of your BFF: ");
                int friendPort = scanner.nextInt();
                try {
                    Chord.join(ip, port, friendIp, friendPort);
                } catch (PortException e) {
                    System.out.println("The chosen port is already in use. Your new port is: " +e.getPort());
                } catch (NotInitializedException e) {
                    System.out.println(e.getMessage() + ". Try with another IP address");
                }
                scanner.skip("\n");
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
                scanner.skip("\n");
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
                    scanner.skip("\n");
                }
                else{
                    System.out.println("The key must be of 4 characters");
                }
            }
            else if(input.equals("d")){
                System.out.println("Insert the file key :");
                String key= scanner.nextLine();
                if(key.length()==4) {
                    System.out.println("Insert your port number :");
                    int port = scanner.nextInt();
                    try {
                        Chord.deleteFile(key, port);
                    } catch (NotInitializedException e) {
                        System.out.println(e.getMessage());
                    }
                    scanner.skip("\n");
                }
                else
                    System.out.println("The key must be of 4 characters");
            }
            else if(input.equals("t")){
                System.out.println("Insert your port number : ");
                int port= scanner.nextInt();
                try {
                    Chord.deleteNode(port);
                } catch (NotInitializedException e) {
                    e.printStackTrace();
                }
                scanner.skip("\n");
            }
            else if(input.equals("ps")){
                Chord.printChord();
            }
            else if(input.equals("e")){
                Chord.deleteAll();
                exit=true;
            }
            else{
                System.out.println("Wrong input");
            }
        }
        System.out.println("Program terminate");
    }
}




