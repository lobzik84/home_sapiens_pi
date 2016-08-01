/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lobzik.home_sapiens.pi;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.lobzik.home_sapiens.entity.Measurement;
import org.lobzik.home_sapiens.entity.Parameter;

/**
 *
 * @author lobzik
 */
public class MeasurementsCache {

    private static final Map<Parameter, List> cache = new HashMap();
    private static MeasurementsCache instance = null;
    private static final int CACHE_SIZE = 100;

    private MeasurementsCache() {
    }

    public static MeasurementsCache getInstance() {
        if (instance == null) {
            for (Integer paramId : AppData.parametersStorage.getParameterIds()) {
                List<Measurement> history = new LinkedList();
                cache.put(AppData.parametersStorage.getParameter(paramId), history);
            }

            instance = new MeasurementsCache();
        }
        return instance;

    }
    
    public void add(Measurement m) {
        Parameter p = m.getParameter();
        List<Measurement> history = cache.get(p);
        history.add(m);
        while (history.size() > CACHE_SIZE) {
            history.remove(0);
        }
        cache.put(p, history);
    }

    public Measurement getLastMeasurement (Parameter p) {
        List<Measurement> history = cache.get(p);
        if (history.size() < 1) return null;
        return history.get(history.size()-1);
    }
    
    public Measurement getAvgMeasurement(Parameter p) {
        return getAvgMeasurementFrom(p, 0l);
    }

    public Measurement getMinMeasurement(Parameter p) {
        return getMinMeasurementFrom(p, 0l);
    }

    public Measurement getMaxMeasurement(Parameter p) {
        return getMaxMeasurementFrom(p, 0l);
    }

    public Measurement getAvgMeasurementFrom(Parameter p, long millis) {
        if (!(p.getType() == Parameter.Type.DOUBLE || p.getType() == Parameter.Type.INTEGER)) {
            return null; //calculating of other types doe not make sense
        }
        List<Measurement> history = cache.get(p);
        if (history.isEmpty()) {
            return null;
        }
        long lastMeasurement = 0l;
        int occurencies = 0;
        double sum = 0d;

        for (Measurement m : history) {
            if (m.getTime() > millis && m.getDoubleValue() != null) {
                occurencies++;
                if (m.getTime() > lastMeasurement) {
                    lastMeasurement = m.getTime(); //get last measure time, average value, calculetd for last measure 
                }
                sum += m.getDoubleValue();
            }
        }
        if (occurencies == 0) {
            return null;
        }
        Measurement avg = new Measurement(p, sum / occurencies, lastMeasurement);
        return avg;
    }

    public Measurement getMaxMeasurementFrom(Parameter p, long millis) {
        if (!(p.getType() == Parameter.Type.DOUBLE || p.getType() == Parameter.Type.INTEGER)) {
            return null; //calculating of other types doe not make sense
        }
        List<Measurement> history = cache.get(p);
        if (history.isEmpty()) {
            return null;
        }
        Measurement max = history.get(0);

        for (Measurement m : history) {
            if (m.getTime() > millis && m.getDoubleValue() != null) {
                if (m.getDoubleValue() > max.getDoubleValue()) {
                    max = m;
                }
            }
        }
        return max;
    }

    public Measurement getMinMeasurementFrom(Parameter p, long millis) {
        if (!(p.getType() == Parameter.Type.DOUBLE || p.getType() == Parameter.Type.INTEGER)) {
            return null; //calculating of other types doe not make sense
        }
        List<Measurement> history = cache.get(p);
        if (history.isEmpty()) {
            return null;
        }
        Measurement min = history.get(0);

        for (Measurement m : history) {
            if (m.getTime() > millis && m.getDoubleValue() != null) {
                if (m.getDoubleValue() < min.getDoubleValue()) {
                    min = m;
                }
            }
        }
        return min;
    }

    public Integer getTransferTrueCountFrom(Parameter p, long millis) {
        if (p.getType() != Parameter.Type.BOOLEAN) {
            return null;
        }
        List<Measurement> history = cache.get(p);
        if (history.isEmpty()) {
            return 0;
        }
        int count = 0;
        Boolean prev = history.get(0).getBooleanValue();
        for (Measurement m : history) { 
            if (m.getBooleanValue().equals(true) && prev.equals(false))
                count++;
            prev = m.getBooleanValue();
        }
        return count;
    }
}
