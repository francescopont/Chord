package chord.network;

public class MessageHandler implements Runnable {
    private Message message;

    public MessageHandler (Message message){
        this.message=message;
    }

    @Override
    public void run() {
        switch (message.getType()){

        }
    }
}
