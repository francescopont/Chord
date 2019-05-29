package chord.Messages;

import chord.model.NodeInfo;

public class PublishRequestMessage extends Message {

    private final String data;
    private final String key;

    public PublishRequestMessage(NodeInfo destination, String data, String key, NodeInfo sender){
        super(85,true,destination,sender);
        this.data=data;
        this.key=key;
    }

    public String getData(){
        return this.data;
    }

    public String getKey() { return key;}
}
