package Beans;

import java.io.Serializable;

public class ResponseBean implements Serializable {
    private int responseCode;
    public int getResponseCode(){return responseCode;}
    public void  setResponseCode(int responseCode){this.responseCode = responseCode ;}
}
