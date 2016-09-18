/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lobzik.home_sapiens.pi;

import java.util.HashMap;
import org.json.JSONObject;
import org.lobzik.home_sapiens.entity.Measurement;
import org.lobzik.home_sapiens.entity.Parameter;
import org.lobzik.home_sapiens.pi.event.Event;

/**
 * class for generating and parsing JSON to be used in TunnelClient and
 * JSONServlet
 *
 * @author lobzik
 */
public class JSONInterface {

    public static void doUserCommand(JSONObject json) throws Exception {
        String commandName = json.getString("command_name");
        HashMap commandData = new HashMap();
        JSONObject jsonData = json.getJSONObject("command_data");
        for (String key : jsonData.keySet()) {
            commandData.put(key, jsonData.get(key));
        }
        Event event = new Event(commandName, commandData, Event.Type.USER_ACTION);
        AppData.eventManager.newEvent(event);
    }

    public static JSONObject getParametersJSON() throws Exception {

        JSONObject paramsJson = new JSONObject();
        ParametersStorage ps = AppData.parametersStorage;
        MeasurementsCache mc = AppData.measurementsCache;
        for (Integer pId : ps.getParameterIds()) {
            Parameter p = ps.getParameter(pId);
            if (mc.getLastMeasurement(p) == null) {
                continue;
            }
            JSONObject parJson = new JSONObject();
            parJson.put("par_name", p.getName());
            parJson.put("par_unit", p.getUnit());
            Measurement m = mc.getLastMeasurement(p);
            parJson.put("last_value", m.toStringValue());
            parJson.put("last_date", m.getTime());
            paramsJson.put(pId + "", parJson);
        }
        JSONObject reply = new JSONObject();
        reply.put("parameters", paramsJson);
        return reply;

    }
}
