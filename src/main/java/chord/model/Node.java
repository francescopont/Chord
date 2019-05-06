package chord.model;

import chord.Exceptions.NotInitializedException;
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
            NodeInfo potential_predecessor = this.dispatcher.sendPredecessorRequest(this.successor_list.getFirst(), this.nodeInfo);
            String key = successor_list.getFirst().getIPAddress().concat(Integer.toString(successor_list.getFirst().getPort()));
            String hashedkey_successor = Utilities.hashfunction(key);
            String potential_key = potential_predecessor.getIPAddress().concat(Integer.toString(potential_predecessor.getPort()));
            String hashedkey_potential_predecessor = Utilities.hashfunction(potential_key);

            if (hashedkey_successor.compareTo(this.nodeidentifier) < 0) {
                if (hashedkey_potential_predecessor.compareTo(this.nodeidentifier) > 0 || hashedkey_potential_predecessor.compareTo(hashedkey_successor) < 0) {
                    this.successor_list.set(0, potential_predecessor);
                }
            } else if (hashedkey_potential_predecessor.compareTo(hashedkey_successor) < 0 && hashedkey_potential_predecessor.compareTo(this.nodeidentifier) > 0) {
                this.successor_list.set(0, potential_predecessor);
            }

        } catch (TimerExpiredException e) {
            NodeInfo old_successor = this.successor_list.removeFirst();
            this.finger_table.remove(0);
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
        this.finger_table.set(fix_finger_counter, nodeInfo);
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
            Comparator comparator=new NodeComparator(this.nodeidentifier);
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
                successor= finger_table.closestSuccessor(nodeInfo);
                successor= this.dispatcher.sendSuccessorRequest(successor,key,this.nodeInfo);
            } catch (NotInitializedException ex) {
                ex.printStackTrace();
            } catch (TimerExpiredException ex) {
                ex.printStackTrace();
            }
        } catch (NotInitializedException e) {
            e.printStackTrace();
        }

        return successor;

    }

    //when you create a new chord, you have to initialize all the stuff
    public synchronized void initialize() {
        for (int i = 0; i < 16; i++) {
            finger_table.add(this.nodeInfo);
        }

        for (int i = 0; i < 4; i++) {
            successor_list.add(this.nodeInfo);
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
        this.successor_list.add(successor);
        this.finger_table.add(0, successor);
        this.predecessor=null;

        //now I populate the successor list and the finger table on a separate thread
        new Thread(new Runnable() {
            @Override
            public void run() {

                //first, the successor list
                //QUA CI SONO UN PO' DI CORNER CASES DA GESTIRE
                for (int i = 1; i < 4; i++) {
                    NodeInfo predecessor = successor_list.get(i - 1);
                    String key = predecessor.getIPAddress().concat(Integer.toString(predecessor.getPort()));
                    String hashedkey = Utilities.hashfunction(key);
                    NodeInfo successor = null;
                    try {
                        successor = dispatcher.sendSuccessorRequest(predecessor, hashedkey, nodeInfo);
                    } catch (TimerExpiredException e) {
                        //put code here
                    }

                    //questo è un primo modo di gestire un corner case
                    if (successor.equals(nodeidentifier)) {
                        while (i < 4) {
                            successor_list.addLast(nodeInfo);
                            i++;
                        }
                    } else {
                        successor_list.addLast(successor);
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
                    finger_table.add(i-1, finger);
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
            String key = this.predecessor.getIPAddress().concat(Integer.toString(nodeInfo.getPort()));
            String hashedkey_predecessor = Utilities.hashfunction(key);
            String potential_key = potential_predecessor.getIPAddress().concat(Integer.toString(nodeInfo.getPort()));
            String hashedkey_potential_predecessor = Utilities.hashfunction(potential_key);
            if (hashedkey_predecessor.compareTo(this.nodeidentifier) < 0) {
                if (hashedkey_potential_predecessor.compareTo(this.nodeidentifier) < 0 && hashedkey_potential_predecessor.compareTo(hashedkey_predecessor) > 0) {
                    this.predecessor = potential_predecessor;
                }
            } else if (hashedkey_potential_predecessor.compareTo(hashedkey_predecessor) > 0 || hashedkey_potential_predecessor.compareTo(this.nodeidentifier) < 0) {
                this.predecessor = potential_predecessor;
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

