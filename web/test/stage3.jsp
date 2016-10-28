
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ page import="java.util.*"%>
<%@ page import="org.lobzik.home_sapiens.pi.*"%>
<%@ page import="org.lobzik.home_sapiens.entity.*"%>
<h2>Тесты - этап 3</h2>
<br>
Проверьте актуальность параметров!
<br>

<%
    ParametersStorage ps = AppData.parametersStorage;
    MeasurementsCache mc = AppData.measurementsCache;

    for (Integer pId : ps.getParameterIds()) {
        Parameter p = ps.getParameter(pId);
        if (mc.getLastMeasurement(p) == null) {
            continue;
        }
%>
<%=p.getName()%>: <%=mc.getLastMeasurement(p).toStringValue()%> <%=p.getUnit()%><br>
<%}%>
<br>
<br>
Если всё в порядке, можно двигаться дальше! <br><br>
 <form action="" method="post">
          <input type="hidden" name="new_stage" value="new_stage"/>
      <input type="submit" value="Назад" name="back" />  Комментарий: <input type="text" name="comment" /> <input type="submit" value="Вперёд" name="forward" /> 
</form> <br>