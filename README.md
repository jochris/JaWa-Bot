# 🤖 JaWa-Bot

Modular WhatsApp Client Bot application built on top of the [JaWa WhatsApp Library](https://github.com/jochris/JaWa) v0.0.4.

---

## 🌟 Features

- 📱 **Phone Pairing Code Authentication** (Connect via phone number without scanning QR).
- 🗄️ **Pure SQLite Database Session Storage & Maintenance** (`sessions/jawabot.db` with integrity check and disk vacuuming).
- 🧩 **Modular Sub-Services Architecture** (`client.messages()`, `client.groups()`, `client.media()`, `client.chats()`).
- 📁 **Modular Command System** (`cmd/` with categories like `cmd/info/PingCmd.java` & `cmd/info/InfoServerCmd.java`).
- 🏷️ **Rich Command Metadata** (`name`, `tag`, `usage`, `description`, `aliases`).
- 🏓 **`.ping` Command** (Response latency & uptime report).
- 🖥️ **`.infoserver` Command** (Detailed OS version, CPU cores, RAM usage, Java Runtime, Uptime).
- 🗃️ **`.db` / `.vacuum` Command** (Owner command for inspecting SQLite session stats and reclaiming disk space).
- 🔒 **`.self` / `.public` Command** (Owner toggle between private self mode and public mode).
- 💻 **`> <cmd>` Shell Command** (Owner terminal exec capability).
- 📋 **Auto-generated `.menu` / `.help`** (Categorized command listing).

---

## 🚀 Getting Started

### 1. Build Project
```bash
mvn compile
```

### 2. Run Bot
```bash
mvn exec:java -Dexec.mainClass="id.jawabot.Main"
```

Or pass your phone number directly for instant pairing code generation:
```bash
mvn exec:java -Dexec.mainClass="id.jawabot.Main" -Djawa.phone=628123456789
```

---

## 📁 Project Structure

```
JaWa-Bot/
├── pom.xml
├── README.md
├── sessions/
│   └── jawabot.db
└── src/main/java/id/jawabot/
    ├── Main.java
    ├── cmd/
    │   ├── Command.java
    │   ├── CommandRegistry.java
    │   ├── info/
    │   │   ├── PingCmd.java
    │   │   └── InfoServerCmd.java
    │   └── owner/
    │       ├── DbMaintCmd.java
    │       ├── ExecCmd.java
    │       └── ModeCmd.java
    ├── config/
    │   └── BotConfig.java
    └── helper/
        ├── JidHelper.java
        └── SystemHelper.java
```
