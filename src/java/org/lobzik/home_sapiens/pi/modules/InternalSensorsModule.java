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
import java.sql.Connection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import org.apache.log4j.Logger;
import org.lobzik.home_sapiens.entity.Measurement;
import org.lobzik.home_sapiens.pi.AppData;
import org.lobzik.home_sapiens.pi.BoxCommonData;
import org.lobzik.home_sapiens.pi.event.Event;
import org.lobzik.home_sapiens.pi.event.EventManager;
import org.lobzik.tools.db.mysql.DBTools;

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
    private static final long POLL_PERIOD = 5000;//5 * 60 * 1000l;

    @Override
    public String getModuleName() {
        return MODULE_NAME;
    }

    @Override
    public void handleEvent(Event e) {

    }

    private InternalSensorsModule() { //singleton
    }

    public static InternalSensorsModule getInstance() {
        if (instance == null) {
            instance = new InternalSensorsModule(); //lazy init
        }
        return instance;
    }

    @Override
    public synchronized void run() {
        setName(this.getClass().getSimpleName() + "-Thread");
        try {
            connect(BoxCommonData.SERIAL_PORT);
        } catch (Exception e) {
            e.printStackTrace();
            //break;
        }
    }

    public static void finish() {
        if (serialWriter != null) {
            serialWriter.finish();

        }
        run = false;
    }

    public void setLogger(Logger logger) {
        log = logger;
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
            int paramId = 0;
            Measurement m = new Measurement(val);
            switch (address) {
                case "28:f4:a7:26:04:00:00:35:":
                    paramId = 1;
                    break;
                case "28:80:a6:26:04:00:00:bc:":
                    paramId = 2;
                    break;
                case "28:99:d2:26:04:00:00:03:":
                    paramId = 3;
                    break;
                case "28:a5:b7:26:04:00:00:50:":
                    paramId = 4;
                    break;

            }
            if (paramId > 0) {
                HashMap eventData = new HashMap();
                eventData.put("parameter_id", paramId);
                eventData.put("measurement_new", m);

                Event e = new Event("1-wire updated", eventData, Event.Type.PARAMETER_UPDATED);

                AppData.eventManager.newEvent(e);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void connect(String portName) throws Exception {
        CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(portName);

        int timeout = 2000;
        commPort = portIdentifier.open(this.getClass().getName(), timeout);

        SerialPort serialPort = (SerialPort) commPort;
        serialPort.setSerialPortParams(9600,
                SerialPort.DATABITS_8,
                SerialPort.STOPBITS_1,
                SerialPort.PARITY_NONE);

        serialWriter = new SerialWriter(serialPort.getOutputStream(), serialPort);
        serialWriter.start();

        BufferedReader in = new BufferedReader(new InputStreamReader(serialPort.getInputStream()));

        String decodedString;
        while ((decodedString = in.readLine()) != null && run) {
            System.out.println(decodedString);
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

        public synchronized void run() {
            OutputStreamWriter outWriter = new OutputStreamWriter(this.out);
            while (run) {
                try {
                    outWriter.write("gt\r\n");
                    outWriter.flush();
                    try {
                        synchronized (this) {
                            wait(POLL_PERIOD);
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
