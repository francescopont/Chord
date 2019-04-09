package chord.model;

//è davvero molto utile: la usiamo ovunque, la consiglio
public class NodeInfo {

    private String IPAddress;
    private int port;

    //only other classes from this library can create instances
    protected NodeInfo(String IPAddress, int port){
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


}
