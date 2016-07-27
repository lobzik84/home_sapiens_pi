/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lobzik.home_sapiens.pi.modules;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import org.apache.log4j.Appender;
import org.apache.log4j.Logger;
import org.lobzik.home_sapiens.entity.Measurement;
import org.lobzik.home_sapiens.pi.AppData;
import org.lobzik.home_sapiens.pi.BoxCommonData;
import org.lobzik.home_sapiens.pi.ConnJDBCAppender;
import org.lobzik.home_sapiens.pi.event.Event;
import org.lobzik.home_sapiens.pi.event.EventManager;

/**
 *
 * @author lobzik
 */
public class InternalSensorsModule extends Thread implements Module {

    private static InternalSensorsModule instance = null;


    public final String MODULE_NAME = this.getClass().getSimpleName();

    private static Logger log = null;
    private static boolean run = true;
    private static CommPort commPort = null;
    private static SerialWriter serialWriter = null;

    public static final int HISTORY_SIZE = 50;
    private static final long POLL_PERIOD = 5000l;

    @Override
    public String getModuleName() {
        return MODULE_NAME;
    }

    @Override
    public void handleEvent(Event e) {
        if (e.type == Event.Type.TIMER_EVENT && e.name.equals("internal_sensors_poll")) {
            serialWriter.poll();
        }
    }

    private InternalSensorsModule() { //singleton
    }

    public static InternalSensorsModule getInstance() {
        if (instance == null) {
            instance = new InternalSensorsModule(); //lazy init
            log = Logger.getLogger(instance.MODULE_NAME);
            Appender appender = ConnJDBCAppender.getAppenderInstance(AppData.dataSource, instance.MODULE_NAME);
            log.addAppender(appender);
        }
        return instance;
    }

    @Override
    public synchronized void run() {
        setName(this.getClass().getSimpleName() + "-Thread");
        log.info("Starting " + getName() + " on " + BoxCommonData.SERIAL_PORT);
        EventManager.subscribeForEventType(this, Event.Type.TIMER_EVENT);
        try {
            connect(BoxCommonData.SERIAL_PORT);
        } catch (Exception e) {
            log.error(e.getMessage());
            e.printStackTrace();
            //break;
        }
    }

    public static void finish() {
        log.info("Stopping " + serialWriter.getName());
        if (serialWriter != null) {
            serialWriter.finish();

        }
        run = false;
    }


    private void parse1WireReply(String data) {
        try {
            if (!data.contains("TEMP")) {
                return;
            }
            String val = data.substring(data.lastIndexOf(":") + 1, data.length());
            val = val.trim();
            String address = data.substring(0, data.indexOf("TEMP"));
            address = address.trim();
            int paramId = AppData.parametersStorage.resolveAlias(address);
            Measurement m = new Measurement(val);

            if (paramId > 0) {
                HashMap eventData = new HashMap();
                eventData.put("parameter", AppData.parametersStorage.getParameter(paramId));
                eventData.put("measurement", m);
                Event e = new Event("1-wire updated", eventData, Event.Type.PARAMETER_UPDATED);

                AppData.eventManager.newEvent(e);
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            e.printStackTrace();
        }
    }

    private void connect(String portName) throws Exception {
        CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(portName);

        int timeout = 2000;
        commPort = portIdentifier.open(this.getClass().getName(), timeout);

        SerialPort serialPort = (SerialPort) commPort;
        serialPort.setSerialPortParams(57600,
                SerialPort.DATABITS_8,
                SerialPort.STOPBITS_1,
                SerialPort.PARITY_NONE);

        serialWriter = new SerialWriter(serialPort.getOutputStream(), serialPort);
        serialWriter.start();

        BufferedReader in = new BufferedReader(new InputStreamReader(serialPort.getInputStream()));

        String decodedString;
        while ((decodedString = in.readLine()) != null && run) {
            log.debug("UART: " + decodedString);
            parse1WireReply(decodedString);
        }
        in.close();
        serialWriter.finish();
        portIdentifier = null;
        commPort = null;
    }

    public static class SerialWriter extends Thread {

        OutputStream out;
        SerialPort port;
        private static boolean run = true;

        public SerialWriter(OutputStream out, SerialPort port) {
            setName(this.getClass().getSimpleName() + "-Thread");
            this.out = out;
            this.port = port;
        }

        public void finish() {
            run = false;
            synchronized (this) {
                notify();
            }
        }
        
        public void poll() {
            synchronized (this) {
                notify();
            }
        }

        public synchronized void run() {
            OutputStreamWriter outWriter = new OutputStreamWriter(this.out);
            while (run) {
                try {
                    outWriter.write("gt\r\n");
                    outWriter.flush();
                    try {
                        synchronized (this) {
                            //wait(POLL_PERIOD);
                            wait();
                        }
                    } catch (InterruptedException ie) {
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
            try {
                outWriter.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                port.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

}