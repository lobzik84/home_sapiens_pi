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
import org.lobzik.home_sapiens.pi.event.Event;

import java.sql.Connection;
import java.util.*;

import org.apache.log4j.Logger;
import org.lobzik.home_sapiens.pi.AppData;
import org.lobzik.home_sapiens.pi.BoxCommonData;
import org.lobzik.tools.db.mysql.DBSelect;
import org.lobzik.tools.db.mysql.DBTools;
import org.lobzik.tools.sms.CIncomingMessage;
import org.lobzik.tools.sms.CMessage;
import org.lobzik.tools.sms.COutgoingMessage;

/**
 *
 * @author lobzik
 */
public class ModemModule implements Module {

    public final String MODULE_NAME = this.getClass().getSimpleName();
    private static ModemModule instance = null;
    private static Connection conn = null;
    private static final int timeout = 1000;
    private static int pollPeriod = 30;
    private static Logger log = null;
    private static boolean busy = false;
    private static boolean run = true;
    private static String smscNumber = "";
    private static final ArrayList<String> smsReplies = new ArrayList();
    public static final int STATUS_NEW = 0;
    public static final int STATUS_SENT = 1;
    public static final int STATUS_READ = 2;
    public static final int STATUS_ERROR = -1;
    private static String lastRecieved = "";
    private static long lastNewCheck = System.currentTimeMillis();
    private static CommPort commPort = null;
    private static ModemSerialWriter serialWriter = null;
    private static final int repliesBufferSize = 100;

    private ModemModule() { //singleton
    }

    public static ModemModule getInstance() {
        if (instance == null) {
            instance = new ModemModule(); //lazy init
        }
        return instance;
    }

    @Override
    public String getModuleName() {
        return MODULE_NAME;
    }

