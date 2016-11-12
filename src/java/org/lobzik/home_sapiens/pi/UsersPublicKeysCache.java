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
import java.util.HashMap;
import java.util.List;
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
            return initUserPublicKey(userId);
        }
    }

    public String getLogin(int userId) {
        String login = usersLogins.get(userId);
        if (usersLogins != null) {
            return login;
        } else {
            initUserPublicKey(userId);
            return usersLogins.get(userId);
        }
    }

    public void addKey(int userId, RSAPublicKey key, String login) {
        usersKeys.put(userId, key);
        usersLogins.put(userId, login);
    }

    public RSAPublicKey initUserPublicKey(int userId) {
        String sSQL = "select id, login, public_key from users where id=" + userId;

        try (Connection conn = AppData.dataSource.getConnection()) {
            List<HashMap> resList = DBSelect.getRows(sSQL, conn);
            if (resList.size() > 0) {
                String publicKey = (String) resList.get(0).get("public_key");
                BigInteger modulus = new BigInteger(publicKey, 16);
                RSAPublicKeySpec spec = new RSAPublicKeySpec(modulus, BoxCommonData.RSA_E);
                KeyFactory factory = KeyFactory.getInstance("RSA");
                RSAPublicKey usersPublicKey = (RSAPublicKey) factory.generatePublic(spec);
                usersKeys.put(userId, usersPublicKey);
                usersLogins.put(userId, (String) resList.get(0).get("login"));
                return usersPublicKey;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
