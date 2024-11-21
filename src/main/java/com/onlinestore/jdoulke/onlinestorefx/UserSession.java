package com.onlinestore.jdoulke.onlinestorefx;

import com.onlinestore.jdoulke.onlinestorefx.entities.User;

public class UserSession {
    private static User currentUser;

    public static User getCurrentUser() {
        return currentUser;
    }

    public static void setCurrentUser(User user) {
        currentUser = user;
    }

    public static void clearSession() {
        currentUser = null;
    }

}
