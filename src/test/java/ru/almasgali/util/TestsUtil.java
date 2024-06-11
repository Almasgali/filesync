package ru.almasgali.util;

public class TestsUtil {
    public static String constructAuthRequest(String username, String password) {
        return "{\"username\" : \"" + username + "\", \"password\" : \"" + password + "\" }";
    }
}
