# Matrix Welcome Bot
[![Build Status](https://travis-ci.org/kamax-matrix/matrix-welcome-bot.svg?branch=master)](https://travis-ci.org/kamax-matrix/matrix-welcome-bot)

---

**This project is no longer maintained.**

---

## Overview
This Matrix bot will join whatever room it is invited to and greed new users with your configured message.   

## Build
Requirements:
- Java JDK 1.8

```
./gradlew build
```

A runnable jar will be created in the subfolder `build/libs`

## Run
Requirements:
- Java JRE/JDK 1.8

The following environment variables are required:

| Name                   | Description                                                        |
|------------------------|--------------------------------------------------------------------|
| `WB_STORE_FILE`        | Storage file for the bot. Must be readable and writable.           |
| `WB_USER_MXISD`        | Matrix ID of the user to use for the bot.                          |
| `WB_USER_PASSWORD`     | Password of the user to use for the bot.                           |
| `WB_HS_URL`            | Base URL of Homeserver to connect to. Auto-discovered if possible. |
| `WB_MESSAGE_TEXT_FILE` | The message in text format to send.                                |
| `WB_MESSAGE_HTML_FILE` | The message in text format to send.                                |

`%JOINED_USER%` is to be used as a placeholder in the files and will be replaced by the Matrix ID of the joined user. 

In the directory where the built jar is, run with:
```
<ENV>... java -jar matrix-welcome-bot.jar
```

Example for:
- The file `/var/matrix-welcome-bot/data` for the bot to store its state data
- The user `@john:example.org`
- The password `MyPassword`
- HS Base URL is auto-discovered thanks to `.well-known` support
- Message in text format in `/etc/matrix-welcome-bot/message.txt`
- Message in text format in `/etc/matrix-welcome-bot/message.html`

```
WB_STORE_FILE=/var/matrix-welcome-bot/data
WB_USER_MXID='@john:example.org' \
WB_USER_PASSWORD='MyPassword' \
WB_MESSAGE_TEXT_FILE='/etc/matrix-welcome-bot/message.txt' \
WB_MESSAGE_HTML_FILE='/etc/matrix-welcome-bot/message.html' \
java -jar welcome-bot.jar 
```

## Docker
Build with:
```
docker build -t matrix-welcome-bot .
```

Run with:
```
docker run --rm -e 'WB_CONFIG_KEY=...' matrix-welcome-bot
```
