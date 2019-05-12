package chord.model;

public class Finger {
    private  String hash;
    //numberofFinger goes from 0 to 15
    private  int numberofFinger;
    public Finger(int numberofFinger, String hash){
        this.numberofFinger = numberofFinger;
        this.hash = hash;
    }

    public int getNumberofFinger(){
        return this.numberofFinger;
    }

    public String getHash(){
        return this.hash;
    }

    @Override
    public boolean equals(Object o){
        Finger finger = (Finger) o;
        return this.hash.equals(finger.hash)  && numberofFinger == finger.numberofFinger;
    }
}
