
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ page import="java.util.*"%>
<%@page import="org.lobzik.home_sapiens.pi.event.Event"%>
<%@ page import="org.lobzik.home_sapiens.pi.*"%>
<%@ page import="org.lobzik.home_sapiens.entity.*"%>
<h2>Тесты - этап 3</h2>
<br>
Убедитесь, что команды включения и выключения устройств успешно выполняются.
<br>

<%
    if (request.getMethod().equalsIgnoreCase("POST")) {
        Map<String, String[]> pars = request.getParameterMap();
        for (String command : pars.keySet()) {
            if (command.endsWith("=")) {
                String val = pars.get(command)[0];
                HashMap data = new HashMap();
                data.put("uart_command", command + val);
                Event e = new Event("internal_uart_command", data, Event.Type.USER_ACTION);
                AppData.eventManager.newEvent(e);
            }
        }
    }
%>
<form action="" method="POST">
    <table>
        <thead>
        <th>ВКЛ</th><th>Устройство</th><th>ВЫКЛ</th>
        </thead>
        <tr>
            <td><input type="submit" name="relay1=" value="on"></td>
            <td>Реле 1</td>
            <td><input type="submit" name="relay1=" value="off"></td>
        </tr>
        <tr>
            <td><input type="submit" name="relay2=" value="on"></td>
            <td>Реле 2</td>
            <td><input type="submit" name="relay2=" value="off"></td>
        </tr>
        <tr>
            <td><input type="submit" name="relay3=" value="on"></td>
            <td>Реле 3</td>
            <td><input type="submit" name="relay3=" value="off"></td>
        </tr>
        <tr>
            <td><input type="submit" name="relay4=" value="on"></td>
            <td>Реле 4</td>
            <td><input type="submit" name="relay4=" value="off"></td>
        </tr>
        <tr>
            <td><input type="submit" name="led1=" value="on"></td>
            <td>Диод 1</td>
            <td><input type="submit" name="led1=" value="off"></td>
        </tr>
        <tr>
            <td><input type="submit" name="led2=" value="on"></td>
            <td>Диод 2</td>
            <td><input type="submit" name="led2=" value="off"></td>
        </tr>
        <tr>
            <td><input type="submit" name="433_TX=" value="<%=BoxSettingsAPI.get("Lamp1OnCommand433")%>"></td>
            <td>Лампа 433 1</td>
            <td><input type="submit" name="433_TX=" value="<%=BoxSettingsAPI.get("Lamp1OffCommand433")%>"></td>
        </tr>
        <tr>
            <td><input type="submit" name="433_TX=" value="<%=BoxSettingsAPI.get("Lamp2OnCommand433")%>"></td>
            <td>Лампа 433 2</td>
            <td><input type="submit" name="433_TX=" value="<%=BoxSettingsAPI.get("Lamp2OffCommand433")%>"></td>
        </tr>
        <tr>
            <td><input type="submit" name="433_TX=" value="<%=BoxSettingsAPI.get("Socket1OnCommand433")%>"></td>
            <td>Розетка 433</td>
            <td><input type="submit" name="433_TX=" value="<%=BoxSettingsAPI.get("Socket1OffCommand433")%>"></td>
        </tr>
        <tr>
            <td><input type="submit" name="charge=" value="on"></td>
            <td>Зарядка АКБ</td>
            <td><input type="submit" name="charge=" value="off"></td>
        </tr>   
    </table>
</form>
<br>
<br>
Также проверьте работоспособность WiFi (наличие SSID HomeSapiens в эфире), разъём USB, убедитесь, что дисплей работае, отключите сетевое питание ненадолго для проверки резервных АКБ <br>
Если всё в порядке - можно переходить к регистрации устройства!<br>
<form action="" method="post">
    <input type="hidden" name="new_stage" value="new_stage"/>
    <input type="submit" value="Назад" name="back" />  Комментарий: <input type="text" name="comment" /> <input type="submit" value="Регистрация" name="forward" /> 
</form> <br>