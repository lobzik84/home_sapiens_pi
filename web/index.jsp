<%-- 
    Document   : index
    Created on : Jul 29, 2016, 4:06:57 PM
    Author     : lobzik
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ page import="java.util.*"%>
<%@ page import="org.lobzik.home_sapiens.pi.*"%>
<%@ page import="org.lobzik.home_sapiens.entity.*"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>JSP Page</title>
    </head>
    <body>
        <h1>Hello World!</h1>
        
<% 
ParametersStorage ps = AppData.parametersStorage;
MeasurementsCache mc = AppData.measurementsCache;

for (Integer pId: ps.getParameterIds()) {
    Parameter p = ps.getParameter(pId);
    if (mc.getLastMeasurement(p) == null) continue;
%>
        <%=p.getName()%>: <%=mc.getLastMeasurement(p).toStringValue()%> <%=p.getUnit()%><br>
<%}%>

    </body>
</html>
