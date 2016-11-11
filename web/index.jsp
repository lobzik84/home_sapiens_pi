<%@page import="java.io.File"%>
<%@page import="org.lobzik.tools.Tools"%>
<%@page import="java.sql.Connection"%>
<%@page import="org.lobzik.tools.db.mysql.DBTools"%>
<%@page import="org.lobzik.home_sapiens.pi.modules.VideoModule"%>
<%@page import="org.lobzik.home_sapiens.pi.event.Event"%>
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

        <br>
        <%
            request.setCharacterEncoding("UTF-8");
            if (BoxCommonData.TEST_MODE) {
                if (request.getMethod().equalsIgnoreCase("POST")) {

                    String new_stage = request.getParameter("new_stage");
                    if (new_stage != null && new_stage.length() > 1) {
                        String message = request.getParameter("comment");
                        String forward = request.getParameter("forward");
                        String back = request.getParameter("back");
                        HashMap dataMap = new HashMap();

                        dataMap.put("message", message);
                        if (forward != null && forward.length() > 0) {
                            AppData.testStage++;
                            Connection conn = DBTools.openConnection(BoxCommonData.dataSourceName);
                            dataMap.put("stage", AppData.testStage);
                            DBTools.insertRow("test_stages", dataMap, conn);
                            conn.close();
                            if (AppData.testStage == 2 || AppData.testStage == 5) {
                                //после первого и 4го этапа отрубаем питание
                                Tools.sysExec("sudo halt -p", new File("/"));
                            }

                        } else if (back != null && back.length() > 0) {
                            AppData.testStage--;
                            Connection conn = DBTools.openConnection(BoxCommonData.dataSourceName);
                            dataMap.put("stage", AppData.testStage);
                            DBTools.insertRow("test_stages", dataMap, conn);
                            conn.close();
                        }
                    }
                }
                switch (AppData.testStage) {
                    case 1:
        %>   <jsp:include page="test/stage1.jsp" /><%
                break;
            case 2:
        %>   <jsp:include page="test/stage2.jsp" /><%
                break;
            case 3:
        %>   <jsp:include page="test/stage3.jsp" /><%
                break;
            case 4:
        %>   <jsp:include page="test/stage4.jsp" /><%
                break;
            case 5:
        %>   <jsp:include page="test/stage5.jsp" /><%
                    break;
            }

        } else {

            if (request.getMethod().equalsIgnoreCase("POST")) {
                String command = request.getParameter("command");
                String event = request.getParameter("event");
                String sound = request.getParameter("sound");
                String image = request.getParameter("image");
                String sms = request.getParameter("sms");
                String recipient = request.getParameter("recipient");
                String subject = request.getParameter("subject");
                String mail = request.getParameter("mail");
                String system_event = request.getParameter("system_event");
                if (command != null && command.length() > 0) {
                    HashMap data = new HashMap();
                    data.put("uart_command", command);
                    Event e = new Event("internal_uart_command", data, Event.Type.USER_ACTION);
                    AppData.eventManager.newEvent(e);
                }
                if (mail != null && mail.length() > 0) {
                    HashMap data = new HashMap();
                    data.put("mail_text", mail);
                    data.put("mail_to", recipient);
                    data.put("mail_subject", subject);
                    Event e = new Event("send_email", data, Event.Type.REACTION_EVENT);
                    AppData.eventManager.newEvent(e);
                }
                if (sms != null && sms.length() > 0) {
                    HashMap data = new HashMap();
                    data.put("message", sms);
                    data.put("recipient", recipient);
                    Event e = new Event("send_sms", data, Event.Type.USER_ACTION);
                    AppData.eventManager.newEvent(e);
                }
                if (sound != null && sound.length() > 0) {
                    HashMap data = new HashMap();
                    data.put("sound_file", sound);
                    Event e = new Event("play_sound", data, Event.Type.USER_ACTION);
                    AppData.eventManager.newEvent(e);
                }
                if (image != null && image.length() > 0) {
                    HashMap data = new HashMap();
                    data.put("image_file", sound);
                    Event e = new Event("show_image", data, Event.Type.USER_ACTION);
                    AppData.eventManager.newEvent(e);
                } else if (system_event != null && system_event.length() > 0) {
                    Event e = new Event(system_event, null, Event.Type.SYSTEM_EVENT);
                    AppData.eventManager.newEvent(e);
                } else if (event != null && event.length() > 0) {
                    Event e = new Event(event, null, Event.Type.TIMER_EVENT);
                    AppData.eventManager.newEvent(e);
                }
            }
        %> 

        Успешно зарегистрированое устройство, id=<%=BoxCommonData.BOX_ID%>
        <br>
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
        <br> <br>

        <b>Internal UART command: </b>
        <form action="" method="post">
            <input type="text" name="command" /><input type="submit" value="OK" name="submit" />
        </form> <br> <br>
        <b>Generate timer event: </b>
        <form action="" method="post">
            <input type="text" name="event" value="db_clearing" /><input type="submit" value="OK" name="submit" />
        </form><br> <br>
        <b>Play sound: </b>
        <form action="" method="post">
            <input type="text" name="sound" value="Front_Center.wav" /><input type="submit" value="OK" name="submit" />
        </form>
        <br> <br>
        <b>Show image: </b>
        <form action="" method="post">
            <input type="text" name="image" value="screen.jpg" /><input type="submit" value="OK" name="submit" />
        </form>
        <br> <br>
        <b>Do system command: </b>
        <form action="" method="post">
            <input type="text" name="system_event" value="shutdown" /><input type="submit" value="OK" name="submit" />
        </form>
        <br>
        <form action="" method="post">
            SMS to:<input type="text" name="recipient" /><br>
            Text:<input type="text" name="sms" /><input type="submit" value="Send" name="submit" />
        </form> <br> <br>
        <br>
        <form action="" method="post">
            EMAIL to:<input type="text" name="recipient" /><br>
            Subject:<input type="text" name="subject" /><br>
            Text:<input type="text" name="mail" /><input type="submit" value="Send" name="submit" />
        </form> <br> <br>
        <br>
        <%
            for (String capture : VideoModule.IMAGE_FILES) {
        %>
        <img src="<%=request.getContextPath()%>/capture/<%=capture%>" /> <br>
        <%
                }
            }
        %>

    </body>
</html>
