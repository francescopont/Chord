package chord.model;

public class Data extends Object {
    private String data;
    private String key;

    public Data(String data){
        this.data=data;
        this.key=Utilities.hashfunction(data);
    }

    public String getKey() {
        return key;
    }

    public String getData() {
        return data;
    }
}
