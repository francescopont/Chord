package chord.Exceptions;

public class NotInitializedException extends Exception {
    // this exception is used when you are not able to join a Chord
    public NotInitializedException(String msg){
        super(msg);
    }
}
