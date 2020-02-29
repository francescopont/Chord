package chord.model;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Class which contains all the operations that must be run periodically on each node, and the code for computing the hash function
 */
public class Utilities implements Runnable {
    /**
     * Node on which the operations are performed
     */
    private final Node virtualnode;


    public Utilities(Node node){
        this.virtualnode = node;
    }

    /**
     * Calls periodic functions on the nodes
     */
    @Override
    public void run() {
        if ( !virtualnode.isAlone()) {
            virtualnode.stabilize();
            virtualnode.fixFinger();
            virtualnode.checkPredecessor();
            virtualnode.fixSuccessorList();
        }
    }



    /**
     * Calculate the hashed key of a string. In case of node the string is obtained chaining Ip address and port of the node (concat ip and port)
     * @param key string to calculate the hash
     * @return a string representation of the hash of the string passed as param
     */
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

    /**
     * Compute the i-th finger in the current Chord, given the node identifier ( which is basically the hash calculated in the above function).
     * i from 0 to 15.
     * Always use the integer, never return to the bytes, to avoid the management of negative numbers in two's complement
     * @param nodeidentifier of the node
     * @param finger position of the finger to calculate
     * @return
     */
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

    /**
     *
     * @param hash an array of Integer in the interval (0,256); each integer corresponds to a byte in the hash, ad to two chars in the key
     * @param tosum the integer value to sum
     * @param i the index in the hash to handle the carry over
     */
    private static void recursion (int hash[], int tosum, int i){
        if (hash[i] + tosum <= 255){
            hash[i] = hash[i] + tosum;
        }
        else{
            //in case is reached the maximum value in that byte
            hash[i] =  (hash[i] + tosum -256);
            if(i >0){
                recursion(hash,1, i-1);
            }
            //if it is the largest byte it is right that the carry-over is lost
        }
    }

    /**
     * Set the number of bits to use from the algorithm sha1, which uses 160 bits
     * @return number of bit to use for the algorithm
     */
    public static int numberOfBit(){
        return 16;
    }

    /**
     * Get the period of utilities functions
     * @return the period for executing utilies functions
     */
    public static int getPeriod(){
        //0.5 seconds
        return 500;
    }

    /**
     * Get the timer for the message
     * @return the time within which a node must receive a reply to a message
     */
    public static int getTimer(){
        // 1 second
        return 1000;
    }




}
