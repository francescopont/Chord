package chord.model;

import chord.Exceptions.SuccessorListException;
import chord.Exceptions.TimerExpiredException;

import java.util.Timer;

public class Node {
    private NodeInfo nodeInfo;
    private String nodeidentifier;
    private FingerTable fingerTable;
    private SuccessorList successorList;
    private NodeInfo predecessor;
    private boolean initialized;
    private boolean terminated;
    private NodeDispatcher dispatcher;
    private int fix_finger_counter;
    private NodeComparator comparator;

    //this constructor is called when you CREATE and when you JOIN an existent Chord
    public Node(NodeInfo me) {
        this.nodeInfo = me;
        //I need to computer the identifier associated to this node, given the key
        this.nodeidentifier = me.getHash();
        this.fingerTable = new FingerTable(me.getHash());
        this.successorList = new SuccessorList(me.getHash());
        this.predecessor = null;
        this.initialized = false;
        this.terminated = false;
        this.dispatcher = new NodeDispatcher(this.getPort());
        this.fix_finger_counter = 0;
        this.comparator=new NodeComparator(me.getHash());
    }

    //getters
    public int getPort() {
        return this.nodeInfo.getPort();
    }
    public NodeInfo getPredecessor() {
        return predecessor;
    }

    //this method is called from messageHandler
    public NodeDispatcher getDispatcher() {
        return dispatcher;
    }

    public void modifyPort(int port) {
        this.nodeInfo.setPort(port);
        NodeInfo me = this.nodeInfo;
        String key = me.getIPAddress().concat(Integer.toString(me.getPort()));
        this.nodeidentifier = Utilities.hashfunction(key);
    }

    //periodic operations to handle changes in the chord
    public void stabilize() {
        try {
            NodeInfo successor = this.successorList.getFirstElement();
                NodeInfo potentialSuccessor = this.dispatcher.sendPredecessorRequest(successor, this.nodeInfo);
                String successorKey = successor.getHash();
                String potentialSuccessorKey = potentialSuccessor.getHash();
                if((comparator.compare(potentialSuccessorKey,nodeidentifier)>=0)&& (comparator.compare(potentialSuccessorKey,successorKey)<=0)){
                    this.successorList.modifyEntry(0,potentialSuccessor);
                }
        } catch (TimerExpiredException e) {
            //put code here
        }
        try {
            NodeInfo successor = this.successorList.getFirstElement();
            this.dispatcher.sendNotify(successor, this.nodeInfo);
        } catch (TimerExpiredException e) {
            //put code here
        }

    }

    public synchronized void fix_finger() {
        String hashedkey = Utilities.computefinger(this.nodeidentifier, fix_finger_counter);
        NodeInfo nodeInfo = find_successor(hashedkey);
        this.fingerTable.modifyFinger(fix_finger_counter, nodeInfo);
        fix_finger_counter++;
        if (fix_finger_counter == Utilities.numberOfBit()){
            fix_finger_counter = 0;
        }
    }

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

    //ritorno il primo elemento della mia successorList
    public NodeInfo getFirstSuccessor(){
        return successorList.getFirstElement();
    }

    //[in a recursive manner]
    //ask this node to find the successor of id
    //param = an hashed identifier of the item I want to retrieve
    public NodeInfo find_successor( String key){
        NodeInfo successor=null;

        if(key.equals(this.nodeidentifier)){
            return this.nodeInfo;
        }
        //am I responsible for that key? If yes return myself
        FingerTableComparator comparator= new FingerTableComparator(this.nodeidentifier);
        if(predecessor!=null){
            String predecessorKey= predecessor.getHash();
            if((comparator.compare(this.nodeidentifier,key)>=0)&& (comparator.compare(predecessorKey,key)<0)){
                return this.nodeInfo;
            }
        }
        //Is anyone from the successor list responsable for that key?
        try {
            successor = successorList.closestSuccessor(key);
            return successor;
            } catch (SuccessorListException e) {
        }

        //look in the finger table
        try {
            NodeInfo closestPredecessor = fingerTable.closestPredecessor(key);
            successor = this.dispatcher.sendSuccessorRequest(closestPredecessor,key,this.nodeInfo);
        } catch (TimerExpiredException ex) {
            ex.printStackTrace();
        }

        return successor;

    }



