package chord.Exceptions;

public class PortAlreadyChosenException extends Exception {
    private static String msg = "One node already existent with the same port";

    public  PortAlreadyChosenException(){
        super(msg);
    }
}
