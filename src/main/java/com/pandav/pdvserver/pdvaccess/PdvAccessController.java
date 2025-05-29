package com.pandav.pdvserver.pdvaccess;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@CrossOrigin
@RequestMapping("/api")
public class PdvAccessController {

    private static final Logger logger = LoggerFactory.getLogger(PdvAccessController.class);

    @Value("${node.wallet.url}")
    private String nodeWalletUrl; // 节点钱包 API 地址

    @Value("${pdv.token.id}")
    private String PDV_TOKEN_ID; // PDV 项目的 Token ID

    @Value("${pdv.api.key}")
    private String apiKey; // API 请求所需的密钥

    @Value("${pdv.wallet.password}")
    private String walletPassword; // 钱包密码（发送前用于解锁）

    private final UserSessionService sessionService;
    private final Map<String,String> cardToSessionMap = new ConcurrentHashMap<>();
    private final Map<String,Boolean> cardAccessMap = new ConcurrentHashMap<>();
    private final Map<String, Long> cardScanTimestamps = new ConcurrentHashMap<>();
    private static final long SCAN_COOLDOWN_MS = 10_000; // 10秒内只处理最后一次

    public PdvAccessController(UserSessionService sessionService) {
        this.sessionService = sessionService;
    }

    @Bean
    public RestTemplate restTemplate() {
        RestTemplate rt = new RestTemplate();
        rt.setInterceptors(Collections.singletonList((request, body, execution) -> {
            request.getHeaders().add("api_key", apiKey);
            return execution.execute(request, body);
        }));
        return rt;
    }

    // ✅ 绑定卡片与 Session
    @PostMapping("/bindCardToSession")
    public ResponseEntity<String> bindCard(@RequestBody Map<String,String> p) {
        String sid  = p.get("sessionId");
        String card = p.get("card");
        if (sid == null || card == null) {
            return ResponseEntity.badRequest().body("Missing sessionId or card");
        }
        cardToSessionMap.put(card, sid);
        logger.info("[BindCard] card={} -> session={}", card, sid);
        return ResponseEntity.ok("bound");
    }

    // ✅ 解绑卡片
    @PostMapping("/unbindCardFromSession")
    public ResponseEntity<String> unbind(@RequestBody Map<String,String> p) {
        String card = p.get("card");
        cardToSessionMap.remove(card);
        return ResponseEntity.ok("unbound");
    }

