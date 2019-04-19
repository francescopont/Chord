package chord.Messages;

public class TimerExpiredException extends Exception {
private final int ticket;

    public TimerExpiredException(int ticket) {
        this.ticket = ticket;
    }

    public int getTicket() {
        return ticket;
    }
}
