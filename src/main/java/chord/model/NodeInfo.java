package chord.model;

import java.io.Serializable;

//è davvero molto utile: la usiamo ovunque, la consiglio
public class NodeInfo implements Serializable{

    private String IPAddress;
    private int port;

    //only other classes from this library can create instances
    public NodeInfo(String IPAddress, int port){
        this.IPAddress = IPAddress;
        this.port = port;
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
    }

    public int getPort() {
        return port;
    }

    //it may be useful
    //attenzione: non è l'override del metodo equals standard della classe object
    public boolean equals(NodeInfo nodeInfo){
        if (nodeInfo.IPAddress == this.IPAddress && nodeInfo.port == this.port){
            return true;
        }
        return  false;
    }
}
