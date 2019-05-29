package chord.model;

import com.google.gson.Gson;

import java.util.HashMap;

public class FileSystem {
    Gson gson;
    HashMap<String , String> files;

    public FileSystem(){
        this.files=new HashMap<>();
    }

    public void publish (String file, String key){
        files.put(key, file);
    }

    public String getFile(String key){
        return files.get(key);
    }
}
