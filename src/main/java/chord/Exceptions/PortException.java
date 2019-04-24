package chord.Exceptions;

//se lancio quest'eccezione vuol dire che i socket hanno scelto una porta loro al posto mio
//tramite quest'eccezione i socket mi comunicano quale porta abbiano scelto
public class PortException extends Exception {
    private final int port;
    public PortException(int port){
        this.port = port;
    }

    public int getPort(){
        return this.port;
    }

}
