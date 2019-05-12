package chord.model;

public class Finger {
    private  String hash;
    //position goes from 0 to 15
    private  int position;

    public Finger(String hash){
        this.position = -1;
        this.hash = hash;
    }

    public int getPosition(){
        return this.position;
    }

    public String getHash(){
        return this.hash;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    //useful for testing
    public Finger(String hash, int position){
        this.position = position;
        this.hash = hash;
    }

    @Override
    public boolean equals(Object o){
        Finger finger = (Finger) o;
        return this.hash.equals(finger.hash)  && position == finger.position;
    }
}
