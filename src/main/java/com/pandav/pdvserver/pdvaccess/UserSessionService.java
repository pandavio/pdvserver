package com.pandav.pdvserver.pdvaccess;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

@Service
public class UserSessionService {

    private final HashMap<String, UserData> userDataMap = new HashMap<>();
    private final Logger logger = LoggerFactory.getLogger(UserSessionService.class);

    public UserData getUserData(String sessionId) {
        synchronized (userDataMap) {
            UserData userData;
            if (userDataMap.containsKey(sessionId))
                userData = userDataMap.get(sessionId);
            else {
                userData = new UserData();
                userDataMap.put(sessionId, userData);
            }
            userData.setActiveNow();
            return userData;
        }
    }

    @Scheduled(fixedRate = 1000 * 60 * 60)
    private void deleteInactiveUserData() {
        synchronized (userDataMap) {
            Set<Map.Entry<String, UserData>> userSet = userDataMap.entrySet();
            Iterator<Map.Entry<String, UserData>> iterator = userSet.iterator();

            while (iterator.hasNext()) {
                Map.Entry<String, UserData> entry = iterator.next();
                UserData user = entry.getValue();

                if (user.getLastActiveMs() < System.currentTimeMillis() - 1000L * 60 * 30) {
                    logger.info("Removing user data for session {}", entry.getKey());
                    iterator.remove();
                }
            }
        }
    }

    public String getWalletAddress(String sessionId) {
        return getUserData(sessionId).getP2pkAddress();
    }

    public void setGateTxBroadcasted(String sessionId, boolean status) {
        getUserData(sessionId).setGateTxReady(status);
    }

    public boolean isGateTxBroadcasted(String sessionId) {
        return getUserData(sessionId).isGateTxReady();
    }
}