    // ✅ 查询 Session 信息
    @GetMapping("/getSessionInfo/{card}")
    public ResponseEntity<Map<String,String>> getSessionInfo(@PathVariable String card) {
        String sid    = cardToSessionMap.get(card);
        String wallet = sid == null ? "" : sessionService.getWalletAddress(sid);
        Map<String,String> resp = new HashMap<>();
        resp.put("sessionId", sid == null ? "" : sid);
        resp.put("walletAddress", wallet == null ? "" : wallet);
        logger.info("[GetSessionInfo] card={} -> {}", card, resp);
        return ResponseEntity.ok(resp);
    }

// ✅ 刷卡开门逻辑，含防抖处理、发币、记录 Tx
@GetMapping("/openDoor/{card}")
public ResponseEntity<String> openDoor(@PathVariable String card) {
    long now = System.currentTimeMillis();
    cardScanTimestamps.put(card, now); // 记录扫码时间
    try {
        Thread.sleep(150); // 延迟等待可能更后一次扫码
    } catch (InterruptedException ignored) {}

    long latest = cardScanTimestamps.getOrDefault(card, 0L);
    if (now < latest) {
        logger.info("[OpenDoor] Ignored duplicate scan for card={} at {} (latest={})", card, now, latest);
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body("duplicate");
    }

    String sid = cardToSessionMap.get(card);
    if (sid == null) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Card not bound");
    }
    String userAddr = sessionService.getWalletAddress(sid);
    if (userAddr == null) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No user wallet");
    }

    // ✅ 构造交易请求体
    Map<String,Object> req = new LinkedHashMap<>();
    req.put("address", userAddr);
    req.put("value", 1_000_000L);

    Map<String,Object> asset = new LinkedHashMap<>();
    asset.put("tokenId", PDV_TOKEN_ID);
    asset.put("amount", 10_000L);
    req.put("assets", Collections.singletonList(asset));
    List<Map<String,Object>> payment = Collections.singletonList(req);

    try {
        // ✅ 判断钱包是否已解锁（未解锁则自动解锁，已解锁则跳过）
        try {
            String statusUrl = nodeWalletUrl + "/wallet/status";
            RestTemplate rt = restTemplate();
            ResponseEntity<Map> statusResp = rt.getForEntity(statusUrl, Map.class);
            boolean isUnlocked = Boolean.TRUE.equals(statusResp.getBody().get("unlocked"));

            if (!isUnlocked) {
                logger.info("[Wallet] Locked. Unlocking now...");
                String unlockUrl = nodeWalletUrl + "/wallet/unlock";
                HttpHeaders unlockHeaders = new HttpHeaders();
                unlockHeaders.setContentType(MediaType.APPLICATION_JSON);
                Map<String,String> unlockPayload = new HashMap<>();
                unlockPayload.put("pass", walletPassword);
                HttpEntity<Map<String,String>> unlockEntity = new HttpEntity<>(unlockPayload, unlockHeaders);
                try {
                    rt.postForEntity(unlockUrl, unlockEntity, String.class);
                    logger.info("[Wallet] Unlocked successfully.");
                } catch (HttpClientErrorException ex) {
                    String responseBody = ex.getResponseBodyAsString();
                    if (ex.getStatusCode() == HttpStatus.BAD_REQUEST && responseBody.contains("Wallet already unlocked")) {
                        logger.warn("[Wallet] Already unlocked (ignored error).");
                    } else {
                        throw ex; // 其他错误照常抛出
                    }
                }
            } else {
                logger.info("[Wallet] Already unlocked. Skipping unlock step.");
            }
        } catch (Exception unlockEx) {
            logger.error("[WalletUnlock] Failed to check or unlock wallet", unlockEx);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("wallet unlock failed");
        }

        // ✅ 发送交易请求
        RestTemplate rt = restTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<List<Map<String,Object>>> entity = new HttpEntity<>(payment, headers);

        String url = nodeWalletUrl + "/wallet/payment/send";
        logger.info("[OpenDoor] POST {}", url);
        logger.info("[OpenDoor] Request body = {}", payment);

        ResponseEntity<String> resp = rt.postForEntity(url, entity, String.class);

        if (resp.getStatusCode().is2xxSuccessful()) {
            logger.info("[OpenDoor] <<SUCCESS>> Transaction ID: {}", resp.getBody());
            sessionService.getUserData(sid).setLastTxId(resp.getBody());

            // ✅ 成功后锁钱包
            try {
                String lockUrl = nodeWalletUrl + "/wallet/lock";
                HttpHeaders lockHeaders = new HttpHeaders();
                lockHeaders.set("accept", "application/json");
                HttpEntity<Void> lockEntity = new HttpEntity<>(lockHeaders);
                ResponseEntity<String> lockResp = restTemplate().exchange(
                        lockUrl, HttpMethod.GET, lockEntity, String.class);
                logger.info("[WalletLock] lockResp = {}", lockResp.getBody());
            } catch (Exception ex) {
                logger.error("[WalletLock] exception", ex);
            }

            cardAccessMap.put(card, true);
            return ResponseEntity.ok("waiting");
        } else {
            logger.error("[OpenDoor] node wallet error: {}", resp.getStatusCode());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("payment error");
        }
    } catch (Exception ex) {
        logger.error("[OpenDoor] exception", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("open failed");
    }
}

    // ✅ 客户端轮询结果
    @GetMapping("/accessStatus/{card}")
    public ResponseEntity<String> accessStatus(@PathVariable String card) {
        logger.info("[AccessStatus] check for card={}", card);
        boolean ok = cardAccessMap.getOrDefault(card, false);
        if (ok) {
            cardAccessMap.remove(card);
            String sid = cardToSessionMap.get(card);
            if (sid != null) {
                UserData user = sessionService.getUserData(sid);
                String lastTxId = user.getLastTxId();
                logger.info("[AccessStatus] card={} has txId={}", card, lastTxId);
                if (lastTxId != null && !lastTxId.isEmpty()) {
                    return ResponseEntity.ok("valid:" + lastTxId.replace("\"", ""));
                } else {
                    return ResponseEntity.ok("valid");
                }
            } else {
                logger.warn("[AccessStatus] card={} has no session", card);
                return ResponseEntity.ok("valid");
            }
        }
        return ResponseEntity.ok("waiting");
    }

    // ✅ 钱包签名回调设置地址
    @GetMapping("/setAddress/{sessionId}/{address}")
    public PdvAccessResponse setAddress(@PathVariable String sessionId,
                                      @PathVariable String address) {
        logger.info("[Login] SessionID={} WalletAddress={}", sessionId, address);
        sessionService.getUserData(sessionId).setP2pkAddress(address);
        PdvAccessResponse resp = new PdvAccessResponse();
        resp.address = address;
        resp.message = "Connected to your address " + address;
        resp.messageSeverity = PdvAccessResponse.Severity.INFORMATION;
        return resp;
    }

    // ✅ 测试 ESP 联通性
    @GetMapping("/testConnection")
    public ResponseEntity<String> testConn() {
        return ResponseEntity.ok("ESP connected OK");
    }
}
