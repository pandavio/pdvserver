# PDVServer - ErgoPay Integration Server

Backend server for **PandaV Access**, using **ErgoPay** to authenticate users via their Ergo wallet.

Users sign transactions (including reduced or zero-value TXs) to prove wallet ownership. The server binds the wallet address to a session or card, enabling secure blockchain-based login and door access.

---

## 💡 Key Features

- ✅ ErgoPay-based login flow
- ✅ Bind wallet address to session or NFT Token ID or card
- ✅ Transaction signing via ErgoPay deeplink or QR
- ✅ Written in Java + Spring Boot
- ✅ Includes example endpoints for real-world access systems

---

## 🔧 How to Run Locally

```bash
git clone https://github.com/pandavio/pdvserver.git
cd pdvserver
./gradlew bootRun

```

> 📝 Make sure you have **Java 11+** and internet access to resolve dependencies.

---

## 🔗 Example ErgoPay Login Flow

1. Client creates a session ID.
2. Server generates a ReducedTx for the user to sign.
3. Ergo wallet opens (via `ergopay://...`) and signs the transaction.
4. Signed TX is returned → Server extracts wallet address → Login complete.

### 🔄 Try a login (example endpoint)

```http
GET /setAddress/{sessionId}/{walletAddress}
```

You can trigger this with a wallet-compatible deeplink:

```
ergopay://yourserver.com:8080/setAddress/{sessionId}/{walletAddress}
```

---

## 📂 Main Controller

Check out:

- `PdvAccessController.java` → handles ErgoPay address binding
- `PdvAuthController.java` → handles optional ErgoAuth signature verification
- `UserSessionService.java` → handles session/address mapping

---

## ✅ Deploying

To deploy on your own server:

1. Set environment port (e.g. `server.port=8080`)
2. Expose backend via HTTPS or tunneling service (e.g. [Ngrok](https://ngrok.com/))
3. Update frontend to call your `ergopay://` or `/setAddress` endpoints

---

## 🛡️ Security Notes

- All wallet authentication is verified via signed Ergo transactions.
- No password is required — authentication is cryptographic and decentralized.
- Transactions can be logged to the blockchain for immutable access records.

---

## 🧠 Credits

Built with ❤️ by [PandaV] (https://www.pandav.io)

[Ergo Platform] (https://ergoplatform.org), using the native ErgoPay protocol for wallet-based authentication and secure access control.

---

# PDVServer - ErgoPay 集成服务器

PandaV Access 的后端服务器，使用 ErgoPay 通过 Ergo 钱包进行用户身份验证。

用户通过签署交易（包括简化或零金额交易）来证明钱包所有权。服务器将钱包地址绑定到会话、NFT Token ID 或实体卡，实现基于区块链的安全登录与门禁控制。

---

💡 主要功能

- ✅ 基于 ErgoPay 的登录流程
- ✅ 将钱包地址绑定到会话、NFT Token ID 或门禁卡
- ✅ 通过 ErgoPay Deeplink 或二维码进行交易签名
- ✅ 使用 Java + Spring Boot 编写
- ✅ 包含真实门禁系统的示例接口

---

🔧 本地运行方式

git clone https://github.com/pandavio/pdvserver.git
cd pdvserver
./gradlew bootRun

📝 请确保你安装了 Java 11 或更高版本，并具备联网能力以下载依赖。

---

🔗 ErgoPay 登录流程示意

1. 客户端创建一个 Session ID。
2. 服务器生成用户需签署的 ReducedTx（简化交易）。
3. 通过 ergopay://... Deeplink 打开 Ergo 钱包，用户签名交易。
4. 签名交易返回 → 服务器解析钱包地址 → 登录完成。

🔄 示例登录接口

GET /setAddress/{sessionId}/{walletAddress}

你可以通过支持钱包 Deeplink 的方式触发：

ergopay://yourserver.com:8080/setAddress/{sessionId}/{walletAddress}

---

📂 核心控制器

请查看：

- PdvAccessController.java → 处理钱包地址绑定逻辑
- PdvAuthController.java → （可选）处理 ErgoAuth 的签名验证
- UserSessionService.java → 管理会话与钱包地址映射关系

---

✅ 部署方式

如需部署到你自己的服务器：

1. 设置环境端口（例如 server.port=8080）
2. 通过 HTTPS 或隧道服务（如 Ngrok）对外开放后端接口
3. 更新前端调用 ergopay:// 或 /setAddress 接口的地址

---

🛡️ 安全说明

- 所有钱包身份验证均通过 Ergo 签名交易完成验证。
- 无需密码 —— 身份验证基于加密签名，去中心化且安全。
- 所有访问记录可通过区块链交易实现不可篡改的追踪日志。

---

🧠 致谢

由 PandaV ❤️ 构建。

基于 Ergo Platform，使用原生 ErgoPay 协议实现基于钱包的认证与门禁控制系统。
