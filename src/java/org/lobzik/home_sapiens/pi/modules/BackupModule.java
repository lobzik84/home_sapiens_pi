/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lobzik.home_sapiens.pi.modules;

import org.apache.log4j.Appender;
import org.apache.log4j.Logger;
import org.lobzik.home_sapiens.pi.AppData;
import org.lobzik.home_sapiens.pi.BoxCommonData;
import org.lobzik.home_sapiens.pi.ConnJDBCAppender;
import org.lobzik.home_sapiens.pi.event.Event;
import org.lobzik.home_sapiens.pi.event.EventManager;
import org.lobzik.tools.Tools;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.security.SecureRandom;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import org.lobzik.home_sapiens.pi.UsersPublicKeysCache;
import org.lobzik.tools.StreamGobbler;

/**
 *
 * @author lobzik
 */
public class BackupModule implements Module {

    public final String MODULE_NAME = this.getClass().getSimpleName();
    public static final int AES_CHUNK_SIZE = 1048576;

    private static BackupModule instance = null;
    private static Logger log = null;
    private static String BKP_UPLOAD_URL = null;

    public static BackupModule getInstance() {
        if (instance == null) {
            instance = new BackupModule(); //lazy init
            log = Logger.getLogger(instance.MODULE_NAME);
            Appender appender = ConnJDBCAppender.getAppenderInstance(AppData.dataSource, instance.MODULE_NAME);
            log.addAppender(appender);
            BKP_UPLOAD_URL = BoxCommonData.TUNNEL_SERVER_URL.replace("wss:", "http:");
            BKP_UPLOAD_URL = BKP_UPLOAD_URL.replace("ws:", "http:");
            BKP_UPLOAD_URL = BKP_UPLOAD_URL.replace("/wss", "/bkp");
        }
        return instance;
    }

    private BackupModule() {

    }

    @Override
    public String getModuleName() {
        return MODULE_NAME;
    }

    @Override
    public void start() {
        try {
            EventManager.subscribeForEventType(this, Event.Type.BEHAVIOR_EVENT);
            EventManager.subscribeForEventType(this, Event.Type.TIMER_EVENT);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void handleEvent(Event e) {
        if (e.name.equals("do_backup")) //    && e.type == Event.Type.BEHAVIOR_EVENT) 
        {
            try {
                log.info("Creating database backup");
                if (!TunnelClientModule.getInstance().tunnelIsUp()) {
                    throw new Exception("No server link");
                }
                String boxSessionKey = TunnelClientModule.getInstance().getBoxSessionKey();

                new Thread() {
                    @Override
                    public void run() {
                        try {
                            String backupDir = AppData.getBackupWorkDir().getAbsolutePath();
                            String backupFileName = backupDir + "/dump_boxId_" + BoxCommonData.BOX_ID + "_" + System.currentTimeMillis() + ".sql.gz";
                            Runtime runtime = Runtime.getRuntime();
                            Process process = runtime.exec("/bin/bash " + backupDir + "/createBackup.sh " + backupFileName);
                            /*StringBuilder output = new StringBuilder();
                            StreamGobbler errorGobbler = new StreamGobbler(process.getErrorStream(), output);
                            StreamGobbler outputGobbler = new StreamGobbler(process.getInputStream(), output);
                            errorGobbler.start();
                            outputGobbler.start();*/
                            process.waitFor();
                            File backupFile = new File(backupFileName);
                            if (process.exitValue() == 0 && backupFile.exists()) {
                                // encrypt and upload to server
                                log.info("Backup created");
                                encryptAndUploadFile(backupFile, boxSessionKey);
                                log.info("Backup uploaded");
                                Tools.sysExec("sudo rm " + backupFileName, AppData.getBackupWorkDir());
                            } else {
                                log.error("Error creating backup: " + process.exitValue());
                                //System.err.println(output);
                            }
                        } catch (Exception e) {
                            log.error(e.getMessage());
                        }
                    }
                }.start();
            } catch (Exception ee) {
                log.error("Error " + ee.getMessage());
            }
        }
    }

    private static void encryptAndUploadFile(File file, String boxSessionKey) throws Exception {

        long fileSize = file.length();

        KeyGenerator kgen = KeyGenerator.getInstance("AES");
        SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
        sr.nextBytes(new byte[8]);
        sr.setSeed(1232);//add entropy
        kgen.init(128, sr);
        SecretKey skey = kgen.generateKey();
        byte[] rawKey = skey.getEncoded();
        String keyString = DatatypeConverter.printHexBinary(rawKey);

        byte[] iv = new byte[]{0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0};

        IvParameterSpec ivSpec = new IvParameterSpec(iv);

        SecretKeySpec skeySpec = new SecretKeySpec(rawKey, "AES");

        byte[] plainBytes = new byte[AES_CHUNK_SIZE];
        String noPadding = "AES/CFB/NoPadding";
        int read = 0;
        long pos = 0;
        FileInputStream fis = new FileInputStream(file);


        Cipher cipherRSA = Cipher.getInstance("RSA");
        // encrypt the plain text using the public key
        cipherRSA.init(Cipher.ENCRYPT_MODE, UsersPublicKeysCache.getInstance().getMainKey());
        byte[] keyCipherRaw = cipherRSA.doFinal(keyString.getBytes());

        String keyCipher = DatatypeConverter.printHexBinary(keyCipherRaw);
        
        while ((read = fis.read(plainBytes)) > 0) {

            String addr = BKP_UPLOAD_URL + file.getName() + "?f=" + pos + "&s=" + boxSessionKey;
            if ((pos + AES_CHUNK_SIZE) >= fileSize) {
                addr += "&done=true";
            }
            if (pos == 0) {
                addr += "&kc=" + keyCipher;
            }
            URL url = new URL(addr);

            Cipher cipher = Cipher.getInstance(noPadding);

            cipher.init(Cipher.ENCRYPT_MODE, skeySpec, ivSpec);
            byte[] encrypted = cipher.doFinal(plainBytes, 0, read);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("User-Agent", "Mozilla/5.0");
            conn.setRequestProperty("Content-Length", "0");
            conn.setRequestProperty("Accept", "*/*");

            conn.setRequestProperty("Content-Type", "application/octet-stream");
            conn.setUseCaches(false);
            conn.setDoInput(true);
            conn.setDoOutput(true);
            OutputStream os = conn.getOutputStream();

            os.write(encrypted);
            os.flush();
            os.close();
            int responseCode = conn.getResponseCode();
            if (responseCode != 200) {
                log.error("Error while upolading to " + addr);
                log.error("Response code " + responseCode);
                break;
            }

            pos = pos + read;
        }
    }
}
