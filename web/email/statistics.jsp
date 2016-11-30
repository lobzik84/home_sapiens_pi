<%@page import="org.lobzik.tools.db.mysql.DBSelect"%>
<%@page import="org.lobzik.tools.db.mysql.DBTools"%>
<%@page import="java.sql.Connection"%>
<%@page import="org.lobzik.tools.Tools"%>
<%@ page contentType="text/html" pageEncoding="UTF-8"%>
<%@ page import="java.util.*"%>
<%@ page import="org.lobzik.home_sapiens.pi.*"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>JSP Page</title>
    </head>
    <body>
        <h1>Hello World!</h1>

        Here goes some statistics
        <table>
            <%

                long from = Tools.parseLong(request.getParameter("from"), 0L);
                long to = Tools.parseLong(request.getParameter("to"), System.currentTimeMillis());
                String moduleName = request.getParameter("moduleName");

                String severity = "";
                if (request.getParameter("severity") != null) {
                    severity = request.getParameter("severity");
                }
                List argsList = new LinkedList();
                Connection conn = DBTools.openConnection(BoxCommonData.dataSourceName);
                try {
                    String sSQL = "select * from logs l where 1=1 \n"
                            + " and unix_timestamp(l.dated) > " + from / 1000 + " \n"
                            + " and unix_timestamp(l.dated) < " + to / 1000 + " \n";
                    if (moduleName != null && moduleName.length() > 0 && !moduleName.equals("*")) {
                        sSQL += " and l.module_name = ? \n";
                        argsList.add("?");
                    }
                    if (severity.equals("ALARM")) {
                        sSQL += " and l.level in ('FATAL') \n";
                    } else if (severity.equals("ALERT")) {
                        sSQL += " and l.level in ('FATAL', 'ERROR') \n";
                    } else if (severity.equals("OK")) {
                        sSQL += " and l.level in ('FATAL', 'ERROR', 'WARN') \n";
                    } else if (severity.equals("INFO")) {
                        sSQL += " and l.level in ('FATAL', 'ERROR', 'WARN', 'INFO') \n";
                    }

                    sSQL += " order by l.dated desc limit 100;";
                    List<HashMap> logs = DBSelect.getRows(sSQL, argsList, conn);
                    for (int i = 0; i < logs.size(); i++) {
                        String level = "INFO";
                        HashMap h = logs.get(i);
                        String levelF = (String) h.get("level");
                        if (levelF != null) {

                            if (levelF.equals("FATAL")) {
                                level = "ALARM";
                            } else if (levelF.equals("ERROR")) {
                                level = "ALERT";
                            } else if (levelF.equals("WARN")) {
                                level = "OK";
                            } else {
                                level = "INFO";
                            }

                        }
                        String date = Tools.getFormatedDate((Date) h.get("dated"), "YYYY-MM-dd HH:mm");
                        String message = (String) h.get("message");
                        String alias = "DEFAULT";
                        if (message.startsWith("ALIAS:")) {
                            message = message.substring(6);
                            int in = message.indexOf(": ");
                            if (in > 0) {
                                alias = message.substring(0, in);
                                message = message.substring(in + 2);
                            }
                        }
            %><tr>
                <td><%=date%></td>
                <td><%=alias%></td>
                <td><%=level%></td>
                <td><%=message%></td>
            </tr>
            <%
                }

            } catch (Exception e) {

            %><td>Ошибка сбора статистики! <%=e.getClass() + ":" + e.getMessage()%></td> <%
                } finally {
                    conn.close();
                }
            %>

        </table>

    </body>
</html>
