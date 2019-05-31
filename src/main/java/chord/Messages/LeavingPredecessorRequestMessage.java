package chord.Messages;

import chord.model.NodeInfo;

import java.util.Map;

public class LeavingPredecessorRequestMessage extends Message {
    private final NodeInfo newPredecessor;
    private final Map<String, String> files;

    public LeavingPredecessorRequestMessage(NodeInfo destination, NodeInfo newPredecessor, Map<String, String> files, NodeInfo sender) {
        super(44, true, destination, sender);
        this.newPredecessor = newPredecessor;
        this.files = files;
    }

    public NodeInfo getNewPredecessor() {
        return newPredecessor;
    }

    public Map<String, String> getFiles() {
        return files;
    }


}
