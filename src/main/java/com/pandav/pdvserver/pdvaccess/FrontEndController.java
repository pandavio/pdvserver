// FrontEndController.java
package com.pandav.pdvserver.pdvaccess;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

@RestController
@CrossOrigin
@RequestMapping("/api")
public class FrontEndController {

    private final UserSessionService sessionService;

    public FrontEndController(UserSessionService sessionService) {
        this.sessionService = sessionService;
    }

    @GetMapping("/getUserAddress/{sessionId}")
    public ResponseEntity<String> getUserAddress(@PathVariable String sessionId) {
        String addr = sessionService.getWalletAddress(sessionId);
        return ResponseEntity.ok(addr != null ? addr : "");
    }
}
