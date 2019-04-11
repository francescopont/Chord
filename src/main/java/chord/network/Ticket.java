package chord.network;

public class Ticket {
    private static int ticket=0;

    public synchronized static int getTicket(){
        //TO DO
        // add code to avoid overflow ( basically, restart ticket after a certain value)
        ticket++;
        return ticket;
    }

}
