package chord.model;

// this class contains all the operations we need to run periodically on each node, and the code for the hash function




import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.TimerTask;
public class Utilities extends TimerTask {
    private final Node virtualnode;

    //constructor
    public Utilities(Node node){
        this.virtualnode = node;
    }

    //calls periodic functions on the nodes
    @Override
    public void run() {
        synchronized (virtualnode) {
            if (virtualnode.isInitialized() && !virtualnode.isTerminated()) {
                virtualnode.stabilize();
                virtualnode.fix_finger();
                virtualnode.check_predecessor();
                virtualnode.fix_successor_list();
            }
        }
    }

    //code modified for testing
    //we use just the first two byte to test the correct behaviour of the function
    //returns a string representation of the hash of the string passed as param
    //in case of nodes, param = concat of ip and port
    public static String hashfunction(String key){
        MessageDigest digest;
        byte[] hash;
        StringBuffer hexHash = new StringBuffer();
        try {
            // Create the SHA-1 of the nodeidentifier
            digest = MessageDigest.getInstance("SHA-1");
            hash = digest.digest(key.getBytes(StandardCharsets.UTF_8));

            // Convert hash bytes into StringBuffer ( via integer representation)
            for (int i = 0; i < numberOfBit()/8; i++) {
                String hex = Integer.toHexString(0xff &  hash[i]);
                if (hex.length() == 1) hexHash.append('0');
                hexHash.append(hex);
            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return hexHash.toString();
    }

    //SI CONTA DA 0 A 15
    //to compute the i-th finger in the current Chord, given the node identifier
    //uso sempre l'intero, non ritorno mai ai bytes, per evitare la gestione dei numeri negativi in complemento a due
    public static String computefinger(String nodeidentifier, int finger){

        //reconverting the  string nodeidentifier in the  integer representation
        int[] hash = new int[numberOfBit()/8];
        int j=0;
        for (int i =0; i< (numberOfBit()/8) *2; i= i+2){
            //every byte corresponds to two chars in the String representation
            String number = "" + nodeidentifier.charAt(i) + nodeidentifier.charAt(i+1);
            hash[j] = Integer.parseInt(number,16);
            j++;

        }

        //counter starts from 0
        finger = numberOfBit()-finger;

        //changing the byte
        //the function recursion basically executes the sum, handlind the eventual carry over
        recursion(hash, (int) Math.pow(2,(numberOfBit()-finger)%8), (finger-1)/8);

        //reconverting the hash in the String representation to return
        StringBuffer hexHash = new StringBuffer();
        for (int h = 0; h < numberOfBit()/8; h++) {
            String hex = Integer.toHexString(hash[h]);
            if (hex.length() == 1) hexHash.append('0');
            hexHash.append(hex);
        }
        return hexHash.toString();
    }

    private static void recursion (int hash[], int tosum, int i){
        if (hash[i] + tosum <= 255){
            hash[i] = hash[i] + tosum;
        }
        else{
            //in case we reach the maximum value in that byte
            hash[i] =  (hash[i] + tosum -256);
            if(i >0){
                recursion(hash,1, i-1);
            }
            //se siamo al bit più grande è giusto che il riporto si perda
        }
    }


    public static int numberOfBit(){
        //this is used to set the number of bits we want to use from the algorithm sha1, which uses 160 bits
        return 16;
    }

    //period of utilities
    public static int getPeriod(){
        return 1000;
    }

}
