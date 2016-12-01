<%@page import="org.lobzik.tools.db.mysql.DBSelect"%>
<%@page import="org.lobzik.tools.db.mysql.DBTools"%>
<%@page import="java.sql.Connection"%>
<%@page import="org.lobzik.tools.Tools"%>
<%@ page contentType="text/html" pageEncoding="UTF-8"%>
<%@ page import="java.util.*"%>
<%@ page import="org.lobzik.home_sapiens.pi.*"%>
<%

    String[] yearMonths = {"янв", "фев", "мар", "апр", "мая", "июн", "июл", "авг", "сен", "окт", "ноя", "дек"};

    long from = Tools.parseLong(request.getParameter("from"), 0L);
    long to = Tools.parseLong(request.getParameter("to"), System.currentTimeMillis());
    
    String dateFrom = Tools.getFormatedDate(new Date(from), "dd.MM.YYYY");
    String dateTo = Tools.getFormatedDate(new Date(to), "dd.MM.YYYY");
    String moduleName = request.getParameter("moduleName");
    
    String severity = "";
    if (request.getParameter("severity") != null) {
        severity = request.getParameter("severity");
    }
%>
<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml">
    <head>
        <meta charset="utf-8">
            <meta name="viewport" content="width=device-width">
                <meta http-equiv="X-UA-Compatible" content="IE=edge">
                    <meta name="x-apple-disable-message-reformatting">
                        <title>Отчет о событиях и действиях за период: <%=dateFrom%> - <%=dateTo%></title>
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
                                                    <p style="font-size: 16px; font-weight: bold; margin:40px 0;">Управдом - <%=BoxSettingsAPI.get("BoxName")%>. &nbsp;Отчет о событиях и действиях за период: <%=dateFrom%> - <%=dateTo%></p>
                                                </td>
                                            </tr>
                                            <tr>
                                                <td bgcolor="#ffffff" style="padding: 0 10px;">                        
                                                    <table width="100%" style="width:100%; min-width: 600px; padding: 0; margin:0;">
<%
                                                        List argsList = new LinkedList();
                                                        Connection conn = DBTools.openConnection(BoxCommonData.dataSourceName);
                                                        try {
                                                        String sSQL = "select * from logs l where 1=1 \n"
                                                        + " and unix_timestamp(l.dated) > " + from / 1000 + " \n"
                                                        + " and unix_timestamp(l.dated) < " + to / 1000 + " \n";
                                                        if (moduleName != null && moduleName.length() > 0 && !moduleName.equals("*")) {
                                                        sSQL += " and l.module_name = ? \n";
                                                        argsList.add(moduleName);
                                                        }
                                                        if (severity.equals("ALARM")) {
                                                        sSQL += " and l.level in ('FATAL') \n";
                                                        } else if (severity.equals("ALERT")) {
                                                        sSQL += " and l.level in ('FATAL', 'ERROR') \n";
                                                        } else if (severity.equals("OK")) {
                                                        sSQL += " and l.level in ('FATAL', 'ERROR', 'WARN') \n";
                                                        } else if (severity.equals("INFO")) {
                                                        sSQL += " and l.level in ('FATAL', 'ERROR', 'WARN', 'INFO') \n";
                                                        }

                                                        sSQL += " order by l.dated desc limit 1000;";
                                                        List<HashMap> logs = DBSelect.getRows(sSQL, argsList, conn);
                                                            for (int i = 0; i < logs.size(); i++) {
                                                            String color = "#607d8b";
                                                            String icon = "";
                                                            String iStyle ="";
                                                            HashMap h = logs.get(i);
                                                            String levelF = (String) h.get("level");
                                                            if (levelF != null) {

                                                            if (levelF.equals("FATAL")) {
                                                            color = "#f44336";
                                                            icon = "!";
                                                            
                                                            } else if (levelF.equals("ERROR")) {
                                                            color = "#ff9801";
                                                            icon = "!";
                                                            } else if (levelF.equals("WARN")) {
                                                            color = "#4caf50";
                                                            icon = "ok";
                                                            } else {
                                                            color = "#607d8b";
                                                            icon = "i";
                                                            iStyle = "font-family:Georgia; font-style: italic;";
                                                            }

                                                            }
                                                            Date dateJ = (Date) h.get("dated"); 
                                                            Calendar c = new GregorianCalendar();
                                                            c.setTime(dateJ);
                                                            String date =  Tools.getFormatedDate(dateJ, "dd");
                                                            date += " " +yearMonths[c.get(Calendar.MONTH)] + ", ";
                                                            date += Tools.getFormatedDate(dateJ, "HH:mm");
                                                            String message = (String) h.get("message");
                                                            String alias = "DEFAULT";

                                                            if (message.startsWith("ALIAS:")) {
                                                            message = message.substring(6);
                                                            int in = message.indexOf(": ");
                                                            if (in > 0) {
                                                            alias = message.substring(0, in);
                                                            message = message.substring(in + 2);
                                                            }
                                                            }
                                                            %>
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
                                                            <%
                                                                }

                                                            }
                                                            catch (Exception e) {

                                                            %><tr><td>Ошибка сбора статистики! <%=e.getClass() + ":" + e.getMessage()%></td> </tr><%
                                                                } finally {
                                                                    conn.close();
                                                                }
                                                            %>

                                                    </table>                        
                                                </td>
                                            </tr>                                
                                            <tr>
                                                <td bgcolor="#ffffff">
                                                    <table role="presentation" cellspacing="0" cellpadding="0" border="0" width="100%">
                                                        <tr>
                                                            <td style="padding: 40px; font-family: sans-serif; font-size: 15px; line-height: 20px; color: #555555;">
                                                                Отчет сформирован автоматически на основании указанной Вами команды "Отправлять еженедельную статистику на электронную почту" в личном кабинете устройства <a href="http://my.moidom.molnet.ru">my.moidom.molnet.ru</a>
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