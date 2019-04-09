package chord.network;

public class Ticket {
    private static int ticket=0;

    public synchronized static int getTicket(){
        ticket++;
        return ticket;
    }

}
