package chord.model;
//DISCLAIMER

//le note in italiano sono per spiegare il codice informalmente, le note in inglese sono documentazione definitiva
//problemi che abbiamo ora: SINCRONIZZAZIONE
//dal momento che SHA-1 mappa su 160 bit, non possiamo usare nè gli int nè i long per rappresentare questi numeri,
//sicchè int usa 16 bit e long 32 bit
//-> soluzione: usiamo le stringhe e le confrontiamo con compareto, un metodo già implementato fornito dalla libreria String
//attenzione alla convenzione corretta:
//key indica ip.concat(port)
//nodeidentifier indica l'hash di key

//TO DO


import chord.Exceptions.NotInitializedException;
import chord.Exceptions.PortException;
import chord.Messages.Message;
import chord.network.Router;
import com.google.gson.Gson;

import java.util.LinkedList;
import java.util.List;

public class Chord{
    //the list of virtual nodes this application is handling
    private static final  List<Node> virtualnodes = new LinkedList<>();


    //don't let anyone instantiate this class
    private Chord(){};

    //static methods to be used from the application layer
    public static void join(String IPAddress, int port, String knownIPAddress, int knownPort) throws PortException, NotInitializedException {
        synchronized (virtualnodes){
            if (virtualnodes.isEmpty()){
                Router.setIPAddress(IPAddress);
            }
            NodeInfo nodeInfo = new NodeInfo(IPAddress,port);
            NodeInfo knownnode = new NodeInfo(knownIPAddress,knownPort);
            Node node = new Node(nodeInfo);
            virtualnodes.add(node);
            try{
                Router.addnode(port);
            }catch(PortException e){
                node.modifyPort(e.getPort());
                throw e;
            }finally {
                node.initialize(knownnode);
            }

            //try{
            /*}catch (NotInitializedException e){
                virtualnodes.remove(node);
                Router.terminate(node.getPort());
                System.out.println(e.getMessage());
                throw new NotInitializedException(e.getMessage());
            }*/
        }
    }

    public static void create(String IPAddress, int port)throws PortException {
        synchronized (virtualnodes){
            if (virtualnodes.isEmpty()){
                Router.setIPAddress(IPAddress);
            }
            NodeInfo nodeInfo = new NodeInfo(IPAddress,port);
            Node node = new Node(nodeInfo);
            virtualnodes.add(node);
            try{
                Router.addnode(port);
            }catch(PortException e){
                node.modifyPort(e.getPort());
                throw e;
            }finally {
                node.initialize();
            }
        }
    }

    public static String lookup(String key){

        String hashedkey = Utilities.hashfunction(key);
        NodeInfo nodeInfo;
        //come gestiamo il fatto che un host possiede più nodi virtuali? deve poter selezionare
        //da quale nodo far partire la query?
        for (Node virtualnode: virtualnodes){
            nodeInfo = virtualnode.findSuccessor(hashedkey);
        }
        //dobbiamo accordarci su cosa debba ritornare?? una concat di ip e porta???
        return "not implemented yet";
    };


    public static void deleteNode(int port){
        Node node = null;
        for (Node virtualnode: virtualnodes){
            if (virtualnode.getPort() == port){
                node = virtualnode;
            }
        }
        System.out.println("Sto cancellando il nodo "+ node.getNodeInfo().getHash());
        node.terminate();
        Router.terminate(port);
        virtualnodes.remove(node);
    }

    //this method is called from the socket layer to delived a message to the chord layer
    public static void deliverMessage(int port, Message message){
        for (Node virtualnode: virtualnodes){
            if (virtualnode.getPort() == port){
                MessageHandler handler = new MessageHandler(virtualnode,message);
                Threads.executeImmediately(handler);
            }
        }
    }

    //publish method
    public static String publish(Object o, int port){
        Gson gson=new Gson();
        String json=gson.toJson(o);
        String key= Utilities.hashfunction(json);
        System.out.println("Ecco la chiave: "+ key);
        Node me=null;
        for(Node node: virtualnodes){
            if(node.getPort()==port){
                me=node;
            }
        }
        System.out.println(json);
        me.publish(json,key);
        return key;
    }

    public static void publish(Object o, String key, int port){
        Gson gson=new Gson();
        String json=gson.toJson(o);
        Node me=null;
        for(Node node: virtualnodes){
            if(node.getPort()==port){
                me=node;
            }
        }
        System.out.println(json);
        me.publish(json,key);
    }

    public static void getFile(String key, int port){
        Node me=null;
        for(Node node: virtualnodes){
            if(node.getPort()==port){
                me=node;
            }
        }
        String file= me.getFile(key);
        if(file!=null){
            System.out.println(file);
        }
        else{
            System.out.println("Nessuno ha questo file");
        }
    }

    public static void deleteFile(String key, int port){
        Node me=null;
        for(Node node: virtualnodes){
            if(node.getPort()==port){
                me=node;
            }
        }
        me.deleteFile(key);
    }

    //useful for testing
    public static void addNodeTesting(Node node){
        virtualnodes.add(node);
    }

    public static void printChord() {
        for (Node node : virtualnodes) {
            node.printStatus();
        }
    }


}
