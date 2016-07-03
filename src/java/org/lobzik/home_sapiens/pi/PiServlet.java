/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lobzik.home_sapiens.pi;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.lobzik.home_sapiens.entity.Measurement;
import org.lobzik.home_sapiens.pi.modules.InternalSensorsModule;

/**
 *
 * @author lobzik
 */
@WebServlet(name = "PiServlet", urlPatterns = {"/hs", "/hs/*"})
public class PiServlet extends HttpServlet {

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
        PrintWriter out = response.getWriter();
        try {
            /* TODO output your page here. You may use following sample code. */
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Servlet PiServlet</title>");            
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>1-wire temps </h1><br>");
            out.println("<table><tr>");
            out.println("<td>Internal</td><td>Room</td><td>Left AC</td><td>Right AC</td>");
            out.println("</tr>");
            for (int i=0; i <= InternalSensorsModule.HISTORY_SIZE; i++) {
                out.println("<tr>");
                out.print("<td><b>");
                if (i < InternalSensorsModule.internalTemps.size()) 
                    out.print(InternalSensorsModule.internalTemps.get(i).toString() + " °C");
                out.println("</td></b>");
                out.print("<td><b>");
                if (i < InternalSensorsModule.roomTemps.size()) 
                    out.print(InternalSensorsModule.roomTemps.get(i).toString() + " °C");
                out.println("</td></b>");
                out.print("<td><b>");
                if (i < InternalSensorsModule.leftACTemps.size()) 
                    out.print(InternalSensorsModule.leftACTemps.get(i).toString()  + " °C");
                out.println("</td></b>");
                out.print("<td><b>");
                if (i < InternalSensorsModule.rightACTemps.size()) 
                    out.print(InternalSensorsModule.rightACTemps.get(i).toString() + " °C");
                out.println("</td></b>");
                out.println("</tr>");
            }

            out.println("</table>");
            out.println("</body>");
            out.println("</html>");
        } finally {
            out.close();
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
