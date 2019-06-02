package chord.model;

import chord.Exceptions.NotInitializedException;
import chord.Exceptions.PredecessorException;
import chord.Exceptions.SuccessorListException;
import chord.Exceptions.TimerExpiredException;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;

/**
 * Class which represents a Node in the Chord protocol
 */
public class Node {
    /**
     * For the description of each attribute refer to the specific class
     */
    private NodeInfo nodeInfo;
    private String nodeidentifier;
    private FingerTable fingerTable;
    private SuccessorList successorList;
    private NodeInfo predecessor;
    private NodeDispatcher dispatcher;
    private int fixFingerCounter;
    private FileSystem fileSystem;
    private NodeComparator comparator;
    private boolean started;
    private ScheduledFuture UtilitiesThread;

    public Node(NodeInfo me) {
        this.nodeInfo = me;
        this.nodeidentifier = me.getHash();
        this.fingerTable = new FingerTable(me.getHash());
        this.successorList = new SuccessorList(me.getHash());
        this.predecessor = null;
        this.dispatcher = new NodeDispatcher(this.getPort());
        this.fixFingerCounter = 0;
        this.fileSystem = new FileSystem(me.getHash());
        this.comparator=new NodeComparator(me.getHash());
        this.started = false;
    }

    /**
     * Change the port when the chosen one is not available
     * @param port the new port of this node
     */
    public void modifyPort(int port) {
        this.nodeInfo.setPort(port);
        NodeInfo me = this.nodeInfo;
        String key = me.getIPAddress().concat(Integer.toString(me.getPort()));
        this.nodeidentifier = Utilities.hashfunction(key);
    }

    /**
     * Getter and setter methods
     */

    public int getPort() {
        return this.nodeInfo.getPort();
    }
    public NodeInfo getPredecessor()throws PredecessorException {
        if (this.predecessor == null){
            throw new PredecessorException();
        }
        return predecessor;
    }

    /**
     * @return the first element of the successor list
     */
    public NodeInfo getFirstSuccessor(){
        return successorList.getFirstElement();
    }
    public boolean isAlone() {
        return this.successorList.isAlone();
    }
    public void setAlone(boolean alone) {
        this.successorList.setAlone(alone);
    }
    public boolean isStarted() {
        return started;
    }
    public void setStarted(boolean started) {
        this.started = started;
    }
    /**
     * Called only from MessageHandler Class
     */
    public NodeDispatcher getDispatcher() {
        return dispatcher;
    }
    public FileSystem getFileSystem(){ return  fileSystem;}


    /**
     * Method called when a new Chord is created, it initializes the finger table and the successor list and starts executing periodic operations
     */
    public void initialize() {
        for (int i = 0; i < Utilities.numberOfBit(); i++) {
            fingerTable.addFinger( this.nodeInfo);
        }
        for (int i = 0; i < 4; i++) {
            successorList.addEntry(this.nodeInfo);
        }
        this.predecessor = this.nodeInfo;
        this.UtilitiesThread = Threads.executePeriodically(new Utilities(this));
    }

