/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lobzik.home_sapiens.pi;

import org.lobzik.home_sapiens.entity.UsersSession;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.PublicKey;
import java.security.Signature;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.sql.Connection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONObject;
import org.lobzik.home_sapiens.pi.event.Event;
import org.lobzik.home_sapiens.pi.modules.TunnelClientModule;
import org.lobzik.tools.Tools;
import org.lobzik.tools.db.mysql.DBSelect;
import org.lobzik.tools.db.mysql.DBTools;

/**
 *
 * @author lobzik
 */
@WebServlet(name = "JsonServlet", urlPatterns = {"/json", "/json/*"})
public class JSONServlet extends HttpServlet {

    private static final BigInteger G = new BigInteger("2"); //TODO move to CommonData (common for all projects)
    private static final BigInteger N = new BigInteger("115b8b692e0e045692cf280b436735c77a5a9e8a9e7ed56c965f87db5b2a2ece3", 16);
    private static final BigInteger K = new BigInteger("c46d46600d87fef149bd79b81119842f3c20241fda67d06ef412d8f6d9479c58", 16);
    private static final String SALT_ALPHABET = "1234567890abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String FAKE_SALT_KEY = "mri9gjn0990)M09V^&DF&*GR^%^WTioh89t;";

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT");
        //response.setHeader("Access-Control-Allow-Credentials", "true");  

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        InputStream is = request.getInputStream();
        long readed = 0;
        long content_length = request.getContentLength();
        byte[] bytes = new byte[65536];
        while (readed < content_length) {
            int r = is.read(bytes);
            if (r < 0) {
                break;
            }
            baos.write(bytes, 0, r);
            readed += r;
        }
        baos.close();
        String requestString = baos.toString("UTF-8");

        try {
            if (requestString.startsWith("{")) {
                JSONObject json = new JSONObject(requestString);
                request.setAttribute("json", json);
                int userId = 0;
                if (json.has("session_key")) {
                    String session_key = json.getString("session_key");
                    UsersSession session = AppData.sessions.get(session_key);
                    if (session != null) {
                        userId = Tools.parseInt(session.get("UserId"), 0);
                    }
                }
                String action = json.getString("action");
                switch (action) {
                    case "check":
                        doRequestLogin(request, response);
                        break;

                    case "register":
                        registerUser(request, response);
                        break;

                    case "handshake_srp":
                        handshakeUserSRP(request, response);
                        break;

                    case "login_srp":
                        loginUserSRP(request, response);
                        break;

                    case "update_srp":
                        updateUserSRP(request, response);
                        break;

                    case "login_rsa":
                        loginUserRSA(request, response);
                        break;

                    case "kf_upload":
                        if (userId > 0) {
                            updateKeyFile(userId, request, response);
                            Event e = new Event("upload_unsynced_users_to_server", new HashMap(), Event.Type.SYSTEM_EVENT);
                            AppData.eventManager.newEvent(e);
                        } else {
                            doRequestLogin(request, response);
                        }
                        break;

                    case "kf_download":
                        if (userId > 0) {
                            downloadKeyFile(userId, request, response);
                        } else {
                            doRequestLogin(request, response);
                        }
                        break;

                    case "command":
                        if (userId > 0) {
                            doUserCommand(request, response);
                            //replyWithParameters(request, response);

                        } else {
                            doRequestLogin(request, response);
                        }
                        break;

                    case "get_capture":
                        if (userId > 0) {
                            replyWithCapture(request, response);
                        } else {
                            doRequestLogin(request, response);
                        }

                        break;

                    case "get_settings":
                        if (userId > 0) {
                            replyWithSettings(request, response);
                        } else {
                            doRequestLogin(request, response);
                        }

                        break;

                    case "get_history":
                        if (userId > 0) {
                            replyWithHistory(request, response);
                        } else {
                            doRequestLogin(request, response);
                        }

                        break;

                    case "get_log":
                        if (userId > 0) {
                            replyWithLog(request, response);
                        } else {
                            doRequestLogin(request, response);
                        }

                        break;
                    default:
                        if (userId > 0) {
                            replyWithParameters(request, response);
                        } else {
                            doRequestLogin(request, response);
                        }
                }
            } else {
                response.getWriter().print("accepted json only");
            }
        } catch (Throwable e) {
            //e.printStackTrace();
            response.getWriter().print("{\"result\":\"error\",\"message\":\"" + e.getMessage() + "\"}");
        }

    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

    private void registerUser(HttpServletRequest request, HttpServletResponse response) throws Exception {
        //check if it's the only user/
        String sSQL = "select id from users where status = 1;";
        JSONObject json = (JSONObject) request.getAttribute("json");

        try (Connection conn = DBTools.openConnection(BoxCommonData.dataSourceName)) {
            List<HashMap> resList = DBSelect.getRows(sSQL, conn);
            if (resList.size() > 0) {
                throw new Exception("User registered already, please login");
            }
            HashMap newUser = new HashMap();
            String publicKey = json.getString("public_key");
            BigInteger modulus = new BigInteger(publicKey, 16);
            RSAPublicKeySpec spec = new RSAPublicKeySpec(modulus, BoxCommonData.RSA_E);
            KeyFactory factory = KeyFactory.getInstance("RSA");
            PublicKey usersPublicKey = factory.generatePublic(spec);
            for (String key : json.keySet()) {
                newUser.put(key, json.get(key));
            }
            newUser.put("status", 1);
            int newUserId = DBTools.insertRow("users", newUser, conn);
            String session_key = AppData.sessions.createSession();
            AppData.sessions.get(session_key).put("UserId", newUserId);
            AppData.sessions.get(session_key).put("UsersPublicKey", usersPublicKey);
            AppData.sessions.get(session_key).put("Login", json.getString("login"));
            String hexModulus = BoxCommonData.PUBLIC_KEY.getModulus().toString(16);
            json = new JSONObject();
            json.put("result", "success");
            json.put("new_user_id", newUserId);
            json.put("box_public_key", hexModulus);
            json.put("box_id", BoxCommonData.BOX_ID);
            json.put("session_key", session_key);
            response.getWriter().print(json.toString());

        }
    }

    private void updateKeyFile(int userId, HttpServletRequest request, HttpServletResponse response) throws Exception {
        if (userId <= 0) {
            throw new Exception("Trying to upload unauthorized keyfile!");
        }
        try (Connection conn = DBTools.openConnection(BoxCommonData.dataSourceName)) {
            HashMap dataMap = new HashMap();
            JSONObject json = (JSONObject) request.getAttribute("json");
            dataMap.put("id", userId);
            dataMap.put("synced", 0);
            dataMap.put("keyfile", json.getString("kfCipher"));
            DBTools.updateRow("users", dataMap, conn);
            json = new JSONObject();
            json.put("result", "success");
            response.getWriter().print(json.toString());
        }
    }

    private void downloadKeyFile(int userId, HttpServletRequest request, HttpServletResponse response) throws Exception {
        String sSQL = "select id, keyfile from users where status = 1 and id = " + userId;
        try (Connection conn = DBTools.openConnection(BoxCommonData.dataSourceName)) {
            List<HashMap> resList = DBSelect.getRows(sSQL, conn);
            if (resList.isEmpty()) {
                throw new Exception("User id=" + userId + " not found");
            }
            HashMap userMap = resList.get(0);
            JSONObject json = new JSONObject();
            json.put("result", "success");
            json.put("kfCipher", (String) userMap.get("keyfile"));
            response.getWriter().print(json.toString());
        }
    }

    private void loginUserRSA(HttpServletRequest request, HttpServletResponse response) throws Exception {
        JSONObject json = (JSONObject) request.getAttribute("json");
        UsersSession session = null;
        if (json.has("session_key")) {
            String session_key = json.getString("session_key");
            session = AppData.sessions.get(session_key);
        }
        if (session == null) {
            return;
        }
        String challenge = (String) session.get("challenge");
        int userId = Tools.parseInt(json.get("user_id"), 0);
        String digest = json.getString("digest");
        String sSQL = "select id, public_key, login from users where status = 1 and id=" + userId;
        try (Connection conn = DBTools.openConnection(BoxCommonData.dataSourceName)) {
            List<HashMap> resList = DBSelect.getRows(sSQL, conn);
            if (resList.isEmpty()) {
                throw new Exception("Login using RSA failed: userId " + userId + " not found on this box");
            }
            String publicKey = (String) resList.get(0).get("public_key");
            BigInteger modulus = new BigInteger(publicKey, 16);
            RSAPublicKeySpec spec = new RSAPublicKeySpec(modulus, BoxCommonData.RSA_E);
            KeyFactory factory = KeyFactory.getInstance("RSA");
            PublicKey usersPublicKey = factory.generatePublic(spec);
            Signature verifier = Signature.getInstance("SHA256withRSA");
            verifier.initVerify(usersPublicKey);
            verifier.update(challenge.getBytes("UTF-8"));
            boolean valid = verifier.verify(Tools.toByteArray(digest));
            json = new JSONObject();
            if (valid) {
                session.put("UsersPublicKey", usersPublicKey);
                session.put("UserId", userId);
                session.put("Login", (String) resList.get(0).get("login"));
                json.put("user_id", userId);
                json.put("result", "success");
                System.out.println("RSA LOGIN OK! UserId=" + userId);
            } else {
                throw new Exception("Login using RSA digest check failed");
            }

        } catch (Exception e) {
            System.out.println("RSA Login error");
            json.put("result", "error");
            json.put("message", e.getMessage());
        }
        response.getWriter().print(json.toString());
    }

    private void handshakeUserSRP(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String sSQL = "select id, salt, verifier from users where status = 1 and login=?;";
        JSONObject json = (JSONObject) request.getAttribute("json");
        if (!json.has("login") || !json.has("srp_A")) {
            return;
        }
        try (Connection conn = DBTools.openConnection(BoxCommonData.dataSourceName)) {
            String login = json.getString("login");
            List params = new LinkedList();
            params.add(login);
            List<HashMap> resList = DBSelect.getRows(sSQL, params, conn);
            String salt;
            BigInteger v;
            int userId = 0;
            if (resList.size() > 0) {
                salt = (String) resList.get(0).get("salt");
                v = new BigInteger((String) resList.get(0).get("verifier"), 16);
                userId = Tools.parseInt(resList.get(0).get("id"), 0);
            } else {
                salt = ""; //если нету такого юзера в БД, нельзя это показать пытающемуся зайти. Так что на основе введённого логина генерируем "поддельную" соль и отдаём.
                BigInteger fakeSaltNum = sha256(login + FAKE_SALT_KEY);
                v = sha256(FAKE_SALT_KEY + login + FAKE_SALT_KEY);//new BigInteger(64, new Random());
                for (int i = 0; i < 16; i++) {
                    salt += SALT_ALPHABET.charAt(fakeSaltNum.mod(new BigInteger("" + SALT_ALPHABET.length())).intValue());
                    fakeSaltNum = fakeSaltNum.divide(new BigInteger("" + SALT_ALPHABET.length()));
                }
            }
            BigInteger A = new BigInteger(json.getString("srp_A"), 16);
            BigInteger b = null;
            BigInteger B = BigInteger.ZERO;
            BigInteger u = null;
            while (true) {
                b = new BigInteger(32, new Random());
                B = (K.multiply(v)).add(G.modPow(b, N));
                u = sha256(A.toString(16) + B.toString(16));

                if ((B.remainder(N).intValue() != 0) && (u.remainder(N).intValue() != 0)) {
                    break;
                }
            }
            BigInteger S = ((v.modPow(u, N)).multiply(A)).modPow(b, N);
            BigInteger M = sha256(A.toString(16) + B.toString(16) + S.toString(16));
            json = new JSONObject();
            if (A.remainder(N).intValue() == 0) {
                json.put("result", "error");
                json.put("message", "Invalid ephemeral key");
            } else {
                String session_key = AppData.sessions.createSession();
                UsersSession session = AppData.sessions.get(session_key);
                session.put("srp_S", S.toString(16));
                session.put("srp_M", M.toString(16));
                session.put("srp_A", A.toString(16));
                session.put("srp_login", login);
                session.put("Login", login);
                json.put("result", "success");
                json.put("srp_B", B.toString(16));
                json.put("salt", salt);
                json.put("box_id", BoxCommonData.BOX_ID);
                json.put("session_key", session_key);

            }

            response.getWriter().print(json.toString());

        }
    }

    private void loginUserSRP(HttpServletRequest request, HttpServletResponse response) throws Exception {
        JSONObject json = (JSONObject) request.getAttribute("json");
        UsersSession session = null;
        if (json.has("session_key")) {
            String session_key = json.getString("session_key");
            session = AppData.sessions.get(session_key);
        }
        if (session == null) {
            return;
        }
        String Astr = (String) session.remove("srp_A");
        String Mstr = (String) session.remove("srp_M");
        String Sstr = (String) session.remove("srp_S");
        String username = (String) session.remove("srp_login");

        BigInteger A = new BigInteger(Astr, 16);
        BigInteger M = new BigInteger(Mstr, 16);
        BigInteger S = new BigInteger(Sstr, 16);

        String M_client = json.getString("srp_M");
        json = new JSONObject();
        if (M_client.equals(Mstr)) {

            String sSQL = "select id, salt, verifier, public_key from users where status = 1 and login=?;";
            List params = new LinkedList();
            params.add(username);
            try (Connection conn = DBTools.openConnection(BoxCommonData.dataSourceName)) {
                List<HashMap> resList = DBSelect.getRows(sSQL, params, conn);
                if (resList.size() > 0) {
                    M = sha256(A.toString(16) + M.toString(16) + S.toString(16));
                    int userId = Tools.parseInt(resList.get(0).get("id"), 0);
                    String publicKey = (String) resList.get(0).get("public_key");
                    BigInteger modulus = new BigInteger(publicKey, 16);
                    RSAPublicKeySpec spec = new RSAPublicKeySpec(modulus, BoxCommonData.RSA_E);
                    KeyFactory factory = KeyFactory.getInstance("RSA");
                    PublicKey usersPublicKey = factory.generatePublic(spec);
                    AppData.usersPublicKeysCache.addKey(userId, (RSAPublicKey) usersPublicKey, username);
                    session.put("UsersPublicKey", usersPublicKey);
                    session.put("UserId", userId);
                    json.put("user_id", userId);
                    json.put("box_id", BoxCommonData.BOX_ID);
                    json.put("result", "success");
                    json.put("srp_M", M.toString(16));
                    System.out.println("SRP LOGIN OK! UserId=" + userId);
                }
            }
        } else {
            System.out.println("SRP Login error");
            json.put("result", "error");
            json.put("message", "Invalid login or password");
        }
        response.getWriter().print(json.toString());
    }

    private void updateUserSRP(HttpServletRequest request, HttpServletResponse response) throws Exception {
        JSONObject json = (JSONObject) request.getAttribute("json");
        UsersSession session = null;
        if (json.has("session_key")) {
            String session_key = json.getString("session_key");
            session = AppData.sessions.get(session_key);
        }
        if (session == null) {
            return;
        }
        String Astr = (String) session.remove("srp_A");
        String Mstr = (String) session.remove("srp_M");
        String Sstr = (String) session.remove("srp_S");
        String username = (String) session.remove("srp_login");

        BigInteger A = new BigInteger(Astr, 16);
        BigInteger M = new BigInteger(Mstr, 16);
        BigInteger S = new BigInteger(Sstr, 16);

        String M_client = json.getString("srp_M");
        String newLogin = json.getString("new_login");
        String newSalt = json.getString("new_salt");
        String newVerifier = json.getString("new_verifier");

        json = new JSONObject();
        if (M_client.equals(Mstr)) {
            String sSQL = "select id, public_key from users where status = 1 and login=?;";
            List params = new LinkedList();
            params.add(username);
            try (Connection conn = DBTools.openConnection(BoxCommonData.dataSourceName)) {

                List<HashMap> resList = DBSelect.getRows(sSQL, params, conn);
                if (resList.size() > 0) {
                    M = sha256(A.toString(16) + M.toString(16) + S.toString(16));
                    int userId = Tools.parseInt(resList.get(0).get("id"), 0);
                    String publicKey = (String) resList.get(0).get("public_key");
                    BigInteger modulus = new BigInteger(publicKey, 16);
                    RSAPublicKeySpec spec = new RSAPublicKeySpec(modulus, BoxCommonData.RSA_E);
                    KeyFactory factory = KeyFactory.getInstance("RSA");
                    PublicKey usersPublicKey = factory.generatePublic(spec);
                    AppData.usersPublicKeysCache.addKey(userId, (RSAPublicKey) usersPublicKey, username);
                    session.put("UsersPublicKey", usersPublicKey);
                    session.put("UserId", userId);
                    json.put("user_id", userId);
                    json.put("box_id", BoxCommonData.BOX_ID);
                    json.put("result", "success");
                    json.put("srp_M", M.toString(16));
                    System.out.println("SRP OK, updating userdata");
                    HashMap userMap = new HashMap();
                    userMap.put("id", userId);
                    userMap.put("login", newLogin);
                    userMap.put("salt", newSalt);
                    userMap.put("verifier", newVerifier);
                    userMap.put("synced", 0);
                    DBTools.updateRow("users", userMap, conn);
                    json.put("result", "success");
                    json.put("message", "User updated");
                }

            } catch (Exception e) {
                System.err.println("SRP Userupdate error");
                e.printStackTrace();
                json.put("result", "error");
                json.put("message", e.getMessage());
            }
        } else {
            System.err.println("SRP Userupdate Login error");
            json.put("result", "error");
            json.put("message", "Invalid login or password");
        }
        response.getWriter().print(json.toString());
    }

    private void doUserCommand(HttpServletRequest request, HttpServletResponse response) throws Exception {
        JSONObject json = (JSONObject) request.getAttribute("json");
        UsersSession session = null;
        if (json.has("session_key")) {
            String session_key = json.getString("session_key");
            session = AppData.sessions.get(session_key);
        }
        if (session == null) {
            return;
        }
        JSONAPI.doEncryptedUserCommand(json, BoxCommonData.PRIVATE_KEY, (RSAPublicKey) session.get("UsersPublicKey"));
        JSONObject reply = new JSONObject();
        reply.put("result", "success");
        reply.put("session_key", json.getString("session_key"));
        response.getWriter().write(reply.toString());
    }

    private void replyWithParameters(HttpServletRequest request, HttpServletResponse response) throws Exception {
        JSONObject json = (JSONObject) request.getAttribute("json");
        UsersSession session = null;
        if (json.has("session_key")) {
            String session_key = json.getString("session_key");
            session = AppData.sessions.get(session_key);
        }
        if (session == null) {
            return;
        }
        JSONObject reply = JSONAPI.getEncryptedParametersJSON((RSAPublicKey) session.get("UsersPublicKey"));
        reply.put("result", "success");
        reply.put("session_key", json.getString("session_key"));
        reply.put("connection_type", "local");
        reply.put("server_link", TunnelClientModule.getInstance().tunnelIsUp() ? "up" : "down");
        response.getWriter().write(reply.toString());
    }

    private void replyWithSettings(HttpServletRequest request, HttpServletResponse response) throws Exception {
        JSONObject json = (JSONObject) request.getAttribute("json");
        UsersSession session = null;
        if (json.has("session_key")) {
            String session_key = json.getString("session_key");
            session = AppData.sessions.get(session_key);
        }
        if (session == null) {
            return;
        }
        JSONObject reply = JSONAPI.getSettingsJSON((RSAPublicKey) session.get("UsersPublicKey"), (String) session.get("Login"));
        reply.put("result", "success");
        reply.put("session_key", json.getString("session_key"));
        response.getWriter().write(reply.toString());
    }

    private void replyWithHistory(HttpServletRequest request, HttpServletResponse response) throws Exception {
        JSONObject json = (JSONObject) request.getAttribute("json");
        UsersSession session = null;
        if (json.has("session_key")) {
            String session_key = json.getString("session_key");
            session = AppData.sessions.get(session_key);
        }
        if (session == null) {
            return;
        }

        JSONObject reply = JSONAPI.getEncryptedHistoryJSON(json, (RSAPublicKey) session.get("UsersPublicKey"));
        reply.put("result", "success");
        reply.put("session_key", json.getString("session_key"));
        response.getWriter().write(reply.toString());
    }

    private void replyWithLog(HttpServletRequest request, HttpServletResponse response) throws Exception {
        JSONObject json = (JSONObject) request.getAttribute("json");
        UsersSession session = null;
        if (json.has("session_key")) {
            String session_key = json.getString("session_key");
            session = AppData.sessions.get(session_key);
        }
        if (session == null) {
            return;
        }

        JSONObject reply = JSONAPI.getEncryptedLogJSON(json, (RSAPublicKey) session.get("UsersPublicKey"));
        reply.put("result", "success");
        reply.put("session_key", json.getString("session_key"));
        response.getWriter().write(reply.toString());
    }

    private void replyWithCapture(HttpServletRequest request, HttpServletResponse response) throws Exception {
        JSONObject json = (JSONObject) request.getAttribute("json");
        UsersSession session = null;
        if (json.has("session_key")) {
            String session_key = json.getString("session_key");
            session = AppData.sessions.get(session_key);
        }
        if (session == null) {
            return;
        }

        Event event = new Event("get_capture", null, Event.Type.USER_ACTION);
        AppData.eventManager.lockForEvent(event, this);

        JSONObject reply = JSONAPI.getEncryptedCaptureJSON((RSAPublicKey) session.get("UsersPublicKey"));
        reply.put("result", "success");
        reply.put("session_key", json.getString("session_key"));
        response.getWriter().write(reply.toString());
    }

    private void doRequestLogin(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String sSQL = "select id from users where status = 1;";
        JSONObject json = new JSONObject();
        json.put("box_id", BoxCommonData.BOX_ID);
        try (Connection conn = DBTools.openConnection(BoxCommonData.dataSourceName)) {
            List<HashMap> resList = DBSelect.getRows(sSQL, conn);
            if (resList.size() > 0) {
                String session_key = AppData.sessions.createSession();
                UsersSession session = AppData.sessions.get(session_key);
                String challenge = new BigInteger(32, new Random()).toString(16);
                session.put("challenge", challenge);
                json.put("result", "do_login");
                json.put("challenge", challenge);
                json.put("session_key", session_key);
                json.put("box_id", BoxCommonData.BOX_ID);
                json.put("message", "user exists, please login");
            } else {
                json.put("result", "do_register");
                json.put("message", "no users, please register");
            }
            response.getWriter().print(json.toString());
        }
    }

    private BigInteger sha256(String s) throws Exception {
        MessageDigest sha = MessageDigest.getInstance("SHA-256");
        byte[] bytes = s.getBytes();
        sha.update(bytes, 0, bytes.length);
        return new BigInteger(1, sha.digest());
    }
}
