
<%@page import="java.io.File"%>
<%@page import="org.lobzik.tools.Tools"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ page import="java.util.*"%>
<%@ page import="org.lobzik.home_sapiens.pi.*"%>
<%@ page import="org.lobzik.home_sapiens.entity.*"%>
<script>
setTimeout(function(){
   window.location.reload(1);
}, 3000);    
</script>
    

<h2>Тесты - этап 2</h2>
<% 
String rtcTime = Tools.sysExec("sudo hwclock -r", new File("/"));
%>
<br>
Проверьте время RTC: <%=rtcTime%>
<br>
Проверьте бинарные датчики (обновляются раз в 3 секунды):
<br>

<%
    ParametersStorage ps = AppData.parametersStorage;
    MeasurementsCache mc = AppData.measurementsCache;

    for (Integer pId : ps.getParameterIds()) {
        Parameter p = ps.getParameter(pId);
        if (p.getType() != Parameter.Type.BOOLEAN) {
            continue;
        }
        Measurement m = mc.getLastMeasurement(p);
        if (m == null) {
            %><font color="red"><%=p.getName()%>:  NULL !!! </font><%
        } else if (m.getBooleanValue()){
            %><font color="green"><%=p.getName()%>:  TRUE </font> <%                
        } else if (!m.getBooleanValue()) {
            %><font color="black"><%=p.getName()%>:  FALSE </font> <%   
        }
%><br> <%}%>
<br>
<br>
Если всё в порядке, можно двигаться дальше! <br><br>
 <form action="" method="post">
          <input type="hidden" name="new_stage" value="new_stage"/>
      <input type="submit" value="Назад" name="back" />  Комментарий: <input type="text" name="comment" /> <input type="submit" value="Вперёд" name="forward" /> 
</form> <br>