package chord.model;

import chord.Exceptions.FileSystemException;

import java.util.HashMap;
import java.util.Map;

public class FileSystem {
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
        if(files.size()==0){
            return;
        }
        else{
            files.remove(key);
        }

    }

    public Map<String, String> freeFileSystem(){
        Map<String, String> files = new HashMap();
        for (String key: this.files.keySet()){
            String value = this.files.remove(key);
            files.put(key, value);
        }
        this.files.clear();
        return files;
    }


}
