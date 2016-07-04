/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lobzik.home_sapiens.pi;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.util.HashMap;
import java.util.List;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.lobzik.home_sapiens.entity.Measurement;
import org.lobzik.home_sapiens.pi.modules.InternalSensorsModule;
import org.lobzik.tools.db.mysql.DBSelect;
import org.lobzik.tools.db.mysql.DBTools;

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

        Connection conn = null;
        try {
            String sSQL = "select max(param1) param1, max(param2) param2, max(param3) param3, max(param4) param4, ssd.fdate \n"
                    + "from\n"
                    + "(select \n"
                    + "(case when sd.parameter_id = 1 then sd.value_d else null end) as param1,\n"
                    + "(case when sd.parameter_id = 2 then sd.value_d else null end) as param2,\n"
                    + "(case when sd.parameter_id = 3 then sd.value_d else null end) as param3,\n"
                    + "(case when sd.parameter_id = 4 then sd.value_d else null end) as param4,\n"
                    + "DATE_FORMAT(sd.date,'%Y-%m-%d %H:%i:%s') as fdate\n"
                    + "FROM sensors_data sd\n"
                    + "WHERE TO_DAYS(NOW()) - TO_DAYS(sd.date) <= 1\n"
                    + ") ssd\n"
                    + "group by ssd.fdate\n"
                    + "order by ssd.fdate desc";
            conn = DBTools.openConnection(BoxCommonData.dataSourceName);
            List<HashMap> resList = DBSelect.getRows(sSQL, conn);
            HashMap<String, Object> jspData = new HashMap();
            jspData.put("resList", resList);
            RequestDispatcher disp = request.getSession().getServletContext().getRequestDispatcher("/temperatures.jsp");
		    request.setAttribute("JspData", jspData);
		    disp.include(request, response);
            

        } catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            DBTools.closeConnection(conn);
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
