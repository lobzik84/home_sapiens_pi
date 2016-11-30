<%@page import="org.lobzik.home_sapiens.pi.behavior.Notification"%>
<%@page import="org.lobzik.tools.Tools"%>
<%@ page contentType="text/html" pageEncoding="UTF-8"%>
<%@ page import="java.util.*"%>
<%@ page import="org.lobzik.home_sapiens.pi.*"%><%

    int id = Tools.parseInt(request.getParameter("id"), 0);
    Notification n = AppData.emailNotification.get(id);
    if (n == null) return;
    //AppData.emailNotification.remove(id);
%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Upravdom notifiies</title>
    </head>
    <body>
        <h1>Управдом уведомляет!!</h1>
        <%=n.conditionAlias%>  
        <%=n.text%>
        <%=Tools.getFormatedDate(n.startDate)%>
        <%=n.severity.toString()%>

    </body>
</html>
