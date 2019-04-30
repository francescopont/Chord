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
//robustness of code ( non available ports, etc...)


import chord.Messages.Message;
import chord.Exceptions.PortException;
import chord.network.Router;

import java.util.LinkedList;
import java.util.List;

public class Chord{
    //the list of virtual nodes this application is handling
    private final static List<Node> virtualnodes = new LinkedList<>();


    //don't let anyone instantiate this class
    private Chord(){};

    // Questo metodo può essere chiamato staticamente come si chiamano i metodi della libreria Math per esempio
    //mi sembra la soluzione che più assomiglia ad una libreria "vera" e  che maschera tutta l'implementazione interna
    //inoltre ciascuno nodo fisico può in questo modo creare e gestire tanti nodi virtuali quanti vuole, e
    //il fatto di poter gestire più nodi virtuali migliora le performance, according to the paper
    public static void join(String IPAddress, int port, String knownIPAddress, int knownPort) throws PortException {
        synchronized (virtualnodes){
            //questo codice è davvero necessario???
            NodeInfo nodeInfo = new NodeInfo(IPAddress,port);
            NodeInfo knownnode = new NodeInfo(knownIPAddress,knownPort);
            Node node = new Node(nodeInfo);
            virtualnodes.add(node);
            try{
                Router.addnode(port);
            }catch(PortException e){
                node.modifyPort(e.getPort());
                throw e;
            }

            node.initialize(knownnode);
        }

    }

    public static void create(String IPAddress, int port)throws PortException {
        synchronized (virtualnodes){
            NodeInfo nodeInfo = new NodeInfo(IPAddress,port);
            Node node = new Node(nodeInfo);
            virtualnodes.add(node);
            try{
                Router.addnode(port);
            }catch(PortException e){
                node.modifyPort(e.getPort());
                throw e;
            }
            node.initialize();
        }
    }

    public static String lookup(String key){

        String hashedkey = Utilities.hashfunction(key);
        NodeInfo nodeInfo;
        //come gestiamo il fatto che un host possiede più nodi virtuali? deve poter selezionare
        //da quale nodo far partire la query?
        for (Node virtualnode: virtualnodes){
            nodeInfo = virtualnode.find_successor(hashedkey);
        }
        //dobbiamo accordarci su cosa debba ritornare?? una concat di ip e porta???
        return "not implemented yet";
    };

    public static void deliverMessage(int port, Message message){
        System.out.println("messaggio ricevuto");
        for (Node virtualnode: virtualnodes){
            if (virtualnode.getPort() == port){
                MessageHandler handler = new MessageHandler(virtualnode,message);
                System.out.println("destination: " + port + " message id[recevied]: " +message.getId());
                new Thread(handler).start();
            }
        }
    }

    public void deleteNode(int port){
        Router.terminate(port);
        for (Node virtualnode: virtualnodes){
            if (virtualnode.getPort() == port){
                virtualnode.terminate();
            }
        }
    }

    //useful for testing
    public static void addNodeTesting(Node node){
        virtualnodes.add(node);
    }


}
