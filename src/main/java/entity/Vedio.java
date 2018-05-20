package entity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Created by chao on 2018/4/10.
 */
public class Vedio {
    private String name;
    private String timestamp;
    private String hashcode;
    private String owner;

    public Vedio() {
    }

    public Vedio(String name, String timestamp, String hashcode, String owner) {
        this.name = name;
        this.timestamp = timestamp;
        this.hashcode = hashcode;
        this.owner = owner;
    }

    @Override
    public String toString() {
        String rtn = null;
        try {
            rtn = (new ObjectMapper()).writeValueAsString(this);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return rtn;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getHashcode() {
        return hashcode;
    }

    public void setHashcode(String hashcode) {
        this.hashcode = hashcode;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }
}
