package chord.model;

import chord.Exceptions.SuccessorListException;
import chord.Exceptions.TimerExpiredException;

import java.util.*;

public class Node {
    private NodeInfo nodeInfo;
    private String nodeidentifier;
    //attenzione: devo sincerarmi che queste liste mantengano l'ordine di inserimento
    private FingerTable finger_table;
    private SuccessorList successor_list;
    private NodeInfo predecessor;
    private boolean initialized;
    private boolean terminated;
    private final NodeDispatcher dispatcher;
    private int fix_finger_counter;
    private NodeComparator comparator;

    //this constructor is called when you CREATE and when you JOIN an existent Chord
    public Node(NodeInfo me) {
        this.nodeInfo = me;
        //I need to computer the identifier associated to this node, given the key
        String key = me.getIPAddress().concat(Integer.toString(me.getPort()));
        this.nodeidentifier = Utilities.hashfunction(key);
        this.finger_table = new FingerTable(me.getHash());
        this.successor_list = new SuccessorList(me.getHash());
        this.predecessor = null;
        this.initialized = false;
        this.terminated = false;
        this.dispatcher = new NodeDispatcher(this.getPort());
        this.fix_finger_counter = 1;
        this.comparator=new NodeComparator(me.getHash());

    }

    public int getPort() {
        return this.nodeInfo.getPort();
    }
    public NodeInfo getPredecessor() {
        return predecessor;
    }

    public void modifyPort(int port) {
        this.nodeInfo.setPort(port);
        NodeInfo me = this.nodeInfo;
        String key = me.getIPAddress().concat(Integer.toString(me.getPort()));
        this.nodeidentifier = Utilities.hashfunction(key);
    }

    //not implemented yet[periodic operations to handle changes in the chord]
    public void stabilize() {
        try {
            NodeInfo potential_successor = this.dispatcher.sendPredecessorRequest(this.successor_list.getFirst(), this.nodeInfo);
            String successor_key = successor_list.getFirst().getHash();
            String potential_successor_key = potential_successor.getHash();

            if((comparator.compare(potential_successor_key,nodeidentifier)>=0)&& (comparator.compare(potential_successor_key,successor_key)<=0)){
                this.successor_list.setFirst(potential_successor_key,potential_successor);
            }

        } catch (TimerExpiredException e) {
            NodeInfo old_successor = this.successor_list.removeFirst();
            this.finger_table.removeFirst();
        }
        try {
            this.dispatcher.sendNotify(this.successor_list.getFirst(), this.nodeInfo);
        } catch (TimerExpiredException e) {
            //put code here
        }

        //una volta che mi arriva la risposta cosa ci faccio?? niente???
    }

    public void fix_finger() {
        String hashedkey = Utilities.computefinger(this.nodeidentifier, fix_finger_counter);
        NodeInfo nodeInfo = find_successor(hashedkey);
        this.finger_table.modifyEntry(fix_finger_counter, nodeInfo);
        fix_finger_counter++;
        if (fix_finger_counter == 17){
            fix_finger_counter = 1;
        }
    }

    //da sistemare
    public void check_predecessor() {
        if (predecessor != null) {
            try {
                dispatcher.sendPing(this.predecessor, this.nodeInfo);
            } catch (TimerExpiredException e) {
                predecessor = null;
                //put code here
            }
        }
        return;
    }


    //[in a recursive manner]
    //ask this node to find the successor of id
    //param = an hashed identifier of the item I want to retrieve
    public NodeInfo find_successor( String key){
        NodeInfo successor=null;
        //am I responsible for that key? If yes return myself
        if(predecessor!=null){
            String predecessorKey= predecessor.getHash();
            if((comparator.compare(this.nodeidentifier,key)>=0)&& (comparator.compare(predecessorKey,key)<0)){
                return this.nodeInfo;
            }
        }
        //provo la successor list
        try {
            successor= successor_list.getSuccessor(key);
        } catch (SuccessorListException e) {
            try {
                //provo la fingertable
                successor= finger_table.closestSuccessor(key);
                successor= this.dispatcher.sendSuccessorRequest(successor,key,this.nodeInfo);
            } catch (TimerExpiredException ex) {
                ex.printStackTrace();
            }
        }

        return successor;

    }

