/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lobzik.home_sapiens.pi;

import java.util.HashMap;
import org.lobzik.home_sapiens.pi.event.EventManager;

/**
 *
 * @author lobzik
 */
public class AppData {

    public static final HashMap<String, Object> settings = new HashMap();

    public static final EventManager eventManager = EventManager.getInstance();
}
