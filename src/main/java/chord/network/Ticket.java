package chord.network;

/**
 * Class which return an unique identifier for every message
 */
public class Ticket {
    private static int ticket=0;

    public static synchronized  int getTicket(){
        if(ticket == Integer.MAX_VALUE){
            ticket=0;
        }
        ticket++;
        return ticket;
    }
}
