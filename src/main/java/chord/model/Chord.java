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


import java.util.LinkedList;
import java.util.List;
import java.util.Timer;

public class Chord{
    //the list of virtual nodes this application is handling
    private static List<Node> virtualnodes;

    //don't let anyone instantiate this class
    private Chord(){};

    // Questo metodo può essere chiamato staticamente come si chiamano i metodi della libreria Math per esempio
    //mi sembra la soluzione che più assomiglia ad una libreria "vera" e  che maschera tutta l'implementazione interna
    //inoltre ciascuno nodo fisico può in questo modo creare e gestire tanti nodi virtuali quanti vuole, e
    //il fatto di poter gestire più nodi virtuali migliora le performance, according to the paper
    public static void join(String IPAddress, int port, String knownIPAddress, int knownPort)throws Exception{
        synchronized (virtualnodes){
            if (virtualnodes == null){
                virtualnodes = new LinkedList<>();
                //per l'esecuzione delle operazioni periodiche: i tempi sono a casaccio
                Timer timer = new Timer (false);
                timer.schedule(new Utilities(virtualnodes), 0, 20000000);
            }
            //questo codice è davvero necessario???
            for(Node node: virtualnodes){
                if (node.getPort() == port){
                    throw new Exception("Port already in use!");
                }
            }


            virtualnodes.add(new Node(new NodeInfo(IPAddress,port), new NodeInfo(knownIPAddress, knownPort)));

        }



    }

    public static void create(String IPAddress, int port) throws Exception{
        synchronized (virtualnodes){
            if (virtualnodes == null){
                virtualnodes = new LinkedList<>();
                //per l'esecuzione delle operazioni periodiche: i tempi sono a casaccio
                Timer timer = new Timer (false);
                timer.schedule(new Utilities(virtualnodes), 0, 20000000);
            }
            //questo codice è davvero necessario???
            for(Node node: virtualnodes){
                if (node.getPort() == port){
                    throw new Exception("Port already in use!");
                }
            }
            virtualnodes.add(new Node(new NodeInfo(IPAddress,port)));

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
}
