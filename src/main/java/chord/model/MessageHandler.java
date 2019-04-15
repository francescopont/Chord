package chord.model;

import chord.Messages.Message;

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


//please put protocol of types here!!!
/*
    ping 1
    request of predecessor 2
    find successor 3
    notify 4
     initial request of successor 6

    ack 0
    reply 6
 */

