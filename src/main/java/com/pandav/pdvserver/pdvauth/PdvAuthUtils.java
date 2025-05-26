package com.pandav.pdvserver.pdvauth;

import java.util.Base64;

public class PdvAuthUtils {

    /**
     * 模拟验证签名
     * @param sigmaProp - 用于代表目标地址的 SigmaProp 结构（目前未真正使用）
     * @param message - 要求签名的原始消息
     * @param signedMessage - 钱包返回的签名字符串（通常是 base64）
     * @param proof - 解码后的 byte[] 签名（通常是 base64.decode(authResponse.proof)）
     * @return boolean 表示验证是否通过
     */
    public static boolean verifyResponse(SigmaProp sigmaProp, String message, String signedMessage, byte[] proof) {
        // ⚠️ 模拟验证逻辑：只判断 proof 非空即可
        // 你可以根据需要引入真正的签名验证算法
        return proof != null && proof.length > 0;
    }

    // 可选：保留 3参数版本（用于简化测试）
    public static boolean verifyResponse(String signedMessage, String message, String address) {
        // ⚠️ 模拟：检查签名是否包含地址
        return signedMessage != null && signedMessage.contains(address);
    }
}
