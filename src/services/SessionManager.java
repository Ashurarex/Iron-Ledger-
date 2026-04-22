package services;

import models.User;

public final class SessionManager {
    private volatile User currentUser;

    private SessionManager() {
    }

    public static SessionManager getInstance() {
        return Holder.INSTANCE;
    }

    private static class Holder {
        private static final SessionManager INSTANCE = new SessionManager();

        private Holder() {
        }
    }

    public synchronized void startSession(User user) {
        if (currentUser != null) {
            currentUser = null;
        }
        this.currentUser = user;
    }

    public synchronized void clearSession() {
        if (this.currentUser == null) {
            return;
        }
        this.currentUser = null;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public boolean isAuthenticated() {
        return currentUser != null;
    }
}
