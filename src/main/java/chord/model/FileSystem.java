package chord.model;

import chord.Exceptions.FileSystemException;
import com.google.gson.Gson;

import java.util.HashMap;

public class FileSystem {
    Gson gson;
    HashMap<String , String> files;

    public FileSystem(){
        this.files=new HashMap<>();
    }

    //devo fare un controllo sulle chiavi??
    public void publish (String file, String key){
        files.put(key, file);
    }

    //da gestire eccezione del file che non c'è--> eccezione a parte?
    public String getFile(String key) throws FileSystemException {
        /*String file= files.get(key);
        if(file==null || files.size()==0){
            throw new FileSystemException();
        }
        else{
            return file;
        }*/
        return files.get(key);
    }

    //serve davvero implementarlo??
    public void deleteFile(String key){
        files.remove(key);
    }


}
