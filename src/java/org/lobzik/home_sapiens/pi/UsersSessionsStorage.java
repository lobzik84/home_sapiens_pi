/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lobzik.home_sapiens.pi;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Random;

/**
 *
 * @author lobzik
 */
public class UsersSessionsStorage  {
    private static final HashMap<String, UsersSession> storage = new HashMap();
    private static final long SESSION_TTL = 30 * 60 * 1000L;

    public void cleanUpOldSessions() {
        for (String key : storage.keySet()) {
            UsersSession session = storage.get(key);
            if (System.currentTimeMillis() > session.getRefreshTime() + SESSION_TTL) {
                storage.remove(key);
                session = null;
            }
        }
    }

    public String createSession() {
        cleanUpOldSessions();
        BigInteger b = new BigInteger(32, new Random());
        String session_key = b.toString(16);
        UsersSession session = new UsersSession();
        storage.put(session_key, session);
        return session_key;
    }

    public UsersSession get(String session_key) {
        UsersSession session = storage.get(session_key);
        if (session != null) {
            session.updateRefreshTime();
        } else {
            cleanUpOldSessions();
            session = new UsersSession();
            storage.put(session_key, session);

        }
        return session;
    }

}
