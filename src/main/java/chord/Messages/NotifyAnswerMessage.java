package chord.Messages;

import chord.model.NodeInfo;

import java.util.Map;

public class NotifyAnswerMessage extends Message {
    private final Map<String, String> files;

    public NotifyAnswerMessage(NodeInfo destination, NodeInfo sender, Map<String, String> files, int ticket) {
        super(6, false, destination, sender);
        this.files = files;
        this.id=ticket;
    }

    public Map<String, String> getFiles() {
        return files;
    }





}
