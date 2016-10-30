<%@page import="java.io.File"%>
<%@page import="org.lobzik.tools.db.mysql.DBTools"%>
<%@page import="java.sql.Connection"%>
<%@page import="org.lobzik.tools.Tools"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="org.lobzik.home_sapiens.pi.modules.VideoModule"%>
<%@page import="java.util.*"%>
<%@page import="org.lobzik.home_sapiens.pi.*"%>
<%@page import="org.lobzik.home_sapiens.entity.*"%>
<h2>Тесты - этап 1</h2>
Чтобы проверить raspberry в целом, получение даты, взаимодействие с arduino, датчики влажности, температуры, напряжения и температуры АКБ, сетевого напряжения, горючих газов, модем, камеры - <br>
убедитесь в адекватности отображаемых параметров!
<br>
Затем проведите калибровку датчиков.
<br>
<br>
<%
    String hwclockOutput = "";
    if (request.getMethod().equalsIgnoreCase("POST")) {

        String save_settings = request.getParameter("save_settings");
        if (save_settings != null && save_settings.length() > 0) {
            BoxSettingsAPI.set(request.getParameterMap());
        }
        
                String rtc_write = request.getParameter("rtc_write");
        if (rtc_write != null && rtc_write.length() > 0) {
            hwclockOutput = Tools.sysExec("sudo hwclock -w", new File("/"));
        }

    }
    
    

    Date date = new Date();
    Calendar c = new GregorianCalendar();
    c.setTime(date);
    if (c.get(Calendar.YEAR) < 2016) {
%><font color="red"> Дата: <%=date%></font> <br><%
} else {
%>Дата: <%=date%> <br> 
<form action="" method="post">
    <input type="submit" name="rtc_write" value="Сохранить в RTC"/>
</form>
<%=hwclockOutput%><br><br>
<%
    }

    ParametersStorage ps = AppData.parametersStorage;
    MeasurementsCache mc = AppData.measurementsCache;

    String calibrate = request.getParameter("calibrate");
    if (calibrate != null && calibrate.length() > 0) {
        double actual = Tools.parseDouble(request.getParameter("VAC_SENSOR"), 0);
        double last = 0;
        Parameter pVac = ps.getParameter(ps.resolveAlias("VAC_SENSOR"));
        if (mc.getLastMeasurement(pVac) != null) {
            last = mc.getLastMeasurement(pVac).getDoubleValue();
        }
        if (actual > 0 && last > 0) {
            double calibration = actual / last;
            pVac.setCalibration(calibration);
            HashMap dataMap = new HashMap();
            dataMap.put("id", pVac.getId());
            dataMap.put("calibration", calibration);
            Connection conn = DBTools.openConnection(BoxCommonData.dataSourceName);
            DBTools.updateRow("parameters", dataMap, conn);
            conn.close();
        }

        actual = Tools.parseDouble(request.getParameter("VBAT_SENSOR"), 0);
        last = 0;
        pVac = ps.getParameter(ps.resolveAlias("VBAT_SENSOR"));
        if (mc.getLastMeasurement(pVac) != null) {
            last = mc.getLastMeasurement(pVac).getDoubleValue();
        }
        if (actual > 0 && last > 0) {
            double calibration = actual / last;
            pVac.setCalibration(calibration);
            HashMap dataMap = new HashMap();
            dataMap.put("id", pVac.getId());
            dataMap.put("calibration", calibration);
            Connection conn = DBTools.openConnection(BoxCommonData.dataSourceName);
            DBTools.updateRow("parameters", dataMap, conn);
            conn.close();
        }

    }

    for (Integer pId : ps.getParameterIds()) {
        Parameter p = ps.getParameter(pId);
        if (p.getAlias().equals("INTERNAL_TEMP")
                || p.getAlias().equals("INTERNAL_HUMIDITY")
                || p.getAlias().equals("LUMIOSITY")
                || p.getAlias().equals("VBAT_SENSOR")
                || p.getAlias().equals("VAC_SENSOR")
                || p.getAlias().equals("BATT_TEMP")
                || p.getAlias().equals("MODEM_RSSI")
                || p.getAlias().equals("GAS_SENSOR_ANALOG")) {
            if (mc.getLastMeasurement(p) == null || mc.getLastMeasurement(p).toStringValue().length() == 0) {
%><font color="red">
<%=p.getName()%>: Отсутствует!</font> <br> <%
    continue;

} else {%><%=p.getName()%>: <%=mc.getLastMeasurement(p).toStringValue()%> <%=(p.getUnit() != null) ? p.getUnit() : ""%><br> <%}%>
<%}
    }%>
<br> 
<br>
<form action="" method="post" >
    <table>
        <tbody>
            <tr>
                <td>Розетка</td>
                <td> 433 команда ВКЛ <input type="text" name="Socket1OnCommand433" value="<%=BoxSettingsAPI.get("Socket1OnCommand433")%>" /> </td> 
                <td> 433 команда ВЫКЛ <input type="text" name="Socket1OffCommand433" value="<%=BoxSettingsAPI.get("Socket1OffCommand433")%>" /> </td> 
            </tr> 
            <tr>
                <td>Лампа 1</td>
                <td> 433 команда ВКЛ <input type="text" name="Lamp1OnCommand433" value="<%=BoxSettingsAPI.get("Lamp1OnCommand433")%>" /> </td> 
                <td> 433 команда ВЫКЛ <input type="text" name="Lamp1OffCommand433" value="<%=BoxSettingsAPI.get("Lamp1OffCommand433")%>" /> </td> 
            </tr>
            <tr>
                <td>Лампа 2</td>
                <td> 433 команда ВКЛ <input type="text" name="Lamp2OnCommand433" value="<%=BoxSettingsAPI.get("Lamp2OnCommand433")%>" /> </td> 
                <td> 433 команда ВЫКЛ <input type="text" name="Lamp2OffCommand433" value="<%=BoxSettingsAPI.get("Lamp2OffCommand433")%>" /> </td> 
            </tr>
            <tr>
                <td>Датчик двери</td>
                <td> 433 адрес <input type="text" name="DoorSensorAddress433" value="<%=BoxSettingsAPI.get("DoorSensorAddress433")%>" /> </td> 
                <td></td> 
            </tr>
            <tr>
                <td>Датчик потопа</td>
                <td> 433 адрес <input type="text" name="WetSensorAddress433" value="<%=BoxSettingsAPI.get("WetSensorAddress433")%>" /> </td> 
                <td>  </td> 
            </tr>
        </tbody>
    </table>
    <input type="submit" name="save_settings" value="Сохранить настройки"/>
</form>        
<br>
<form action="" method="post">
    Введите текущее напряжение на АКБ <input type="text" name="VBAT_SENSOR"/> В<br>
    Введите текущее напряжение в сети <input type="text" name="VAC_SENSOR"/> В<br>
    <input type="submit" name="calibrate" value="Калибровать"/>
</form>
<br>
<br>
<%
    for (String capture : VideoModule.IMAGE_FILES) {
%><%=capture%>
<img src="<%=request.getContextPath()%>/capture/<%=capture%>" /> 
<%
    }

%>
<br>
<br>
Если всё в порядке, можно двигаться дальше! <br>
Устройство будет выключено. Дождитесь белого экрана и выключите устройство тумблером "питание" (на задней стенке), подождите 30 секунд и включите снова - таким образом проверим RTC
<br><br>
<form action="" method="post">
    <input type="hidden" name="new_stage" value="new_stage"/>
    Комментарий: <input type="text" name="comment" width="120"/> <input type="submit" value="Вперёд!" name="forward" /> 
</form> <br>