package chord.model;

import chord.Exceptions.PredecessorException;
import chord.Exceptions.SuccessorListException;
import chord.Exceptions.TimerExpiredException;

import java.util.concurrent.ScheduledFuture;

public class Node {
    private NodeInfo nodeInfo;
    private String nodeidentifier;
    private FingerTable fingerTable;
    private SuccessorList successorList;
    private NodeInfo predecessor;
    private boolean initialized;
    private boolean alone;
    private boolean terminated;
    private NodeDispatcher dispatcher;
    private int fix_finger_counter;
    private NodeComparator comparator;
    private ScheduledFuture terminate;

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
        this.alone = true;
    }

    //getters
    public int getPort() {
        return this.nodeInfo.getPort();
    }
    public NodeInfo getPredecessor()throws PredecessorException {
        if (this.predecessor == null){
            throw new PredecessorException();
        }
        return predecessor;
    }

    public boolean isAlone() {
        return alone;
    }

    public void setAlone(boolean alone) {
        this.alone = alone;
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
                if(potentialSuccessor.getHash().equals(this.nodeidentifier)) {
                    return;
                }
                String successorKey = successor.getHash();
                String potentialSuccessorKey = potentialSuccessor.getHash();
                if(comparator.compare(potentialSuccessorKey,successorKey)<0){
                    System.out.println("I'm : " + this.nodeidentifier + "and I'm modifying the successor due to stabilize :" +potentialSuccessorKey);
                    this.successorList.modifyEntry(0,potentialSuccessor);
                }
        } catch (TimerExpiredException e) {
            //put code here
        }catch (PredecessorException e){
            System.out.println("My successor said me he does not have a predecessor: " + this.nodeidentifier);
        }
        try {
            NodeInfo successor = this.successorList.getFirstElement();
            this.dispatcher.sendNotify(successor, this.nodeInfo);
        } catch (TimerExpiredException e) {
            //put code here
        }

    }

    public void fix_finger() {
        String hashedkey = Utilities.computefinger(this.nodeidentifier, fix_finger_counter);
        NodeInfo nodeInfo = find_successor(hashedkey);
        if (nodeInfo == null){
            System.out.println("troppo lento!");
        }
        this.fingerTable.modifyFinger(fix_finger_counter, nodeInfo);
        fix_finger_counter++;
        if (fix_finger_counter == Utilities.numberOfBit()){
            fix_finger_counter = 0;
        }
    }

    public void fix_successor_list(){
        for (int i = 0; i<3; i++){
            NodeInfo predecessor = successorList.getElement(i);
            NodeInfo successor = null;
            try {
                successor = dispatcher.sendFirstSuccessorRequest(predecessor,nodeInfo);
                if (successor.getHash().equals(nodeidentifier)) {
                    while (i < 3) {
                        successorList.modifyEntry(i+1, this.nodeInfo);
                        i++;
                    }
                } else {
                    successorList.modifyEntry(i+1, successor);
                }
            } catch (TimerExpiredException e) {
                System.out.println("I'm " + this.nodeidentifier + " and I experienced an exception in the successor list, getting the element "+ i);
                //put code here
            }

        }
    }

    public void check_predecessor() {
        if (predecessor != null) {
            try {
                dispatcher.sendPing(this.predecessor, this.nodeInfo);
            } catch (TimerExpiredException e) {
                System.out.println("I'm "+ this.nodeidentifier+ " and I'm trying to ping my predecessor, but time expired");
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
        this.terminate  = Threads.executePeriodically(new Utilities(this));
        this.printStatus();
        this.printUtilities();

    }

    public synchronized void start(NodeInfo nodeInfo){
        if (alone){
            this.successorList.modifyEntry(0, nodeInfo);
            this.fingerTable.modifyFinger(0, nodeInfo);
            setAlone(false);
        }

    }

    public synchronized void initialize(final NodeInfo myfriend) {
        try {
            NodeInfo successor = this.dispatcher.sendSuccessorRequest(myfriend, this.nodeidentifier, this.nodeInfo);
            this.successorList.addEntry(successor);
            this.fingerTable.addFinger( successor);
            this.predecessor=null;
        } catch (TimerExpiredException e) {
            System.out.println("tempo finito sull'inizialize di " + nodeidentifier);
        }

        //first, the successor list
        for (int i = 1; i < 4; i++) {
            NodeInfo predecessor = successorList.getLastElement();
            NodeInfo successor = null;
            try {
                successor = dispatcher.sendFirstSuccessorRequest(predecessor,nodeInfo);
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
        setAlone(false);
        this.printStatus();
        this.printUtilities();
        try{
            dispatcher.sendStartRequest(myfriend, this.nodeInfo);
        }catch (TimerExpiredException e){
            //put code here
        }
        this.terminate  = Threads.executePeriodically(new Utilities(this));

    }

    //quando ricevo la notify controllo il mio predecessore e in caso lo aggiorno
    public void notify(NodeInfo potential_predecessor) {
        if(potential_predecessor.equals(this.nodeInfo)){
            System.out.println("sto notificando me stesso e sono: "+this.nodeidentifier);
            return;
        }
        if (this.predecessor == null) {
            this.predecessor = potential_predecessor;
        } else {
            //ho le due chiavi
            String predecessor_key = this.predecessor.getHash();
            String potential_key = potential_predecessor.getHash();
            //se la chiave del potenziale successore è più piccola del successore e più grande del nodo, allora ho trovato un nuovo predecessore
            if(comparator.compare(predecessor_key,potential_key)<0){
                this.predecessor=potential_predecessor;
            }
        }
    }

    public boolean isInitialized() {
        return initialized;
    }

    public void terminate() {
        terminate.cancel(true);
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

    public void printUtilities(){
        System.out.println("FINGERS ->");
        for (int i=0; i< 16; i++){
            System.out.println("finger "+ i + ": " + Utilities.computefinger(nodeidentifier, i));
        }
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

