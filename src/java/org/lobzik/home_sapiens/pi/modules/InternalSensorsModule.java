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
import java.util.LinkedList;
import org.apache.log4j.Logger;
import org.lobzik.home_sapiens.entity.Measurement;

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

    private static SerialWriter serialWriter = null;
    
    public static final LinkedList<Measurement> internalTemps = new LinkedList();
    public static final LinkedList<Measurement> roomTemps = new LinkedList();
    public static final LinkedList<Measurement> leftACTemps = new LinkedList();
    public static final LinkedList<Measurement> rightACTemps = new LinkedList();
    
    
    public static final int HISTORY_SIZE = 50;
    private static final long POLL_PERIOD = 5 * 60 * 1000l;
    
    private InternalSensorsModule() { //singleton
    }

    public static InternalSensorsModule getInstance() {
        if (instance == null) {
            instance = new InternalSensorsModule(); //lazy init
        }
        return instance;
    }

    public synchronized void run() {
        while (run) {
            try {
                connectTries++;
                state = CONNECTING_STATE;
                connect("/dev/ttyACM0");

            } catch (Exception e) {
                e.printStackTrace();
                //break;
            }
            if (connectTries > 5) {
                run = false;
                break;
            }

        }
    }

    public synchronized void finish() {
        //log.info("Disconnecting...");
        if (serialWriter != null) {
            serialWriter.finish();
        }
        run = false;
        state = NOT_CONNECTED_STATE;

        synchronized (this) {
            notifyAll();

        }
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
            if (!data.contains("TEMP")) return;
            String val = data.substring(data.lastIndexOf(":")+1, data.length());
            val = val.trim();
            String address = data.substring(0, data.indexOf("TEMP"));
            address = address.trim();
            Measurement m = new Measurement(val);
            switch (address) {
                case "28:f4:a7:26:04:00:00:35:": 
                     internalTemps.add(m);
                    while (internalTemps.size() > HISTORY_SIZE) {
                        internalTemps.remove(0);
                    }
                         
                    break;
                case "28:80:a6:26:04:00:00:bc:": 
                     roomTemps.add(m);
                    while (roomTemps.size() > HISTORY_SIZE) {
                        roomTemps.remove(0);
                    }
                         
                    break;
                case "28:99:d2:26:04:00:00:03:": 
                     leftACTemps.add(m);
                    while (leftACTemps.size() > HISTORY_SIZE) {
                        leftACTemps.remove(0);
                    }
                         
                    break;
                case "28:a5:b7:26:04:00:00:50:": 
                     rightACTemps.add(m);
                    while (rightACTemps.size() > HISTORY_SIZE) {
                        rightACTemps.remove(0);
                    }
                         
                    break;
                    
            }
            
        } catch (Exception e) 
        {
            
        }
    }
    private void connect(String portName) throws Exception {
        CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(portName);
        if (portIdentifier.isCurrentlyOwned()) {
            System.out.println("Error: Port is currently in use");
        } else {
            int timeout = 2000;
            CommPort commPort = portIdentifier.open(this.getClass().getName(), timeout);

            if (commPort instanceof SerialPort) {
                SerialPort serialPort = (SerialPort) commPort;
                serialPort.setSerialPortParams(9600,
                        SerialPort.DATABITS_8,
                        SerialPort.STOPBITS_1,
                        SerialPort.PARITY_NONE);

                serialWriter = new SerialWriter(serialPort.getOutputStream());
                serialWriter.start();

                BufferedReader in = new BufferedReader(new InputStreamReader(serialPort.getInputStream()));
                
                String decodedString;
                //StringBuffer sb = new StringBuffer();
                while ((decodedString = in.readLine()) != null && run) {
                    //sb.append(decodedString);
                    System.out.println(decodedString);
                    parse1WireReply(decodedString);
                }
                in.close();
                //System.out.print(sb.toString());

            } else {
                System.out.println("Error: Only serial ports are handled by this example.");
            }
        }
    }

    public static class SerialWriter extends Thread {

        OutputStream out;
        private static boolean run = true;

        public SerialWriter(OutputStream out) {
            this.out = out;
        }

        public void finish() {
            run = false;
            synchronized (this) {
                notify();
            }
        }

        public void run() {
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

        }
    }

}
