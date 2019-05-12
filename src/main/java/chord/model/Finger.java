package chord.model;

public class Finger {
    private  String hash;
    //numberofFinger goes from 0 to 15
    private  int numberofFinger;

    public Finger(String hash){
        this.numberofFinger = -1;
        this.hash = hash;
    }

    public int getNumberofFinger(){
        return this.numberofFinger;
    }

    public String getHash(){
        return this.hash;
    }

    public void setNumberofFinger(int numberofFinger) {
        this.numberofFinger = numberofFinger;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    //useful for testing
    public Finger(String hash, int position){
        this.numberofFinger = position;
        this.hash = hash;
    }

    @Override
    public boolean equals(Object o){
        Finger finger = (Finger) o;
        return this.hash.equals(finger.hash)  && numberofFinger == finger.numberofFinger;
    }
}
