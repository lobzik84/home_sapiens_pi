/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lobzik.home_sapiens.pi;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.sql.Connection;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import org.lobzik.tools.Tools;
import org.lobzik.tools.db.mysql.DBSelect;

/**
 *
 * @author lobzik
 */
public class UsersPublicKeysCache {

    private static final UsersPublicKeysCache instance = new UsersPublicKeysCache();

    private static final HashMap<Integer, RSAPublicKey> usersKeys = new HashMap();
    private static final HashMap<Integer, String> usersLogins = new HashMap();

    private UsersPublicKeysCache() {
    }

    public static UsersPublicKeysCache getInstance() {
        return instance;
    }

    public RSAPublicKey getKey(int userId) {
        RSAPublicKey key = usersKeys.get(userId);
        if (key != null) {
            return key;
        } else {
            return initUsersPublicKey();
        }
    }

    public String getLogin(int userId) {
        String login = usersLogins.get(userId);
        if (usersLogins != null) {
            return login;
        } else {
            initUsersPublicKey();
            return usersLogins.get(userId);
        }
    }

    public void addKey(int userId, RSAPublicKey key, String login) {
        usersKeys.put(userId, key);
        usersLogins.put(userId, login);
    }
    
    public Collection<String> getLogins() {
        return usersLogins.values();
    }

    public RSAPublicKey initUsersPublicKey() {
        String sSQL = "select id, login, public_key from users";// where id=" + userId;

        try (Connection conn = AppData.dataSource.getConnection()) {
            List<HashMap> resList = DBSelect.getRows(sSQL, conn);
            for (HashMap h:resList) {
                int userId = Tools.parseInt(h.get("id"), 0);
                String publicKey = (String) h.get("public_key");
                BigInteger modulus = new BigInteger(publicKey, 16);
                RSAPublicKeySpec spec = new RSAPublicKeySpec(modulus, BoxCommonData.RSA_E);
                KeyFactory factory = KeyFactory.getInstance("RSA");
                RSAPublicKey usersPublicKey = (RSAPublicKey) factory.generatePublic(spec);
                usersKeys.put(userId, usersPublicKey);
                usersLogins.put(userId, (String) h.get("login"));
                return usersPublicKey;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
