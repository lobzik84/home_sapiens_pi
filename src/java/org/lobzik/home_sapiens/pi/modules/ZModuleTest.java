/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lobzik.home_sapiens.pi.modules;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;


/**
 *
 * @author lobzik
 */
public class ZModuleTest {

    public static void main(String[] args) {
        try {
//            AppData.init();
            Class.forName("com.mysql.jdbc.Driver").newInstance();

            PatternLayout layout = new PatternLayout("%d{yyyy.MM.dd HH:mm:ss} %c{1} %-5p: %m%n");
            ConsoleAppender consoleAppender = new ConsoleAppender(layout);
            BasicConfigurator.configure(consoleAppender);
            Logger.getRootLogger().info("Running test");
            //log.info("Starting hs app. Modules start!");
            ModemModule.test = true;
            ModemModule m = ModemModule.getInstance();

            m.start();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
