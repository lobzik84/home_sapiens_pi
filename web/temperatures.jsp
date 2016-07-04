<%-- 
    Document   : temperatures.jsp
    Created on : Jul 4, 2016, 4:11:32 PM
    Author     : lobzik
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<jsp:useBean id="JspData"  class="java.util.HashMap" scope="request" />
<%@ page import="java.util.*"%>
<% 
   List<HashMap> resList = ( List<HashMap>) JspData.get("resList");
 %>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Home Sapiens monitoring</title>
    </head>
    <body>
        <h1>1-wire temperatures!</h1>
        <br>
        <table border=1>
            <thead>
                <th>Time</th>
                <th>Internal</th>
                <th>Room</th>
                <th>Left A/C</th>
                <th>Right A/C</th>
            </thead>    
<% for (HashMap h: resList)
{
%><tr>
        <td><%=h.get("fdate")%></td>
        <td><% if (h.get("param1") != null) {%><b><%=h.get("param1")%> °C</b><%} else {%> --- <%}%></td>
        <td><% if (h.get("param2") != null) {%><b><%=h.get("param2")%> °C</b><%} else {%> --- <%}%></td>
        <td><% if (h.get("param3") != null) {%><b><%=h.get("param3")%> °C</b><%} else {%> --- <%}%></td>
        <td><% if (h.get("param4") != null) {%><b><%=h.get("param4")%> °C</b><%} else {%> --- <%}%></td>
    </tr>    
<% } %>        
        </table>
    </body>
</html>
