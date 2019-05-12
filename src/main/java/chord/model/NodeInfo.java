package chord.model;

import java.io.Serializable;

//è davvero molto utile: la usiamo ovunque, la consiglio
public class NodeInfo implements Serializable{

    private String IPAddress;
    private int port;
    private String hash;

    //only other classes from this library can create instances
    public NodeInfo(String IPAddress, int port){
        this.IPAddress = IPAddress;
        this.port = port;
        String key = IPAddress.concat(Integer.toString(port));
        this.hash = Utilities.hashfunction(key);
    }

    //getters and setters
    public String getIPAddress() {
        return IPAddress;
    }

    public void setIPAddress(String IPAddress) {
        this.IPAddress = IPAddress;
    }

    public void setPort(int port) {
        this.port = port;
        // I need to update the hash function
        String key = IPAddress.concat(Integer.toString(port));
        this.hash = Utilities.hashfunction(key);
    }

    public int getPort() {
        return port;
    }
    public String getHash() {
        return hash;
    }



    //useful for testing
    @Override
    public boolean equals(Object o){
        NodeInfo nodeInfo = (NodeInfo) o;
        return hash.compareTo(nodeInfo.hash) == 0;
    }

    //fake constructor used only for testing
    public NodeInfo (String hash){
        this.hash = hash;
        this.IPAddress = "-.-.-.-";
        this.port = -1;
    }


}
