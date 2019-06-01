package chord.model;

import java.util.HashMap;
import java.util.Map;

/**
 * This class contains the files stored to the associated node
 */
public class FileSystem {

    /**
     * The hashmap keys are the keys of the files in Chord
     * The hashmap values are json strings of application specific objects
     */
    HashMap<String , String> files;

    public FileSystem(){
        this.files=new HashMap<>();
    }

    /**
     * Insert a file in this filesyste,
     * @param key of the file
     * @param file to insert
     */
    public void publish(String key, String file){
        files.put(key, file);
    }

    /**
     * To retrieve a file from this filesystem
     * @param key of the file
     * @return the file associated with the key
     */
    public String getFile(String key) {
        return files.get(key);
    }

    /**
     * Delete a file from the filesystem
     * @param key of the file
     */
    public void deleteFile(String key){
        if(files.size()==0){
            return;
        }
        else{
            files.remove(key);
        }

    }

    /**
     * When a node terminates, it removes all the files in order to pass them to the successor
     * @return a copy of the filesystem
     */
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
