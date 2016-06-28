/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lobzik.home_sapiens.pi;

import java.util.HashMap;
import org.lobzik.home_sapiens.pi.tunnel.client.TunnelClient;

/**
 *
 * @author lobzik
 */
public class AppData {
    public static HashMap<String, Object> settings = new HashMap();
    public static TunnelClient tunnel = TunnelClient.getInstance();
}
