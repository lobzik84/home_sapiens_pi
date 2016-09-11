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
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.json.JSONObject;
import org.lobzik.home_sapiens.entity.Measurement;
import org.lobzik.home_sapiens.entity.Parameter;

/**
 *
 * @author lobzik
 */
@WebServlet(name = "JsonServlet", urlPatterns = {"/json", "/json/*"})
public class JsonServlet extends HttpServlet {

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        HttpSession session = request.getSession();
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
                JSONObject json = new JSONObject(requestString); //user actions
            }
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

        } catch (Throwable e) {
            e.printStackTrace();
            response.getWriter().print("{\"result\":\"error\"}");
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

}
