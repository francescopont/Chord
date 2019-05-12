package chord.network;

public class Ticket {
    private static int ticket=0;

    public synchronized static int getTicket(){
        if(ticket == Integer.MAX_VALUE){
            ticket=0;
        }
        ticket++;
        return ticket;
    }

}
