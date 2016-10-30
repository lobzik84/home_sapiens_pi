
<%@page import="java.io.File"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ page import="java.util.*"%>
<%@page import="org.lobzik.tools.Tools"%>
<%@ page import="org.lobzik.home_sapiens.pi.*"%>
<%@ page import="org.lobzik.home_sapiens.entity.*"%>
<h2>Тесты - этап 4</h2>
<br>
Проводим генерацию ключей и регистрацию устройства на сервере
<br>
<%
    if (BoxCommonData.BOX_ID > 0) {
%> Устройство уже зарегистрировано!! <br><%
    return;
} else {

    String do_register = request.getParameter("do_register");
    if (do_register != null && do_register.length() > 0) {
        String output = BoxRegistrationAPI.doRegister();
        output = output.replaceAll("\n", "<br>");

%>
<br>
Устройство зарегистрировано на сервере!
<br>
<%=output%>
<%
} else {

    String output = Tools.sysExec("sudo bash /root/reg", new File("/"));
    output = output.replaceAll("\n", "<br>");

%>
<br>
Проведена генерация ключей:
<br>
<%=output%>
<br>
<br>
<form action="" method="POST">
    <input type="submit" name="do_register" value="Регистрироваться!">
</form>
<br> <%}%>
Если всё в порядке, печатайте наклейку с параметрами, клейте на устройство и нажимайте Вперёд. Устройство будет отключено и при следующем включении готово к работе!<br><br>
<form action="" method="post">
    <input type="hidden" name="new_stage" value="new_stage"/>
    <input type="submit" value="Назад" name="back" />  Комментарий: <input type="text" name="comment" /> <input type="submit" value="Вперёд" name="forward" /> 
</form> <br>
<%}%>