    @Override
    public void start() {
        try {
            conn = DBTools.openConnection(BoxCommonData.dataSourceName);
            connect(BoxCommonData.MODEM_INFO_PORT);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void handleEvent(Event e) {
    }

    public static void finish() {
        DBTools.closeConnection(conn);
    }

    public void run() {

        while (run) {
            try {
                //if (conn == null) conn =  DriverManager.getConnection("jdbc:mysql://192.168.4.4:3306/sh?useUnicode=true" +
                //"&characterEncoding=utf8&autoReconnect=true&user=shuser&password=shpass");
                String sSQL = "SELECT * FROM SMS_OUTBOX WHERE STATUS = " + STATUS_NEW;

                synchronized (this) {
                    try {
                        wait(pollPeriod * 1000);
                    } catch (InterruptedException ie) {
                    }
                }
                List<HashMap> smsToSendList = DBSelect.getRows(sSQL, conn);
                for (HashMap smsToSend : smsToSendList) {
                    log.info("Sending SMS ID " + smsToSend.get("ID"));
                    smsToSend.put("STATUS", STATUS_ERROR);
                    DBTools.updateRow("SMS_OUTBOX", smsToSend, conn);//сразу ему ставим статус с ошибкой, чтобы если что не гонялось по кругу
                    COutgoingMessage outMsg = new COutgoingMessage();

                    outMsg.setMessageEncoding(CMessage.MESSAGE_ENCODING_UNICODE);
                    outMsg.setRecipient((String) smsToSend.get("RECIPIENT"));
                    outMsg.setText((String) smsToSend.get("MESSAGE"));
                    String pdu = outMsg.getPDU(smscNumber);
                    int j = pdu.length();
                    j /= 2;
                    if (smscNumber.length() == 0) {
                        j--;
                    } else {
                        j -= ((smscNumber.length() - 1) / 2);
                        j -= 2;
                    }
                    j--;
                    handle("AT+CMGS=" + j + "\r");
                    handle(pdu + "\032");
                    if (lastRecieved.equalsIgnoreCase("OK")) {
                        smsToSend.put("STATUS", STATUS_SENT);
                        log.info("Successfully sent");
                        DBTools.updateRow("SMS_OUTBOX", smsToSend, conn);
                    } else {

                        log.error("Error sending: " + lastRecieved);
                    }
                }
                if (smsToSendList.isEmpty()) {
                    if (System.currentTimeMillis() - lastNewCheck >= pollPeriod * 1000) {
                        handle("AT+CMGL");
                        if (smsReplies.size() > 2) {
                            smsReplies.clear();
                            handle("AT+CMGD=0,4");
                        }
                        lastNewCheck = System.currentTimeMillis();
                    }

                }
            } catch (Exception e) {
                log.error(e.getMessage());
                synchronized (this) {
                    try {
                        wait(10000);
                    } catch (InterruptedException ie) {
                    }
                }
            }
        }
        DBTools.closeConnection(conn);
    }

    public int sendMessage(String recipient, String text) throws Exception {
        HashMap message = new HashMap();
        message.put("MESSAGE", text);
        message.put("RECIPIENT", recipient); //TODO проверка на формат телефона!
        message.put("DATE", new Date());
        message.put("STATUS", STATUS_NEW);
        int msgId = DBTools.insertRow("SMS_OUTBOX", message, conn);
        synchronized (this) {
            notify();
        }
        return msgId;
    }

    public void exit() {
        run = false;
        synchronized (this) {
            notify();
        }
    }

    public void lineRecieved(ArrayList<String> recievedLines) {

        if (recievedLines.get(recievedLines.size() - 1).trim().length() > 0) {
            lastRecieved = recievedLines.get(recievedLines.size() - 1).trim();
        }
        if (lastRecieved.equals("OK") || lastRecieved.contains("ERROR") || lastRecieved.equals(">")) {
            parseReplyLines(recievedLines);
            synchronized (this) {
                notifyAll();
            }
        }
        if (lastRecieved.contains("+CMTI:")) {
            lastNewCheck = 0;
            synchronized (this) {
                notify();
            }
        }
    }

    private void connect(String portName) throws Exception {
        CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(portName);

        commPort = portIdentifier.open(this.getClass().getName(), timeout);

        SerialPort serialPort = (SerialPort) commPort;
        serialPort.setSerialPortParams(57600,
                SerialPort.DATABITS_8,
                SerialPort.STOPBITS_1,
                SerialPort.PARITY_NONE);

        serialWriter = new ModemSerialWriter(serialPort.getOutputStream(), serialPort);
        serialWriter.start();

        BufferedReader in = new BufferedReader(new InputStreamReader(serialPort.getInputStream()));

        String decodedString;
        while ((decodedString = in.readLine()) != null && run) {
            log.debug("UART: " + decodedString);
            smsReplies.add(decodedString);
            lineRecieved(smsReplies);
            if (smsReplies.size() > repliesBufferSize) {
                smsReplies.remove(0);
            }
            if (decodedString.contains("ERROR")) {
                log.error(decodedString);
            } else {
                log.debug(decodedString);
            }

        }
        in.close();
        serialWriter.finish();
        portIdentifier = null;
        commPort = null;
    }

    public synchronized void handle(String command) throws Exception {
        /*if (connector == null || !connector.isConnected()) {
         return;
         }*/
        while (busy) {
            synchronized (this) {
                try {
                    wait(timeout * 10);
                } catch (InterruptedException ie) {
                }
            }
        }
        busy = true;
        serialWriter.doCommand(command);
        synchronized (this) {
            try {
                wait(timeout);
            } catch (InterruptedException ie) {
            }
            busy = false;
            notifyAll();
        }
    }

    public void parseReplyLines(ArrayList<String> replyLines) {
        boolean incoming = false;
        for (String replyLine : replyLines) {
            if (incoming) {
                try {
                    CIncomingMessage message = new CIncomingMessage(replyLine, 1);
                    HashMap dbMessage = new HashMap();
                    dbMessage.put("MESSAGE", message.getNativeText());
                    dbMessage.put("DATE", message.getDate());
                    dbMessage.put("SENDER", message.getOriginator());
                    dbMessage.put("STATUS", STATUS_NEW);
                    int id = DBTools.insertRow("SMS_INBOX", dbMessage, conn);
                    log.info("Recieved SMS from " + message.getOriginator() + " ID = " + id);
                    HashMap eventData = new HashMap();
                    eventData.put("sender", message.getOriginator());
                    eventData.put("text", message.getNativeText());
                    Event e = new Event("sms_recieved", eventData, Event.Type.USER_ACTION);
                    AppData.eventManager.newEvent(e);

                } catch (Exception e) {
                    log.error("Error while getting SMS: " + e.getMessage());
                }
            }
            incoming = false;
            if (replyLine.contains("+CMGL:") || replyLine.contains("+CMGR:")) {
                incoming = true;
            }
        }
    }

    public static class ModemSerialWriter extends Thread {

        OutputStream out;
        SerialPort port;
        private String command = null;
        private static boolean run = true;

        public ModemSerialWriter(OutputStream out, SerialPort port) {
            setName(this.getClass().getSimpleName() + "-Thread");
            this.out = out;
            this.port = port;
        }

        public void doCommand(String command) {
            this.command = command;
            synchronized (this) {
                notify();
            }
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
                    if (command != null) {
                        outWriter.write(command);
                        outWriter.flush();
                        command = null;
                    }
                    try {
                        synchronized (this) {
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
    // Operator-specific comands
}
