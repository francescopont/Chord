package chord.model;

import chord.Exceptions.NotInitializedException;
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
    private boolean alone;
    private boolean terminated;
    private NodeDispatcher dispatcher;
    private FileSystem fileSystem;
    private int fixFingerCounter;
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
        this.terminated = false;
        this.dispatcher = new NodeDispatcher(this.getPort());
        this.fixFingerCounter = 0;
        this.comparator=new NodeComparator(me.getHash());
        this.alone = true;
        this.fileSystem = new FileSystem();
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
        NodeInfo successor = null;
        try {
            successor = this.successorList.getFirstElement();
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
            System.out.println("timer expired exception on the stabilize");
            repopulateSuccessorList(0);
            //put code here
        }catch (PredecessorException e){
            System.out.println("My successor said me he does not have a predecessor: " + this.nodeidentifier);
        }
        NodeInfo newSuccessor = null;
        try {
            newSuccessor = this.successorList.getFirstElement();
            this.dispatcher.sendNotify(newSuccessor, this.nodeInfo);
        } catch (TimerExpiredException e) {
            repopulateSuccessorList(0);
        }

    }

    public void fixFinger() {
        String hashedkey = Utilities.computefinger(this.nodeidentifier, fixFingerCounter);
        NodeInfo successor = findSuccessor(hashedkey);
        this.fingerTable.modifyFinger(fixFingerCounter, successor);
        fixFingerCounter++;
        if (fixFingerCounter == Utilities.numberOfBit()){
            fixFingerCounter = 0;
        }
    }

    public void fixSuccessorList(){
        int i=0;
        NodeInfo lastKnown = null;
        try {
            for (i = 0; i<3; i++) {
                lastKnown = successorList.getElement(i);
                NodeInfo successor = dispatcher.sendFirstSuccessorRequest(lastKnown, nodeInfo);
                if (successor.getHash().equals(nodeidentifier)) {
                    while (i < 3) {
                        successorList.modifyEntry(i + 1, this.nodeInfo);
                        i++;
                    }
                } else {
                    successorList.modifyEntry(i + 1, successor);
                }
            }
        } catch (TimerExpiredException e) {
                System.out.println("I'm " + this.nodeidentifier + " and I experienced an exception while fixing the successor list, getting the element "+ i);
                repopulateSuccessorList(i);
                //put code here
            }

        }


    public void checkPredecessor() {
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
    public NodeInfo findSuccessor(String key){
        NodeInfo successor=null;
        if(key.equals(this.nodeidentifier)){
            return this.nodeInfo;
        }
        //am I responsible for that key? If yes return myself
        FingerTableComparator comparator= new FingerTableComparator(this.nodeidentifier);
        if(predecessor!=null){
            String predecessorKey= predecessor.getHash();
            if( comparator.compare(predecessorKey,key)<0){
                return this.nodeInfo;
            }
        }
        //Is anyone from the successor list responsable for that key?
        try {
            successor = successorList.closestSuccessor(key);
            return successor;
        } catch (SuccessorListException e) {
            //no one in the successor list is responsible for that key
        }

        //look in the finger table
        NodeInfo closestPredecessor = null;

        try {
            closestPredecessor = fingerTable.closestPredecessor(key);
            successor = this.dispatcher.sendSuccessorRequest(closestPredecessor,key,this.nodeInfo);
        } catch (TimerExpiredException ex) {
            System.out.println("sto entrando nel codice nuovo");
            boolean found = false;
            while (!found){
                NodeInfo theprevious = fingerTable.closestPredecessor(closestPredecessor.getHash());
                if (theprevious == null){
                    found = true;
                }
                try{
                    successor = dispatcher.sendSuccessorRequest(theprevious, key, this.nodeInfo);
                    found = true;
                }catch (TimerExpiredException e){
                    closestPredecessor = theprevious;
                }

            }

            //put code here
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
        this.terminate  = Threads.executePeriodically(new Utilities(this));
        this.printStatus();
        this.printUtilities();

    }

    //this method is called when you create a Chord and discover you're not alone
    public synchronized void start(NodeInfo nodeInfo){
        if (alone){
            this.successorList.modifyEntry(0, nodeInfo);
            this.fingerTable.modifyFinger(0, nodeInfo);
            setAlone(false);
        }
    }

    public synchronized void initialize(final NodeInfo myfriend) throws NotInitializedException {
        try {
            NodeInfo successor = this.dispatcher.sendSuccessorRequest(myfriend, this.nodeidentifier, this.nodeInfo);
            this.successorList.addEntry(successor);
            this.fingerTable.addFinger( successor);
            this.predecessor=null;
        } catch (TimerExpiredException e) {
            throw new NotInitializedException("impossible to initialize this node");
        }

        //first, the successor list
        for (int i = 1; i < 4; i++) {
            NodeInfo lastElement = successorList.getLastElement();
            NodeInfo successor = null;
            try {
                successor = dispatcher.sendFirstSuccessorRequest(lastElement,nodeInfo);
                if (successor.getHash().equals(nodeidentifier)) {
                    while (i < 4) {
                        successorList.addEntry( nodeInfo);
                        i++;
                    }
                } else {
                    successorList.addEntry(successor);
                }
            } catch (TimerExpiredException e) {
                this.successorList.removeLast();
                i--;
            }

        }

        //secondly, the fingerTable
        for(int i=1; i<Utilities.numberOfBit(); i++) {
            String hashedkey = Utilities.computefinger(nodeidentifier, i);
            NodeInfo finger = null;
            NodeInfo successor = successorList.getFirstElement();
            try {
                finger = dispatcher.sendSuccessorRequest(successor, hashedkey, nodeInfo);
                fingerTable.addFinger(finger);
            } catch (TimerExpiredException e) {
                repopulateSuccessorList(0);
            }
        }
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
            //se la chiave del potenziale predecessore è più piccola del successore e più grande del nodo, allora ho trovato un nuovo predecessore
            if(comparator.compare(predecessor_key,potential_key)<0){
                this.predecessor=potential_predecessor;
            }
        }
    }


    public void terminate() {
        terminate.cancel(true);

        /*try{
            // I send a message to my successor
            this.dispatcher.sendLeavingPredecessorRequest(this.successorList.getFirstElement(), this.predecessor, this.nodeInfo);
            if (this.predecessor != null){
                this.dispatcher.sendLeavingSuccessorRequest(this.predecessor, this.successorList.getFirstElement(), this.nodeInfo);
            }
        }catch (TimerExpiredException e){
            // do nothing
        }
        */
    }

    //when my predecessor leaves
    public void notifyLeavingPredecessor(NodeInfo newPredecessor){
        System.out.println("notify leaving predecessor on "+ this.nodeidentifier);
        if (newPredecessor != null){
            //qua dovremo mettere il codice di passaggio dei files
            this.predecessor = newPredecessor;
        }
    }

    //when my successor leaves
    public void notifyLeavingSuccessor (NodeInfo newSuccessor){
        System.out.println("notify leaving successor on "+ this.nodeidentifier);
        this.successorList.modifyEntry(0, newSuccessor);
        this.fingerTable.modifyFinger(0, newSuccessor);
        fixSuccessorList();
    }

    public boolean isTerminated() {
        return terminated;
    }




    public void repopulateSuccessorList(int positionOFUnvalidNode){
        if (positionOFUnvalidNode==0){
            try{
                this.dispatcher.sendLeavingPredecessorRequest(this.successorList.getElement(1), this.nodeInfo, this.nodeInfo);
            }catch (TimerExpiredException e1){
                //do something?
            }
        }else{
            try{
                this.dispatcher.sendLeavingPredecessorRequest(this.successorList.getElement(positionOFUnvalidNode +1), this.successorList.getElement(positionOFUnvalidNode-1), this.nodeInfo);
                this.dispatcher.sendLeavingSuccessorRequest(this.successorList.getElement(positionOFUnvalidNode -1), this.successorList.getElement(positionOFUnvalidNode+1),this.nodeInfo);
            }catch (TimerExpiredException e1){
                //do something?
            }
        }

        this.successorList.modifyEntry(positionOFUnvalidNode,this.successorList.getElement(positionOFUnvalidNode +1));

    }

    //cerco il responsabile e gli invio i dati
    public void publish(String data, String key){
        NodeInfo successor=findSuccessor(key);
        try {
            this.dispatcher.sendPublishRequest(successor, data, key, this.nodeInfo);
        }
        catch (TimerExpiredException e){
            //boh??
        }
    }

    //pubblisco i dati nel mio file system
    public void publishFile(String data, String key){
        this.fileSystem.publish(data, key);
    }

    //cerco il responsabile e gli chiedo i dati
    public String getFile(String key){
        String file=null;
        NodeInfo successor= findSuccessor(key);
        try {
            file= this.dispatcher.sendFileRequest(successor,key, this.nodeInfo);
        } catch (TimerExpiredException e) {
            e.printStackTrace();
        }
        return file;
    }

    //chiedo i dati al mio filesystem
    public String getMyFile(String key){
        return this.fileSystem.getFile(key);
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