    /**
     * Method called when a node joins an existing Chord, it initializes the finger table and the successor list and starts executing periodic operations
     * @param myfriend information of the known node of Chord
     * @throws NotInitializedException Exception thrown if the known Node does not exist
     */
    public  void initialize(final NodeInfo myfriend) throws NotInitializedException {
        try {
            NodeInfo successor = this.dispatcher.sendSuccessorRequest(myfriend, this.nodeidentifier, this.nodeInfo);
            this.successorList.addEntry(successor);
            this.fingerTable.addFinger( successor);
            this.predecessor=null;
        } catch (TimerExpiredException e) {
            throw new NotInitializedException("impossible to initialize this node");
        }

        //first, the successor list
        int i=1;
        try{
            for (i = 1; i < 4; i++) {
                NodeInfo lastElement = successorList.getLastElement();
                NodeInfo successor = null;
                successor = dispatcher.sendFirstSuccessorRequest(lastElement,nodeInfo);
                if (successor.getHash().equals(nodeidentifier)) {
                    while (i < 4) {
                        successorList.addEntry( nodeInfo);
                        i++;
                    }
                } else {
                    successorList.addEntry(successor);
                }
            }
        }catch (TimerExpiredException e){
            while (i < 4) {
                successorList.addEntry( nodeInfo);
                i++;
            }
        }

        //secondly, the fingerTable
        for(int j=1; j<Utilities.numberOfBit(); j++) {
            String hashedkey = Utilities.computefinger(nodeidentifier, j);
            try {
                NodeInfo finger = dispatcher.sendSuccessorRequest(successorList.getFirstElement(), hashedkey, nodeInfo);
                fingerTable.addFinger(finger);
            } catch (TimerExpiredException e) {
                repopulateSuccessorList(0);
            }
        }
        try{
            dispatcher.sendStartRequest(myfriend, this.nodeInfo);
        }catch (TimerExpiredException e){
            throw new NotInitializedException("impossible to initialize this node");
        }
        this.UtilitiesThread = Threads.executePeriodically(new Utilities(this));
        setAlone(false);
        setStarted(true);
    }

    /**
     * For every message the method checks whether the node knows that there is another node in the Chord
     * @param nodeInfo information of the message sender
     */
    public  void start(NodeInfo nodeInfo){
        setStarted(true);
        if (isAlone() && ! (nodeInfo.equals(this.nodeInfo))){
            this.successorList.modifyEntry(0, nodeInfo);
            this.fingerTable.modifyFinger(0, nodeInfo);
            setAlone(false);
        }
    }

    /**
     * Periodic operations
     */

