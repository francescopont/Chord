package chord.model;

import chord.Exceptions.NotInitializedException;
import chord.Exceptions.PortException;
import chord.Messages.Message;
import chord.network.Router;
import com.google.gson.Gson;

import java.util.LinkedList;
import java.util.List;

/**
 * The main class of the library, which exposes the library's methods
 */
public class Chord{

    /**
     *List of active nodes in the same application
     */
    private static final  List<Node> virtualnodes = new LinkedList<>();

    private Chord(){};

    /**
     *Static methods to be used from the application layer
     */

    /**
     *Create a new Node and a new Chord
     * @param IPAddress of the new Node
     * @param port of the new Node
     * @throws PortException Exception thrown if the port is already in use
     */
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
            }finally {
                node.initialize();
            }
        }
    }

    /**
     *Create a new Node and join an existing Chord
     * @param IPAddress of the new Node
     * @param port of the new Node
     * @param knownIPAddress of an existing Node of Chord
     * @param knownPort of an existing Node of Chord
     * @throws PortException Exception thrown if the port is already in use
     * @throws NotInitializedException Exception thrown if the known Node does not exist
     */
    public static void join(String IPAddress, int port, String knownIPAddress, int knownPort) throws PortException, NotInitializedException {
        synchronized (virtualnodes){
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
                try {
                    node.initialize(knownnode);
                } catch (NotInitializedException e) {
                    virtualnodes.remove(node);
                    Router.terminate(node.getPort());
                    throw new NotInitializedException(e.getMessage());
                }
            }
        }
    }

    /**
     * Insert the file into the distributed hashtable
     * @param o Application specific object
     * @param port of the Node that calls the method
     * @return the key of the file in the Chord
     * @throws NotInitializedException Exception thrown if there is no node with such port number
     */
    public static String publish(Object o, int port) throws NotInitializedException {
        Gson gson=new Gson();
        String json=gson.toJson(o);
        String key= Utilities.hashfunction(json);
        Node me=null;
        for(Node node: virtualnodes){
            if(node.getPort()==port){
                me=node;
            }
        }
        if(me!=null) {
            me.publish(key, json);
        }
        else{
            throw new NotInitializedException("There is no node associated to this port, try with another port");
        }
        return key;
    }

    /**
     * Looks for the node responsible of the key
     * @param key of the file
     * @param port of the Node that calls the method
     * @return the file associated with the key or null if the file does not exist
     * @throws NotInitializedException Exception thrown if there is no node with such port number
     */
    public static String lookup(String key, int port) throws NotInitializedException {
        Node me=null;
        for(Node node: virtualnodes){
            if(node.getPort()==port){
                me=node;
            }
        }
        if(me!=null) {
            String file = me.getFile(key);
            return file;
        }
        else{
            throw new NotInitializedException("There is no node associated to this port, try with another port");
        }
    }

    /**
     * Delete the file associated with the key from the distributed filesystem
     * @param key of the file
     * @param port of the Node that calls the method
     * @throws NotInitializedException Exception thrown if there is no node with such port number
     */
    public static void deleteFile(String key, int port) throws NotInitializedException {
        Node me=null;
        for(Node node: virtualnodes){
            if(node.getPort()==port){
                me=node;
            }
        }
        if(me!=null){
            me.deleteFile(key);
        }
        else{
            throw new NotInitializedException("There is no node associated to this port, try with another port");
        }

    }

    /**
     * Delete the node associated with the given port from Chord
     * @param port of the Node to delete
     * @throws NotInitializedException Exception thrown if there is no node with such port number
     */
    public static void deleteNode(int port) throws NotInitializedException {
        Node node = null;
        for (Node virtualnode: virtualnodes){
            if (virtualnode.getPort() == port){
                node = virtualnode;
            }
        }
        if(node!=null){
            node.terminate();
            Router.terminate(port);
            virtualnodes.remove(node);
        }
        else {
            throw new NotInitializedException("There is no node associated to this port, try with another port");
        }

    }

    /**
     * Print the status of Chord
     */
    public static void printChord() {
        for (Node node : virtualnodes) {
            node.printStatus();
        }
        return;
    }


    /**
     * this is the only method which is not called from the application layer;
     * when a SocketHandler receives a new message, uses this method to deliver it to the corresponding node.
     * @param port is the port number of the message receiver
     * @param message is the message to deliver
     */
    public static void deliverMessage(int port, Message message){
        for (Node virtualnode: virtualnodes){
            if (virtualnode.getPort() == port){
                MessageHandler handler = new MessageHandler(virtualnode,message);
                Threads.executeImmediately(handler);
            }
        }
    }


}
