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