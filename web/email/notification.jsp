<%@page import="org.lobzik.home_sapiens.pi.behavior.Notification"%>
<%@page import="org.lobzik.tools.Tools"%>
<%@ page contentType="text/html" pageEncoding="UTF-8"%>
<%@ page import="java.util.*"%>
<%@ page import="org.lobzik.home_sapiens.pi.*"%><%

    int id = Tools.parseInt(request.getParameter("id"), 0);
    Notification n = AppData.emailNotification.get(id);
    if (n == null) {
        return;
    }
        // AppData.emailNotification.remove(id);
    String color = "#607d8b";
    String icon = "";
    String iStyle = "";
    String date = Tools.getFormatedDate(n.startDate, "dd.MM.YYYY HH:mm");
    String message = n.text;
    String severity = "";
    switch (n.severity) {
        case ALARM:
            severity = "Тревога";
            color = "#f44336";
            icon = "!";
            break;
        case ALERT:
            severity = "Предупреждение";
            color = "#ff9801";
            icon = "!";
            break;
        case OK:
            severity = "ОК";            
            color = "#4caf50";
            icon = "ok";
            break;
        case INFO:
            severity = "Уведомление";            
            color = "#607d8b";
            icon = "i";
            iStyle = "font-family:Georgia; font-style: italic;";
            break;
    }


%>
<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml">
    <head>
        <meta charset="utf-8">
            <meta name="viewport" content="width=device-width">
                <meta http-equiv="X-UA-Compatible" content="IE=edge">
                    <meta name="x-apple-disable-message-reformatting">
                        <title></title>
                        <link href='https://fonts.googleapis.com/css?family=Roboto:400,700' rel='stylesheet' type='text/css'>   
                            <!--[if mso]>
                                <style>
                                    * {
                                        font-family: sans-serif !important;
                                    }
                                </style>
                            <![endif]-->    
                            <!--[if !mso]><!-->
                            <link href='https://fonts.googleapis.com/css?family=Roboto:400,700' rel='stylesheet' type='text/css'>
                                <!--<![endif]-->
                                <style>
                                    html,
                                    body {
                                        font-family: 'Roboto';
                                        margin: 0 auto !important;
                                        padding: 0 !important;
                                        height: 100% !important;
                                        width: 100% !important;
                                    }            
                                    * {
                                        -ms-text-size-adjust: 100%;
                                        -webkit-text-size-adjust: 100%;
                                    }                
                                    div[style*="margin: 16px 0"] {
                                        margin:0 !important;
                                    }            
                                    table,
                                    td {
                                        mso-table-lspace: 0pt !important;
                                        mso-table-rspace: 0pt !important;
                                    }                    
                                    table {
                                        border-spacing: 0 !important;
                                        border-collapse: collapse !important;
                                        table-layout: fixed !important;
                                        margin: 0 auto !important;
                                    }
                                    table table table {
                                        table-layout: auto; 
                                    }                
                                    img {
                                        -ms-interpolation-mode:bicubic;
                                    }            
                                    .mobile-link--footer a,
                                    a[x-apple-data-detectors] {
                                        color:inherit !important;
                                        text-decoration: underline !important;
                                    }        
                                    .button-link {
                                        text-decoration: none !important;
                                    }      
                                </style>
                                <style>
                                    .button-td,
                                    .button-a {
                                        transition: all 100ms ease-in;
                                    }
                                    .button-td:hover,
                                    .button-a:hover {
                                        background: #38983c !important;
                                        border-color: #38983c !important;
                                    }
                                </style>
                                </head>
                                <body width="100%" bgcolor="#e9ebee" style="margin: 0; mso-line-height-rule: exactly; width:100%; min-width: 600px;">
                                    <center style="width: 100%; background: #e9ebee;">

                                        <!--[if mso]>
                                        <table role="presentation" cellspacing="0" cellpadding="0" border="0" width="600" align="center">
                                        <tr>
                                        <td>
                                        <![endif]-->            
                                        <table role="presentation" cellspacing="0" cellpadding="0" border="0" align="center" width="100%" style="width:100%;min-width: 600px;">
                                            <tr>
                                                <td bgcolor="#ffffff" style="padding: 0 40px;">
                                                    <p style="font-size: 16px; font-weight: bold; margin:40px 0;">Управдом - <%=BoxSettingsAPI.get("BoxName")%>. <%=severity%></p>
                                                </td>
                                            </tr>
                                            <tr>
                                                <td bgcolor="#ffffff" style="padding: 0 10px;">                        
                                                    <table width="100%" style="width:100%; min-width: 600px; padding: 0; margin:0;">

                                                        <tr>
                                                            <td style="width:40px; box-sizing: border-box; color:#ffffff; font-size: 20px; text-align: center; border-bottom: 3px solid #e9ebee; padding: 10px; line-height: 20px; background-color: <%=color%>;">
                                                                <span style="display:block; box-sizing: border-box;<%=iStyle%>"><%=icon%></span>
                                                            </td>
                                                            <td style="width:135px; box-sizing: border-box; font-size: 14px; border-bottom: 3px solid #e9ebee; padding: 10px; line-height: 20px; background-color: #ffffff; color: #a9a9a9;">
                                                                <%=date%>
                                                            </td>
                                                            <td style="font-size: 14px; border-bottom: 3px solid #e9ebee; padding: 10px; line-height: 20px; background-color: #ffffff;">	                                 
                                                                <span style="line-height: 20px;"><%=message%></span>
                                                            </td>
                                                        </tr>   


                                                    </table>                        
                                                </td>
                                            </tr>                                
                                            <tr>
                                                <td bgcolor="#ffffff">
                                                    <table role="presentation" cellspacing="0" cellpadding="0" border="0" width="100%">
                                                        <tr>
                                                            <td style="padding: 40px; font-family: sans-serif; font-size: 15px; line-height: 20px; color: #555555;">
                                                                Данное письмо отправлено Вам, потому что вы указали свою электронную почту в личном кабинете устройства <a href="http://my.moidom.molnet.ru">my.moidom.molnet.ru</a>
                                                                <br><br>                                    
                                                                        <table role="presentation" cellspacing="0" cellpadding="0" border="0" align="center" style="margin: auto;">
                                                                            <tr>
                                                                                <td style="text-align: center;" class="button-td">
                                                                                    <a href="http://my.moidom.molnet.ru" style="background: #4caf50; border: 15px solid #4caf50; font-family: sans-serif; font-size: 14px; line-height: 1.1; text-align: center; text-decoration: none; display: block; font-weight: bold;" class="button-a">
                                                                                        <span style="color:#ffffff;" class="button-link">&nbsp;&nbsp;&nbsp;&nbsp;Перейти в личный кабинет&nbsp;&nbsp;&nbsp;&nbsp;</span>
                                                                                    </a>
                                                                                </td>
                                                                            </tr>
                                                                        </table>
                                                                        </td>
                                                                        </tr>
                                                                        </table>
                                                                        </td>
                                                                        </tr>               
                                                                        </table>           
                                                                        <!--[if mso]>
                                                                        </td>
                                                                        </tr>
                                                                        </table>
                                                                        <![endif]-->

                                                                        </center>
                                                                        </body>
                                                                        </html>