/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package handler;

import java.util.Map;

/**
 *
 * @author Admin
 */
public class GameMessage {

    private String message;
    private Map<String, String> metaData;

    public GameMessage() {
        super();
    }

    public GameMessage(String message, Map<String, String> metaData) {
        super();
        this.message = message;
        this.metaData = metaData;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Map<String, String> getMetaData() {
        return metaData;
    }

    public void setMetaData(Map<String, String> metaData) {
        this.metaData = metaData;
    }
}
