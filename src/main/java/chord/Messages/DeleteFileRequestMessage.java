package chord.Messages;

import chord.model.NodeInfo;

public class DeleteFileRequestMessage extends Message {

    private final String key;

    public DeleteFileRequestMessage(NodeInfo destination, String key, NodeInfo sender){
        super(17, true, destination,sender);
        this.key=key;
    }

    public String getKey() {
        return key;
    }
}
