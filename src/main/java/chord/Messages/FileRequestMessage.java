package chord.Messages;

import chord.model.NodeInfo;

public class FileRequestMessage extends Message {

    private final String key;

    public FileRequestMessage(NodeInfo destination, String key, NodeInfo sender){
        super(25, true, destination,sender);
        this.key=key;
    }

    public String getKey() {
        return key;
    }
}