    //when you create a new chord, you have to initialize all the stuff
    //this method is called when you create a new Chord
    public synchronized void initialize() {
        for (int i = 0; i < Utilities.numberOfBit(); i++) {
            fingerTable.addFinger( this.nodeInfo);
        }
        for (int i = 0; i < 4; i++) {
            successorList.addEntry(this.nodeInfo);
        }
        this.predecessor = this.nodeInfo;
        this.initialized = true;
        Timer timer = new Timer();
        timer.schedule(new Utilities(this), Utilities.getPeriod(),Utilities.getPeriod());
        this.printStatus();
    }



    public synchronized void initialize(final NodeInfo myfriend) {
        try {
            NodeInfo successor = this.dispatcher.sendSuccessorRequest(myfriend, this.nodeidentifier, this.nodeInfo);
            this.successorList.addEntry(successor);
            this.fingerTable.addFinger( successor);
            this.predecessor=null;
        } catch (TimerExpiredException e) {
            //put code here
        }

        //now I populate the successor list and the finger table on a separate thread
        new Thread(() -> {
            //first, the successor list
            for (int i = 1; i < 4; i++) {
                NodeInfo predecessor = successorList.getLastElement();
                String key = predecessor.getHash();
                NodeInfo successor = null;
                try {
                    successor = dispatcher.sendFirstSuccessorRequest(successor,nodeInfo);
                } catch (TimerExpiredException e) {
                    //put code here
                }

                if (successor.getHash().equals(nodeidentifier)) {
                    while (i < 4) {
                        successorList.addEntry( nodeInfo);
                        i++;
                    }
                } else {
                    successorList.addEntry(successor);
                }
            }
            for(int i=1; i<Utilities.numberOfBit(); i++) {
                String hashedkey = Utilities.computefinger(nodeidentifier, i);
                NodeInfo finger = null;
                NodeInfo successor = successorList.getFirstElement();
                try {
                    finger = dispatcher.sendSuccessorRequest(successor, hashedkey, nodeInfo);
                    fingerTable.addFinger(finger);
                } catch (TimerExpiredException e) {
                    e.printStackTrace();
                }
            }
            initialized = true;
        }).start();
        Timer timer = new Timer();
        timer.schedule(new Utilities(this), Utilities.getPeriod(), Utilities.getPeriod());
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

    public boolean isInitialized() {
        return initialized;
    }

    public void terminate() {
        this.terminated = true;
    }

    public boolean isTerminated() {
        return terminated;
    }


    //useful for testing
    public void printStatus() {
        System.out.println("-------------------");
        System.out.println("It's me:  " + this.nodeidentifier + "!");
        if (predecessor != null){
            System.out.println("predecessor: " + predecessor.getHash());
        }
        else {
            System.out.println("Predecessor is null");
        }

        successorList.printTable();
        fingerTable.printTable();
        System.out.println("-------------------");
    }

    public FingerTable getFingerTable() {
        return fingerTable;
    }

    public SuccessorList getSuccessorList() {
        return successorList;
    }

    public NodeInfo getNodeInfo() {
        return nodeInfo;
    }


    public void setNodeInfo(NodeInfo nodeInfo) {
        this.nodeInfo = nodeInfo;
    }

    public void setNodeidentifier(String nodeidentifier) {
        this.nodeidentifier = nodeidentifier;
    }

    public void setFingerTable(FingerTable fingerTable) {
        this.fingerTable = fingerTable;
    }

    public void setSuccessorList(SuccessorList successorList) {
        this.successorList = successorList;
    }

    public void setPredecessor(NodeInfo predecessor) {
        this.predecessor = predecessor;
    }

    public void setDispatcher(NodeDispatcher nodeDispatcher){
        this.dispatcher=nodeDispatcher;
    }





}