    /**
     * Verify the immediate successor of the node and notify the successor about itself
     */
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
                this.successorList.modifyEntry(0,potentialSuccessor);
            }
        } catch (TimerExpiredException e) {
            repopulateSuccessorList(0);
        }catch (PredecessorException e){}
        try {
            NodeInfo newSuccessor = this.successorList.getFirstElement();
            Map<String,String> newFiles = this.dispatcher.sendNotify(newSuccessor, this.nodeInfo);
            if (!newFiles.isEmpty()){
                for (Map.Entry<String, String> newFile: newFiles.entrySet() ){
                    this.fileSystem.publish(newFile.getKey(), newFile.getValue());
                }
            }
        } catch (TimerExpiredException e) {
            repopulateSuccessorList(0);
        }
    }

    /**
     * Check and update the node's predecessor
     * @param potentialPredecessor information about the new potential predecessor of the node
     */
    public  void notify(NodeInfo potentialPredecessor) {
        if(potentialPredecessor.equals(this.nodeInfo)){
            return;
        }
        if (this.predecessor == null) {
            this.predecessor = potentialPredecessor;
        } else {
            String predecessorKey = this.predecessor.getHash();
            String potentialKey = potentialPredecessor.getHash();
            if(comparator.compare(predecessorKey,potentialKey)<0){
                this.predecessor=potentialPredecessor;
            }
        }
    }

    /**
     * Refresh finger table entry at the position of fixFingerCounter
     */
    public void fixFinger() {
        String hashedkey = Utilities.computefinger(this.nodeidentifier, fixFingerCounter);
        NodeInfo successor = findSuccessor(hashedkey);
        this.fingerTable.modifyFinger(fixFingerCounter, successor);
        fixFingerCounter++;
        if (fixFingerCounter == Utilities.numberOfBit()){
            fixFingerCounter = 0;
        }
    }

    /**
     * Refresh the successor list
     */
    public void fixSuccessorList(){
        int i;
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
            //do nothing, we have to wait for periodic operations
        }
    }

    /**
     *Check if the predecessor is still on the network
     */
    public void checkPredecessor() {
        if (predecessor != null) {
            try {
                dispatcher.sendPing(this.predecessor, this.nodeInfo);
            } catch (TimerExpiredException e) {
                predecessor = null;
            }
        }
    }


    /**
     *End of periodic operations
     */


    /**
     * Find the responsible of the given key
     * @param key hashed key to find
     * @return the responsible of the key
     */
    public NodeInfo findSuccessor(String key){
        NodeInfo successor=null;
        if(key.equals(this.nodeidentifier)){
            return this.nodeInfo;
        }
        //check if the node is responsible for the key
        FingerTableComparator comparator= new FingerTableComparator(this.nodeidentifier);
        if(predecessor!=null){
            String predecessorKey= predecessor.getHash();
            if( comparator.compare(predecessorKey,key)<0){
                return this.nodeInfo;
            }
        }
        //look in the successor list
        try {
            successor = successorList.closestSuccessor(key);
            return successor;
        } catch (SuccessorListException e) {
            //no one in the successor list is responsible for that key
        }

        //look in the finger table
        try {
            NodeInfo closestPredecessor = fingerTable.closestPredecessor(key);
            successor = this.dispatcher.sendSuccessorRequest(closestPredecessor,key,this.nodeInfo);
        } catch (TimerExpiredException ex) {
            try{
                this.dispatcher.sendSuccessorRequest(this.successorList.getFirstElement(), key, this.nodeInfo);
            }catch (TimerExpiredException e){
                repopulateSuccessorList(0);
            }
        }
        return successor;
    }

    /**
     * Terminate this node
     */
    public  void terminate() {
        UtilitiesThread.cancel(true);
        try{
            // I send a message to my successor
            this.dispatcher.sendLeavingPredecessorRequest(this.successorList.getFirstElement(), this.predecessor, this.fileSystem.freeFileSystem(),  this.nodeInfo);
            // I send a message to my predecessor
            if (this.predecessor != null){
                this.dispatcher.sendLeavingSuccessorRequest(this.predecessor, this.successorList.getFirstElement(), this.nodeInfo);
            }
        }catch (TimerExpiredException e){
            // do nothing
        }
    }

    /**
     * Notify that the predecessor is leaving
     * @param newPredecessor of this node
     */
    public  void notifyLeavingPredecessor(NodeInfo newPredecessor){
        if (newPredecessor != null){
            this.predecessor = newPredecessor;
        }
    }

    /**
     * Notify that the successor is leaving
     * @param newSuccessor of this node
     */
    public  void notifyLeavingSuccessor (NodeInfo newSuccessor){
        this.successorList.modifyEntry(0, newSuccessor);
        this.fingerTable.modifyFinger(0, newSuccessor);
        fixSuccessorList();
    }

    /**
     * Repopulate the successor list when one the element does not respond and notify other nodes
     * @param positionOFUnvalidNode position in the successor list of the node that does not respond
     */
    public  void repopulateSuccessorList(int positionOFUnvalidNode){
        int positionOFValidSuccessorNode = positionOFUnvalidNode +1;
        if (positionOFUnvalidNode == 0){
            boolean got = false;
            while (!got && positionOFValidSuccessorNode<4){
                try{
                    this.dispatcher.sendLeavingPredecessorRequest(this.successorList.getElement(positionOFValidSuccessorNode), this.nodeInfo,new HashMap<>(), this.nodeInfo);
                    got = true;
                }catch (TimerExpiredException e){
                    positionOFValidSuccessorNode++;//check the next one
                }
            }
            if (positionOFValidSuccessorNode == 4){
                NodeInfo aNode = findRandomFinger(0);
                this.successorList.modifyEntry(0,aNode);
            }else{
                this.successorList.modifyEntry(0,this.successorList.getElement(positionOFValidSuccessorNode));
            }
        }
        else{
            boolean got = false;
            while (!got && positionOFValidSuccessorNode<4){
                try{
                    NodeInfo validSuccessor = this.successorList.getElement(positionOFValidSuccessorNode);
                    NodeInfo validPredecessor = this.successorList.getElement(positionOFUnvalidNode-1);
                    this.dispatcher.sendLeavingPredecessorRequest(validSuccessor,validPredecessor , new HashMap<>(),  this.nodeInfo);
                    got = true;
                }catch (TimerExpiredException e){
                    positionOFValidSuccessorNode++;//check the next one
                }
            }
            int positionOFValidPredecessorNode = positionOFUnvalidNode -1;
            if (positionOFValidSuccessorNode <4){
                got = false;
                while (!got && positionOFValidPredecessorNode >= 0){
                    try{
                        this.dispatcher.sendLeavingSuccessorRequest(this.successorList.getElement(positionOFValidPredecessorNode), this.successorList.getElement(positionOFValidSuccessorNode),this.nodeInfo);
                        got = true;

                    }catch (TimerExpiredException e){
                        positionOFValidPredecessorNode--; //check the next one
                    }
                }
                this.successorList.modifyEntry(positionOFUnvalidNode,this.successorList.getElement(positionOFValidSuccessorNode));
            }else{
                NodeInfo aNode = findRandomFinger(positionOFUnvalidNode);
                this.successorList.modifyEntry(positionOFUnvalidNode,aNode);
            }
        }
    }

    /**
     * Method called when no one in the successor list responds
     * @param position from which starts the research
     * @return a random finger, if no one responds return the node itself (it is the only one in Chord)
     */
    private NodeInfo findRandomFinger(int position){
        for (int i=position; i<Utilities.numberOfBit(); i++){
            NodeInfo nodeInfo = fingerTable.getFinger(i);
            try{
                this.dispatcher.sendPing(nodeInfo, this.nodeInfo);
                return nodeInfo;
            }catch (TimerExpiredException e){
                //try next
            }
        }
        return this.nodeInfo;
    }

    /**
     * Publish a file on the distributed filesystem: find the responsible of the file and send the file
     * @param key of the file
     * @param data file to publish
     */
    public void publish(String key, String data){
        NodeInfo successor=findSuccessor(key);
        try {
            this.dispatcher.sendPublishRequest(successor, data, key, this.nodeInfo);
        }
        catch (TimerExpiredException e){
            //do nothing
        }
    }

    /**
     * Publish the file in the node's filesystem (the node is the responsible of the file)
     * @param key of the file
     * @param data file to publish
     */
    public void publishFile(String key, String data){
        this.fileSystem.publish(key, data);
    }

    /**
     * Search the responsible of the file and ask the file
     * @param key of the file
     * @return the file if it exists , null otherwise
     */
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

    /**
     * Get the file from the node's filesystem (the node is responsible for that key)
     * @param key of the file
     * @return the file if it exists , null otherwise
     */
    public String getMyFile(String key)  {
        String file=null;
        file= this.fileSystem.getFile(key);
        return file;
    }

    /**
     * Search the responsible of the file and send a delete request
     * @param key of the file
     */
    public void deleteFile(String key){
        NodeInfo successor= findSuccessor(key);
        if(successor.getHash().equals(this.nodeidentifier)){
            deleteMyFile(key);
        }
        else{
            try {
                this.dispatcher.sendDeleteFileRequest(successor, key, this.nodeInfo);
            }
            catch (TimerExpiredException e){
            }
        }
    }

    /**
     * Delete the file from the filesystem (the node is responsible for that key)
     * @param key of the file
     */
    public void deleteMyFile(String key){
        fileSystem.deleteFile(key);
    }

    /**
     * Print the status of the node
     */
    public void printStatus() {
        System.out.println("-------------------");
        System.out.println("It's me:  " + this.nodeidentifier + "!");
        if (predecessor != null){
            System.out.println("Predecessor is : " + predecessor.getHash());
        }
        else {
            System.out.println("Predecessor is null");
        }
        successorList.printTable();
        fingerTable.printTable();
        fileSystem.print();
        System.out.println("-------------------");
    }

    /**
     * Methods useful only for testing
     */
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

    public void setFingerTable(FingerTable fingerTable) {
        this.fingerTable = fingerTable;
    }

    public void setSuccessorList(SuccessorList successorList) {
        this.successorList = successorList;
    }

    public void setDispatcher(NodeDispatcher nodeDispatcher){
        this.dispatcher=nodeDispatcher;
    }

}

