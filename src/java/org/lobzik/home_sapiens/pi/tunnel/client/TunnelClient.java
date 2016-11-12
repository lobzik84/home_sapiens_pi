/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lobzik.home_sapiens.pi.tunnel.client;

import java.net.URI;
import java.security.Signature;
import java.security.interfaces.RSAPublicKey;
import java.sql.Connection;
import java.util.HashMap;
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

    public TunnelClient(String endpointURI, Logger log) {
        try {
            this.log = log;
            container = ContainerProvider.getWebSocketContainer();
            container.connectToServer(this, new URI(endpointURI));
        } catch (Exception e) {
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
        if (connected) {
            try {
                session.close();
                connected = false;
            } catch (Exception e) {
            }
        }
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
                        Event e = new Event("upload_unsynced_users_to_server", new HashMap(), Event.Type.SYSTEM_EVENT);
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
                        case "command":
                            if (userId > 0) {
                                usersKey = AppData.usersPublicKeysCache.getKey(userId);
                                JSONAPI.doEncryptedUserCommand(json, BoxCommonData.PRIVATE_KEY, usersKey);
                            }
                            JSONObject reply = new JSONObject();//JSONAPI.getEncryptedParametersJSON(usersKey);
                            reply.put("result", "success");
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
