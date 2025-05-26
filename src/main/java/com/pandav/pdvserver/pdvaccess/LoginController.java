package com.pandav.pdvserver.pdvaccess;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/ergo")
public class LoginController {

    // ✅ 使用ConcurrentHashMap，线程安全
    private final Map<String, LoginSession> loginSessions = new ConcurrentHashMap<>();

    @Value("${pdv.server.ip}")
    private String serverIp; // 用于动态生成 Ergopay 链接地址

    // ✅ 1. App调用 → 创建登录Session
    @GetMapping("/createLoginSession")
    public Map<String, String> createLoginSession() {
        String sessionId = UUID.randomUUID().toString();
        String message = "Please scan QR to login: " + sessionId;

        // ✅ 保存Session
        loginSessions.put(sessionId, new LoginSession(sessionId, message));

        // ✅ 返回ergopay链接
        return Map.of(
                "sessionId", sessionId,
                "message", message,
                "qrContent", "ergopay://" + serverIp + "/setAddress/" + sessionId + "/#P2PK_ADDRESS#"
        );
    }

    // ✅ 2. 钱包调用 → 设置地址
    @GetMapping("/setAddress/{sessionId}/{p2pkAddress}")
    public String setAddress(@PathVariable String sessionId,
                             @PathVariable String p2pkAddress) {
        LoginSession session = loginSessions.get(sessionId);
        if (session != null) {
            session.setWalletAddress(p2pkAddress);
            return "Login success!";
        } else {
            return "Session not found.";
        }
    }

    // ✅ 3. App轮询 → 检查是否扫码完成
    @GetMapping("/checkLoginResult/{sessionId}")
    public Map<String, String> checkLoginResult(@PathVariable String sessionId) {
        LoginSession session = loginSessions.get(sessionId);
        if (session != null && session.getWalletAddress() != null) {
            return Map.of(
                    "status", "signed",
                    "walletAddress", session.getWalletAddress(),
                    "signature", "N/A" // 保留兼容占位
            );
        } else {
            return Map.of("status", "waiting");
        }
    }
}
