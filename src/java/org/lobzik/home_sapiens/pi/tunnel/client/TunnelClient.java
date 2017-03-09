/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lobzik.home_sapiens.pi.tunnel.client;

import java.io.File;
import java.net.InetAddress;
import java.net.URI;
import java.security.Signature;
import java.security.interfaces.RSAPublicKey;
import java.sql.Connection;
import java.util.HashMap;
import javax.naming.Context;
import javax.websocket.ClientEndpoint;
import javax.websocket.CloseReason;
import javax.websocket.ContainerProvider;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;
import javax.xml.bind.DatatypeConverter;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.lobzik.home_sapiens.pi.AppData;
import org.lobzik.home_sapiens.pi.BoxCommonData;
import org.lobzik.home_sapiens.pi.JSONAPI;
import org.lobzik.home_sapiens.pi.event.Event;
import org.lobzik.home_sapiens.pi.modules.TunnelClientModule;
import org.lobzik.tools.Tools;
import org.lobzik.tools.db.mysql.DBSelect;
import org.lobzik.tools.db.mysql.DBTools;

@ClientEndpoint
public class TunnelClient {

    private Session session = null;
    private String box_session_key = "";
    private long lastDataRecieved = 0l;
    private WebSocketContainer container = null;
    private boolean connected = false;
    // private boolean connected = false;
    private static Logger log = null;
    private Context envCtx = null;

    public TunnelClient(String endpointURI, Logger log, Context envCtx) {
        Session s = null;
        this.envCtx = envCtx;
        try {
            URI uri = new URI(endpointURI);// УРИ, УРИ!! Как слышно? Где у Электроника интернет?
            InetAddress address = null;
            try {
                address = InetAddress.getByName(uri.getHost());
                if (address == null) {
                    throw new Exception();
                }
            } catch (Throwable t) {
                throw new Exception("Failed to resolve host IP");
            }
            /*
            if (!address.isReachable(15000)) {
                throw new Exception("Host unreachable by ICMP"); //всё портит, к сожалению. часто пинг есть, а оно не возвращает true
            }*/

            this.log = log;
            container = ContainerProvider.getWebSocketContainer();
            s = container.connectToServer(this, uri);
        } catch (Exception e) {
            //e.printStackTrace();
            try {
                s.close();
            } catch (Exception ee) {
            }
            throw new RuntimeException(e);
        }
    }

    /**
     * Callback hook for Connection open events.
     *
     * @param userSession the userSession which is opened.
     */
    @OnOpen
    public void onOpen(Session session) {
        log.info("Websocket connected");
        // connected = true;
        this.session = session;
        TunnelClientModule.getInstance().setWasConnected();
    }

    /**
     * Callback hook for Connection close events.
     *
     * @param userSession the userSession which is getting closed.
     * @param reason the reason for connection close
     */
    @OnClose
    public void onClose(Session userSession, CloseReason reason) {
        log.info("closing websocket");
        connected = false;
        this.session = null;
    }

    public void disconnect() {
        log.info("Disconnecting");
        //if (connected) {
        try {
            session.close();
            connected = false;
        } catch (Exception e) {
        }
        //}
    }

    public String getBoxSessionKey() {
        return box_session_key;
    }

