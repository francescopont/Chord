package chord.model;

import chord.network.Message;

public class MessageHandler implements Runnable {
    private Message message;
    private Node node;

    public MessageHandler (Node node, Message message){
        this.node = node;
        this.message=message;
    }

    @Override
    public void run() {
        switch (message.getType()){
            //put code here
        }
    }
}
