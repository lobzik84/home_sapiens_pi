/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lobzik.home_sapiens.pi;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.sql.Connection;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.json.JSONObject;
import org.lobzik.home_sapiens.entity.Measurement;
import org.lobzik.home_sapiens.entity.Parameter;
import org.lobzik.tools.Tools;
import org.lobzik.tools.db.mysql.DBSelect;
import org.lobzik.tools.db.mysql.DBTools;

/**
 *
 * @author lobzik
 */
@WebServlet(name = "JsonServlet", urlPatterns = {"/json", "/json/*"})
public class JsonServlet extends HttpServlet {
    
    	private static final BigInteger g = new BigInteger("2");
	private static final BigInteger N = new BigInteger("115b8b692e0e045692cf280b436735c77a5a9e8a9e7ed56c965f87db5b2a2ece3", 16);
	private static final BigInteger k = new BigInteger("c46d46600d87fef149bd79b81119842f3c20241fda67d06ef412d8f6d9479c58", 16);
	private static final String salt_alphabet = "1234567890abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
	private static final String fake_salt_key = "mri9gjn0990)M09V^&DF&*GR^%^WTioh89t;";

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
                JSONObject json = new JSONObject(requestString); //user actions.
                request.setAttribute("json", json);
                String session_key = null;
                if (json.has("session_key")) session_key = json.getString("session_key");
                int userId = Tools.parseInt(AppData.userSessions.get(session_key), 0);
                String action = json.getString("action");
                switch (action) {
                    case "register":
                        registerUser(request, response);
                        uploadUserToServer(request, response);
                        break;

                    case "kf_upload":
                        updateKeyFile(userId, request, response);
                        break;

                    case "kf_download":
                        downloadKeyFile(request, response);
                        break;

                    case "login_srp":
                        loginUserSRP(request, response);
                        break;

                    case "login_rsa":
                        loginUserRSA(request, response);
                        break;

                    case "command":
                        if (userId > 0) {
                            doUserCommand(request, response);
                            replyWithParameters(request, response);

                        }
                        break;

                    default:
                        //if (userId > 0)
                        replyWithParameters(request, response);

                }
            } else //if (userId > 0)
            {
                replyWithParameters(request, response); //??  
            }
        } catch (Throwable e) {
            e.printStackTrace();
            response.getWriter().print("{\"result\":\"error\",\"message\":\"" + e.getMessage() +"\"}");
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
               // throw new Exception("User registered already, please login");
            }
            HashMap newUser = new HashMap();
            for (String key : json.keySet()) {
                newUser.put(key, json.get(key));
            }
            newUser.put("status", 1);
            int newUserId = DBTools.insertRow("users", newUser, conn);
            BigInteger b = new BigInteger(32, new Random());
            String session_key = b.toString(16);
            AppData.userSessions.put(session_key, newUserId);
            json = new JSONObject();
            json.put("result", "success");
            json.put("new_user_id", newUserId);
            json.put("box_public_key", BoxCommonData.PUBLIC_KEY);
            json.put("box_id", BoxCommonData.BOX_ID);
            json.put("session_key", session_key);                 
            response.getWriter().print(json.toString());
            
        }
    }

    private void uploadUserToServer(HttpServletRequest request, HttpServletResponse response) throws Exception {
        //create userdata with box_id, sign it with RSA and upload to server
        //on server side - check signature and insert to users_db
    }

    private void updateKeyFile(int userId, HttpServletRequest request, HttpServletResponse response) throws Exception {
        if (userId <= 0) throw new Exception("Trying to upload unauthorized keyfile!");
        try (Connection conn = DBTools.openConnection(BoxCommonData.dataSourceName)) {
            HashMap dataMap = new HashMap();
            JSONObject json = (JSONObject) request.getAttribute("json");
            dataMap.put("id", userId);
            dataMap.put("keyfile", json.getString("kfCipher"));
            DBTools.updateRow("users", dataMap, conn);
            json = new JSONObject();
            json.put("result", "success");       
            response.getWriter().print(json.toString());
        }
    }

    private void downloadKeyFile(HttpServletRequest request, HttpServletResponse response) throws Exception {

    }

    private void loginUserRSA(HttpServletRequest request, HttpServletResponse response) throws Exception {

    }

    private void loginUserSRP(HttpServletRequest request, HttpServletResponse response) throws Exception {

    }

    private void doUserCommand(HttpServletRequest request, HttpServletResponse response) throws Exception {

    }

    private void replyWithParameters(HttpServletRequest request, HttpServletResponse response) throws Exception {

        JSONObject paramsJson = new JSONObject();
        ParametersStorage ps = AppData.parametersStorage;
        MeasurementsCache mc = AppData.measurementsCache;
        for (Integer pId : ps.getParameterIds()) {
            Parameter p = ps.getParameter(pId);
            if (mc.getLastMeasurement(p) == null) {
                continue;
            }
            JSONObject parJson = new JSONObject();
            parJson.put("par_name", p.getName());
            parJson.put("par_unit", p.getUnit());
            Measurement m = mc.getLastMeasurement(p);
            parJson.put("last_value", m.toStringValue());
            parJson.put("last_date", m.getTime());
            paramsJson.put(pId + "", parJson);
        }
        JSONObject reply = new JSONObject();
        reply.put("parameters", paramsJson);
        response.getWriter().write(reply.toString());
    }

}