    /**
     * Callback hook for Message Events. This method will be invoked when a
     * client send a message.
     *
     * @param message The text message
     */
    @OnMessage
    public void onMessage(String message) {
        // System.out.println("got message " + message);
        lastDataRecieved = System.currentTimeMillis();
        try {
            if (message != null && message.startsWith("{")) {
                JSONObject json = new JSONObject(message);
                if (json.has("result") && json.getString("result").equals("error") && json.has("message")) {
                    log.error("Got error from server: " + json.getString("message"));

                }
                if (json.has("box_session_key")) {
                    box_session_key = json.getString("box_session_key");
                    if ("do_login".equals(json.get("result"))) {
                        String challenge = json.getString("challenge");
                        log.debug("Server authentication requested, challenge = " + challenge);
                        Signature digest = Signature.getInstance("SHA256withRSA");
                        digest.initSign(BoxCommonData.PRIVATE_KEY);
                        digest.update(challenge.getBytes("UTF-8"));
                        byte[] digestRaw = digest.sign();
                        String digestHex = DatatypeConverter.printHexBinary(digestRaw);

                        json = new JSONObject();
                        json.put("digest", digestHex);
                        json.put("box_id", BoxCommonData.BOX_ID);
                        sendMessage(json);
                        return;
                    }
                    if ("success_login".equals(json.get("result"))) {
                        connected = true;
                        log.info("Authenticated successfully.");
                        Event e = new Event("tunnel_connected", new HashMap(), Event.Type.SYSTEM_EVENT);
                        AppData.eventManager.newEvent(e);
                        return;
                    }

                    if ("login_error".equals(json.get("result"))) {
                        connected = false;
                        log.error("Authentication error: " + json.get("message"));
                        return;
                    }

                    if ("user_update_success".equals(json.get("result"))) {
                        HashMap user = new HashMap();
                        user.put("id", json.getInt("user_id"));
                        user.put("synced", 1);
                        try (Connection conn = AppData.dataSource.getConnection()) {
                            DBTools.updateRow("users", user, conn);
                            log.info("User uploaded successfully, synced=1");
                        }
                        return;
                    }
                }
                if (json.has("user_id") && json.has("action")) {
                    String action = json.getString("action");
                    int userId = json.getInt("user_id");
                    RSAPublicKey usersKey = AppData.usersPublicKeysCache.getKey(userId);
                    if (usersKey == null) {
                        replyWithError("cannot find users public key userId=" + userId);
                        return;
                    }

                    switch (action) {
                        case "auth_info":
                            if (json.has("auth_type") && json.has("ip")) {
                                String authType = "";
                                if (json.getString("auth_type").equals("SRP")) {
                                    authType = "remote_SRP";
                                } else if (json.getString("auth_type").equals("RSA")) {
                                    authType = "remote_RSA";
                                }
                                HashMap evData = new HashMap();
                                evData.put("auth_type", authType);
                                evData.put("ip", json.getString("ip"));
                                Event ev = new Event("user_logged_in", evData, Event.Type.SYSTEM_EVENT);
                                AppData.eventManager.newEvent(ev);
                            }
                            JSONObject reply = new JSONObject();
                            reply.put("result", "success");
                            sendMessage(reply);
                            break;

                        case "command":
                            if (userId > 0) {
                                usersKey = AppData.usersPublicKeysCache.getKey(userId);
                                JSONAPI.doEncryptedUserCommand(json, BoxCommonData.PRIVATE_KEY, usersKey, userId);
                            }
                            reply = new JSONObject();//JSONAPI.getEncryptedParametersJSON(usersKey);
                            reply.put("result", "success");
                            sendMessage(reply);
                            break;

                        case "do_sql_query":

                            reply = new JSONObject();
                            try (Connection conn = DBTools.openConnection(BoxCommonData.dataSourceName, envCtx)) {
                                String sql = json.getString("sql");
                                DBSelect.executeStatement(sql, null, conn);

                                reply.put("result", "success");
                            } catch (Exception e) {
                                reply.put("result", "error");
                                reply.put("message", e.getMessage());
                            }
                            sendMessage(reply);
                            break;

                        case "do_system_command":

                            reply = new JSONObject();
                            try {
                                String command = json.getString("command");
                                String output = Tools.sysExec(command, new File("/"));
                                reply.put("output", output);
                                reply.put("result", "success");
                            } catch (Exception e) {
                                reply.put("result", "error");
                                reply.put("message", e.getMessage());
                            }
                            sendMessage(reply);
                            break;

                        case "get_capture":
                            Event event = new Event("get_capture", null, Event.Type.USER_ACTION);
                            AppData.eventManager.lockForEvent(event, this);
                            reply = JSONAPI.getEncryptedCaptureJSON(usersKey);
                            reply.put("result", "success");

                            sendMessage(reply);

                            break;

                        case "get_settings":

                            reply = JSONAPI.getSettingsJSON(usersKey, AppData.usersPublicKeysCache.getLogin(userId));
                            reply.put("result", "success");

                            sendMessage(reply);

                            break;

                        case "get_history":

                            reply = JSONAPI.getEncryptedHistoryJSON(json, usersKey);
                            reply.put("result", "success");

                            sendMessage(reply);

                            break;

                        case "get_log":

                            reply = JSONAPI.getEncryptedLogJSON(json, usersKey);
                            reply.put("result", "success");

                            sendMessage(reply);

                            break;

                        default:
                            reply = JSONAPI.getEncryptedParametersJSON(usersKey);
                            reply.put("result", "success");
                            sendMessage(reply);

                    }
                }

            }
        } catch (Exception e) {
            log.error(e.getMessage());
            try {
                replyWithError(e.getMessage());
            } catch (Exception ee) {
            };
        }
    }

    /**
     * Send a message.
     *
     * @param message
     */
    public void sendMessage(JSONObject json) throws Exception {
        json.put("box_session_key", box_session_key);
        this.session.getBasicRemote().sendText(json.toString());
    }

    public void sendTT() throws Exception {

        this.session.getBasicRemote().sendText("tt");
    }

    public long getLastDataRecieved() {
        return lastDataRecieved;
    }

    public boolean isConnected() {
        return connected;
    }

    private void replyWithError(String message) throws Exception {
        JSONObject json = new JSONObject();
        json.put("result", "error");
        json.put("message", message);
        sendMessage(json);
    }
}
