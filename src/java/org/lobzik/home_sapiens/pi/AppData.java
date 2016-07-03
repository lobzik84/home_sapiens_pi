/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lobzik.home_sapiens.pi;

import java.util.HashMap;
import org.lobzik.home_sapiens.pi.modules.InternalSensorsModule;
import org.lobzik.home_sapiens.pi.tunnel.client.TunnelClient;

/**
 *
 * @author lobzik
 */
public class AppData {

    public static final HashMap<String, Object> settings = new HashMap();
    public static final TunnelClient tunnel = TunnelClient.getInstance();
    public static final InternalSensorsModule internalSensorsModule = InternalSensorsModule.getInstance();
}