    //when you create a new chord, you have to initialize all the stuff
    public synchronized void initialize() {
        for (int i = 0; i < 16; i++) { //mi servono sempre i contatori? si per forza
            finger_table.addEntry(this.nodeidentifier, this.nodeInfo);
        }
        for (int i = 0; i < 4; i++) {
            successor_list.addEntry(this.nodeidentifier,this.nodeInfo);
        }
        this.predecessor = this.nodeInfo;
        this.initialized = true;
        Timer timer = new Timer();
        timer.schedule(new Utilities(this), 100000000,1000000);
    }

    public boolean isInitialized() {
        return initialized;
    }

    public synchronized void initialize(final NodeInfo myfriend) {
        NodeInfo successor = null;
        try {
            successor = this.dispatcher.sendSuccessorRequest(myfriend, this.nodeidentifier, this.nodeInfo);
        } catch (TimerExpiredException e) {
            //put code here
        }
        this.successor_list.addEntry(successor.getHash(),successor);
        this.finger_table.addEntry(successor.getHash(), successor); // come faccio ad essere sicura che sia in posizione 0??
        this.predecessor=null;

        //now I populate the successor list and the finger table on a separate thread
        new Thread(new Runnable() {
            @Override
            public void run() {

                //first, the successor list
                //QUA CI SONO UN PO' DI CORNER CASES DA GESTIRE
                //capire come fare a fare get e set delle POSIZIONI (un po' sbatti)
                for (int i = 1; i < 4; i++) {
                    NodeInfo predecessor = successor_list.getSuccessor(i - 1);
                    String key = predecessor.getHash();
                    NodeInfo successor = null;
                    try {
                        successor = dispatcher.sendSuccessorRequest(predecessor, key, nodeInfo);
                    } catch (TimerExpiredException e) {
                        //put code here
                    }

                    //questo è un primo modo di gestire un corner case
                    if (successor.equals(nodeidentifier)) {
                        while (i < 4) {
                            successor_list.addEntry(nodeidentifier, nodeInfo);
                            i++;
                        }
                    } else {
                        successor_list.addEntry(successor.getHash(),successor);
                    }
                }

                for(int i=2; i<16; i++) {
                    String hashedkey = Utilities.computefinger(nodeidentifier, i);
                    NodeInfo finger = null;
                    try {
                        finger = dispatcher.sendSuccessorRequest(successor_list.getFirst(), hashedkey, nodeInfo);
                    } catch (TimerExpiredException e) {
                        e.printStackTrace();
                    }
                    //finger_table.add(i-1, finger);
                    finger_table.addEntry(hashedkey,finger); //può funzionare perchè l'ordine sarà giusto o poi sistemato?
                    //ci pensa poi la stabilize a sistemarla?? o ci penso io subito??
                }

                initialized = true;
                printStatus();
            }
        }).start();

        Timer timer = new Timer();
        timer.schedule(new Utilities(this), 10000, 10000);
    }

    public NodeDispatcher getDispatcher() {
        return dispatcher;
    }

    //quando ricevo la notify controllo il mio predecessore e in caso lo aggiorno
    public void notify(NodeInfo potential_predecessor) {
        if (this.predecessor == null) {
            this.predecessor = potential_predecessor;
        } else {
            //ho le due chiavi
            String predecessor_key = this.predecessor.getHash();
            String potential_key = potential_predecessor.getHash();
            //se la chiave del potenziale successore è più piccola del successore e più grande del nodo, allora ho trovato un nuovo predecessore
            if((comparator.compare(predecessor_key,potential_key)<=0) && (comparator.compare(potential_key,nodeidentifier)>=0)){
                this.predecessor=potential_predecessor;
            }
        }
    }

    public void terminate() {
        this.terminated = true;
    }

    public boolean isTerminated() {
        return terminated;
    }


    //useful for testing
    public void printStatus() {
        System.out.println("It's me:  " + this.nodeidentifier + "!\n" +
                "Predecessor: " + this.predecessor + "\n"
                + "Successor list: " + "\n");
        for (NodeInfo nodeInfo: this.successor_list){
            System.out.println(nodeInfo.getIPAddress() + "-" + nodeInfo.getPort() + "\n");
        }
        System.out.println("finger table: " + "\n");
        for (NodeInfo nodeInfo: this.finger_table){
            System.out.println(nodeInfo.getIPAddress() + "-" + nodeInfo.getPort() + "\n");
        }
        System.out.println("\n\n");
    }
}

