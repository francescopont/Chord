package chord.model;

import chord.Exceptions.*;

import java.util.concurrent.ScheduledFuture;

public class Node {
    private NodeInfo nodeInfo;
    private String nodeidentifier;
    private FingerTable fingerTable;
    private SuccessorList successorList;
    private NodeInfo predecessor;
    private boolean alone;
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
                repopulateSuccessorList(i);
                //put code here
            }

        }


    public void checkPredecessor() {
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
            try{
                this.dispatcher.sendSuccessorRequest(this.successorList.getFirstElement(), key, this.nodeInfo);
            }catch (TimerExpiredException e){
                repopulateSuccessorList(0);
            }
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
    public  void notify(NodeInfo potential_predecessor) {
        if(potential_predecessor.equals(this.nodeInfo)){
            System.out.println("sto notificando me stesso e sono: "+this.nodeidentifier);
            return;
        }
        if (this.predecessor == null) {
            this.predecessor = potential_predecessor;
        } else {
            //ho le due chiavi
            String predecessorKey = this.predecessor.getHash();
            String potentialKey = potential_predecessor.getHash();
            //se la chiave del potenziale predecessore è più piccola del successore e più grande del nodo, allora ho trovato un nuovo predecessore
            if(comparator.compare(predecessorKey,potentialKey)<0){
                this.predecessor=potential_predecessor;
            }
        }
    }


    public  void terminate() {
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
    public  void notifyLeavingPredecessor(NodeInfo newPredecessor){
        System.out.println("notify leaving predecessor on "+ this.nodeidentifier);
        if (newPredecessor != null){
            //qua dovremo mettere il codice di passaggio dei files
            this.predecessor = newPredecessor;
        }
    }

    //when my successor leaves
    public  void notifyLeavingSuccessor (NodeInfo newSuccessor){
        System.out.println("notify leaving successor on "+ this.nodeidentifier);
        this.successorList.modifyEntry(0, newSuccessor);
        this.fingerTable.modifyFinger(0, newSuccessor);
        fixSuccessorList();
    }


    public  void repopulateSuccessorList(int positionOFUnvalidNode){
        int positionOFValidSuccessorNode = positionOFUnvalidNode +1;
        if (positionOFUnvalidNode == 0){
            boolean got = false;
            while (!got && positionOFValidSuccessorNode<4){
                try{
                    this.dispatcher.sendLeavingPredecessorRequest(this.successorList.getElement(positionOFValidSuccessorNode), this.nodeInfo, this.nodeInfo);
                    got = true;
                }catch (TimerExpiredException e){
                    positionOFValidSuccessorNode++;//check the next one
                }
            }
        }
        else{
            boolean got = false;
            while (!got && positionOFValidSuccessorNode<4){
                try{

                    NodeInfo nodeInfo33 = this.successorList.getElement(positionOFValidSuccessorNode);
                    NodeInfo nodeInfo1 = this.successorList.getElement(positionOFUnvalidNode-1);
                    this.dispatcher.sendLeavingPredecessorRequest(nodeInfo33,nodeInfo1 , this.nodeInfo);
                    got = true;
                }catch (TimerExpiredException e){
                    positionOFValidSuccessorNode++;//check the next one
                }
            }
            int positionOFValidPredecessorNode = positionOFUnvalidNode -1;
            if (positionOFValidSuccessorNode == 4){
                positionOFValidSuccessorNode--;
            }
            got = false;
            while (!got && positionOFValidPredecessorNode >= 0){
                try{
                    System.out.println(this.successorList.getElement(positionOFValidPredecessorNode).getHash());
                    System.out.println(this.successorList.getElement(positionOFValidSuccessorNode).getHash());
                    this.dispatcher.sendLeavingSuccessorRequest(this.successorList.getElement(positionOFValidPredecessorNode), this.successorList.getElement(positionOFValidSuccessorNode),this.nodeInfo);
                    got = true;

                }catch (TimerExpiredException e){
                    positionOFValidPredecessorNode--; //check the next one
                }
            }

        }

        System.out.println("sto modificando la finger "+ positionOFUnvalidNode + " con il nodo "+successorList.getElement(positionOFValidSuccessorNode).getHash() + " " + positionOFValidSuccessorNode + " e sono "+ this.nodeidentifier );
        this.successorList.modifyEntry(positionOFUnvalidNode,this.successorList.getElement(positionOFValidSuccessorNode));
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
        try {
            System.out.println("Sono : "+ this.nodeidentifier+ "ho il file: " + fileSystem.getFile(key));
        } catch (FileSystemException e) {
            e.printStackTrace();
        }

    }

    //cerco il responsabile e gli chiedo i dati
    public String getFile(String key){
        String file=null;
        NodeInfo successor= findSuccessor(key);
        if(successor.getHash().equals(this.nodeidentifier)){
            file=getMyFile(key);
        }
        else {
            try {
                file = this.dispatcher.sendFileRequest(successor, key, this.nodeInfo);
            } catch (TimerExpiredException e) {
                e.printStackTrace();
            }
        }
        return file;
    }

    //chiedo i dati al mio filesystem
    public String getMyFile(String key)  {
        String file=null;
        try {
            file= this.fileSystem.getFile(key);
        } catch (FileSystemException e) {
            System.out.println("Nessun nodo possiede questo file");
        }
        return file;
    }

    public void deleteFile(String key){
        NodeInfo successor= findSuccessor(key);
        if(successor.getHash().equals(this.nodeidentifier)){
            deleteMyFile(key);
        }
        else{
            try {
                this.dispatcher.sendDeeleteFileRequest(successor, key, this.nodeInfo);
            }
            catch (TimerExpiredException e){
                //minchia boh
            }
        }
    }

    public void deleteMyFile(String key){
        fileSystem.deleteFile(key);
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

