package chord.model;

import java.io.Serializable;

/**
 * Class which contains the information about a node : IP address, port number and hashed key
 */
public class NodeInfo implements Serializable{
    private String IPAddress;
    private int port;
    private String hash;

    public NodeInfo(String IPAddress, int port){
        this.IPAddress = IPAddress;
        this.port = port;
        String key = IPAddress.concat(Integer.toString(port));
        this.hash = Utilities.hashfunction(key);
    }

    /**
     *Getter and setter methods
     */

    public String getIPAddress() {
        return IPAddress;
    }


    public void setPort(int port) {
        this.port = port;
        String key = IPAddress.concat(Integer.toString(port));
        this.hash = Utilities.hashfunction(key);
    }

    public int getPort() {
        return port;
    }
    public String getHash() {
        return hash;
    }


    @Override
    public boolean equals(Object o){
        NodeInfo nodeInfo = (NodeInfo) o;
        return this.hash.equals(nodeInfo.hash);
    }

    /**
     * Constructor for testing
     */
    public NodeInfo (String hash){
        this.hash = hash;
        this.IPAddress = "-.-.-.-";
        this.port = -1;
    }


}
