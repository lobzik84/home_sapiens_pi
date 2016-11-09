/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lobzik.home_sapiens.pi;

import java.util.Date;
import org.json.JSONObject;

/**
 *
 * @author lobzik
 */
public class WebNotification {
    
    public Severity severity;
    public String parameterAlias;
    public String text;
    public Date startDate;
    public Date endDate;   
    
    
    public enum Severity
    {
        INFO, //INFO инфо - просто оповещение, бесцветный или серый
        OK, //WARNING вернулось в норму, зелёный
        ALERT, //ERROR ошибка, плохо, но жить можно
        ALARM //FATAL критичное, красный
    };
    
    public final int id;
    
    public WebNotification (Severity severity, String parameterAlias, String text, Date startDate, Date endDate) {
        id = (int) (Math.random() * 100000d);
        this.severity = severity;
        this.parameterAlias = parameterAlias;
        this.text = text;
        this.startDate = startDate;
        this.endDate = endDate;
    }
    
    public JSONObject toJSON() {
        JSONObject json = new JSONObject();
        json.put("id", id);
        json.put("severity", severity.toString());
        json.put("text", text);
        json.put("parameterAlias", parameterAlias);
        json.put("startDate", startDate.getTime());
        json.put("endDate", endDate.getTime());
        
        return json;
    }

}
