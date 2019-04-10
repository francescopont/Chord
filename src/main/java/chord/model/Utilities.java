package chord.model;

// this class contains all the operations we need to run periodically on each node, and the code for the hash function

// la COMPUTE FINGER è SBAGLIATA. E VA CORRETTA
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.TimerTask;
class Utilities extends TimerTask {
    private List<Node> virtualnodes;
    public static int counter=0;


    public Utilities(List<Node> nodes){
        //counter è usato dalla fix finger
        this.virtualnodes = nodes;
        counter++;
        if (counter > 16){
            counter=0;
        }
    }

    //calls periodic functions on the nodes
    @Override
    public void run() {
        synchronized (virtualnodes){
            for ( Node node: virtualnodes){
                //this code might be exposed to frequent changes: it's useful to separate it from the Chord class
                node.stabilize();
                node.fix_finger(counter);
                node.check_predecessor();
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

            // Convert hash bytes into StringBuffer
            for (int i = 0; i < 2; i++) {
                String hex = Integer.toHexString(0xff &  hash[i]);
                if (hex.length() == 1) hexHash.append('0');
                hexHash.append(hex);
            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return hexHash.toString();
    }

    //to compute the i-th finger in the current Chord, given the node identifier
    //conviene riconvertire la stringa in byte piuttosto che ricalcolare l'hash, che è un'operazione onerosa
    public static String computefinger(String nodeidentifier, int finger){

        //reconverting the  string nodeidentifier in the  byte representation
        byte[] hash = new byte[20];
        int j=0;
        for (int i =0; i< nodeidentifier.length() -1; i= i+2){
            //every byte corresponds to two chars in the String representation
            String number = "" + nodeidentifier.charAt(i) + nodeidentifier.charAt(i+1);
            try{

                hash[j] = Byte.decode("0x"+number);
            }catch(NumberFormatException e){
                // in case of negative numbers
                hash[j] = (byte) (-128 +Integer.parseInt(number,16) -128);
            }
            j++;
        }


        //calculating the byte representation of the new finger
        finger = 17 - finger;
        //now finger indicates the bit I need to change
        int i=0;
        //finding the right byte to change
        while (finger > 8){
            finger = finger - 8;
            i++;
        }


        //changing the byte
        //the function recursion basically executes the sum, handlind the eventual carry over
        recursion(hash, (int) Math.pow(2,8-finger), i );


        //reconverting the hash in the String representation to return
        StringBuffer hexHash = new StringBuffer();
        for (int h = 0; h < hash.length; h++) {
            String hex = Integer.toHexString(0xff & hash[h]);
            if (hex.length() == 1) hexHash.append('0');
            hexHash.append(hex);
        }
        return hexHash.toString();
    }

    private static void recursion (byte hash[], int tosum, int i){
        if (hash[i] + tosum <= 127){
            hash[i] = (byte)(hash[i] + tosum);
        }
        else{
            //in case we reach the maximum value in that byte
            hash[i] = (byte) (hash[i] + tosum -128-128);
            if(i >0){
                recursion(hash,1, i-1);
            }
        }
    }

    //REAL CODE
    /*
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

            // Convert hash bytes into StringBuffer
            for (int i = 0; i < hash.length; i++) {
                String hex = Integer.toHexString(0xff & hash[i]);
                if (hex.length() == 1) hexHash.append('0');
                hexHash.append(hex);
            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return hexHash.toString();
    }

    //to compute the i-th finger in the current Chord, given the node identifier
    //conviene riconvertire la stringa in byte piuttosto che ricalcolare l'hash, che è un'operazione onerosa
    public static String computefinger(String nodeidentifier, int finger){

        //reconverting the  string nodeidentifier in the  byte representation
        byte[] hash = new byte[20];
        int j=0;
        for (int i =0; i< nodeidentifier.length() -1; i= i+2){
            //every byte corresponds to two chars in the String representation
            String number = "" + nodeidentifier.charAt(i) + nodeidentifier.charAt(i+1);
            try{

                hash[j] = Byte.decode("0x"+number);
            }catch(NumberFormatException e){
                // in case of negative numbers
                hash[j] = (byte) (-128 +Integer.parseInt(number,16) -128);
            }
            j++;
        }


        //calculating the byte representation of the new finger
        finger = 161 - finger;
        //now finger indicates the bit I need to change
        int i=0;
        //finding the right byte to change
        while (finger > 8){
            finger = finger - 8;
            i++;
        }


        //changing the byte
        //the function recursion basically executes the sum, handlind the eventual carry over
        recursion(hash, (int) Math.pow(2,8-finger), i );


        //reconverting the hash in the String representation to return
        StringBuffer hexHash = new StringBuffer();
        for (int h = 0; h < hash.length; h++) {
            String hex = Integer.toHexString(0xff & hash[h]);
            if (hex.length() == 1) hexHash.append('0');
            hexHash.append(hex);
        }
        return hexHash.toString();
    }

    private static void recursion (byte hash[], int tosum, int i){
        if (hash[i] + tosum <= 127){
            hash[i] = (byte)(hash[i] + tosum);
        }
        else{
            //in case we reach the maximum value in that byte
            hash[i] = (byte) (hash[i] + tosum -128-128);
            if(i >0){
                recursion(hash,1, i-1);
            }


        }
    }

    */

}
