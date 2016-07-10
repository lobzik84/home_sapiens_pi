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
import org.lobzik.home_sapiens.pi.BoxCommonData;
import org.lobzik.tools.db.mysql.DBTools;

/**
 *
 * @author lobzik
 */
public class InternalSensorsModule extends Thread {

    private static InternalSensorsModule instance = null;

    public static final int NOT_CONNECTED_STATE = -1;
    public static final int CONNECTING_STATE = 0;
    public static final int CONNECTED_STATE = 1;

    private int state = NOT_CONNECTED_STATE;
    private static long connectTries = 0;
    private static Logger log = null;
    private static boolean run = true;
    private static CommPort commPort = null;
    private static SerialWriter serialWriter = null;

    public static final int HISTORY_SIZE = 50;
    private static final long POLL_PERIOD = 5 * 60 * 1000l;
    private static Connection conn = null;

    private InternalSensorsModule() { //singleton
    }

    public static InternalSensorsModule getInstance() {
        if (instance == null) {
            instance = new InternalSensorsModule(); //lazy init
        }
        return instance;
    }

    public synchronized void run() {
        try {
            connectTries++;
            state = CONNECTING_STATE;
            conn = DBTools.openConnection(BoxCommonData.dataSourceName);
            connect("/dev/ttyACM0");
            state = CONNECTED_STATE;
           // conn.close();

        } catch (Exception e) {
            e.printStackTrace();
            //break;
        }
    }

    public static void finish() {
        //log.info("Disconnecting...");
        if (serialWriter != null) {
            serialWriter.finish();

        }
        run = false;
        instance.state = NOT_CONNECTED_STATE;
        DBTools.closeConnection(conn);
        /*try {
         if (commPort != null) {
         commPort.close();
         }
         commPort = null;
         } catch (Throwable t) {
         t.printStackTrace();
         }*/
        //log.info("Disconnected.");
    }

    public int getTunnelState() {
        return state;
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
            int param_id = 0;
            Measurement m = new Measurement(val);
            switch (address) {
                case "28:f4:a7:26:04:00:00:35:":
                    param_id = 1;
                    break;
                case "28:80:a6:26:04:00:00:bc:":
                    param_id = 2;
                    break;
                case "28:99:d2:26:04:00:00:03:":
                    param_id = 3;
                    break;
                case "28:a5:b7:26:04:00:00:50:":
                    param_id = 4;
                    break;

            }
            HashMap dataMap = new HashMap();
            dataMap.put("parameter_id", param_id);
            dataMap.put("value_d", m.getDoubleValue());
            dataMap.put("date", new Date(m.getTime()));
            DBTools.insertRow("sensors_data", dataMap, conn);
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
            this.out = out;
            this.port = port;
        }

        public synchronized void finish() {
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
