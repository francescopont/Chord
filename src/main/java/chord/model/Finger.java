package chord.model;

public class Finger {
    private  String hash;
    //position goes from 0 to 15
    private  int position;

    private boolean initializing;

    public Finger(String hash){
        this.position = -1;
        this.hash = hash;
        this.initializing=false;
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

    public void setInitializing(boolean initializing) {this.initializing=initializing;}

    public boolean getInitializing(){ return this.initializing; }

    //useful for testing
    public Finger(String hash, int position){
        this.position = position;
        this.hash = hash;
        this.initializing=false;
    }

    @Override
    public boolean equals (Object o){
        Finger finger = (Finger) o;
        return  hash.equals(finger.hash);
    }


}
