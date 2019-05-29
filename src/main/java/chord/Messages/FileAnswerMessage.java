package chord.Messages;

import chord.model.NodeInfo;

public class FileAnswerMessage extends Message {
    private final String file;

    public FileAnswerMessage(NodeInfo destination, String file, NodeInfo sender, int ticket){
        super(6, false, destination, sender);
        this.file=file;
        this.id=ticket;
    }

    public String getFile() {
        return file;
    }
}
