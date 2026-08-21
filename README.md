# 🧀 치지직 데이터팩 링크 (Chzzk Datapack Link)

[![Minecraft](https://img.shields.io/badge/Minecraft-26.2-blue?logo=minecraft)](https://minecraft.net/)
[![Fabric](https://img.shields.io/badge/Modloader-Fabric-orange?logo=fabric)](https://fabricmc.net/)
[![AI Assisted](https://img.shields.io/badge/AI-Assisted-8A2BE2?logo=google&logoColor=white)](https://github.com/ga170212/chk-datapack-link)
[![License](https://img.shields.io/badge/License-CC0--1.0-green)](LICENSE)

네이버 **치지직(Chzzk)** 라이브 방송의 채팅과 후원(치즈, 미션) 이벤트를 마인크래프트 **NBT 스토리지 및 데이터팩과 실시간으로 연동**해주는 패브릭(Fabric) 모드입니다.

> ⚠️ **안내**: 본 모드는 네이버(NAVER)의 공식 모드가 아닌 비공식 오픈소스 서드파티 모드이며, AI 코딩 어시스턴트의 지원을 받아 제작되었습니다.  
> 💡 **필수**: 본 모드는 치지직 이벤트를 마인크래프트 스토리지로 전달하는 **브릿지(Bridge) 모드**이므로, 이벤트를 받아 실행할 **데이터팩이 필수**입니다. 레포지토리의 **[`example_datapacks`](example_datapacks)** 폴더에 모아둔 예시 데이터팩(`chklink_sample` 등)을 참고하여 적용해 주세요!

---

## ✨ 주요 기능 (Features)

- **📡 실시간 웹소켓 연동**: 방송이 켜져 있지 않아도 채팅방 웹소켓(`WSS`)에 연결되어 채팅 및 후원 패킷을 실시간 수신합니다.
- **📦 NBT 스토리지 자동 저장**: 수신된 데이터를 `minecraft:chat` 및 `minecraft:donation` 스토리지에 즉시 NBT 형태로 저장합니다.
- **🏷️ 펑션 태그 기반 다중 데이터팩 실행**: 스토리지 갱신 후 `#chklink:chat` 및 `#chklink:donation` 태그에 등록된 모든 데이터팩 펑션을 매크로(`with storage`)로 일괄 실행합니다.
- **👥 멀티플레이어 개별 세션 지원**: 서버에 접속한 각 플레이어가 본인의 치지직 채널 ID로 각자 연동할 수 있으며, 데이터팩에서 `$(player_name)`으로 대상을 지정할 수 있습니다.
- **🧹 이모티콘 태그 정제 & !명령어 분리**:
  - `{:d_108:}` 형태의 치지직 구독 이모티콘 태그 자동 제거
  - `!투표 강아지` ➔ `cmd: "투표"`, `chat: "강아지"` 자동 분리
- **🎮 인게임 GUI & 단축키 지원**: `F6` 단축키로 설정창을 열어 채널 ID를 입력하고 연동을 켜고 끌 수 있습니다.
- **🧪 시뮬레이션 명령어**: 인게임에서 `/chklink testchat` 및 `/chklink testdonation` 명령어로 실제 방송 없이도 데이터팩 연동을 테스트할 수 있습니다.

---

## 📋 요구 사항 (Requirements)

- **Minecraft**: 26.2
- **Mod Loader**: [Fabric Loader](https://fabricmc.net/)
- **필수 모드**: [Fabric API](https://modrinth.com/mod/fabric-api)
- **필수 데이터팩**: 치지직 이벤트를 처리할 데이터팩 ([`example_datapacks`](example_datapacks) 내 샘플 데이터팩 참고)

---

## 🎮 사용 방법 (How to Use)

### 1. 데이터팩 적용하기 (필수)
1. 레포지토리의 **[`example_datapacks/chklink_sample`](example_datapacks/chklink_sample)** 폴더(또는 나만의 커스텀 데이터팩)를 마인크래프트 월드의 `datapacks/` 폴더에 복사해 넣습니다.
   > 경로 예시: `.minecraft/saves/월드이름/datapacks/chklink_sample`
2. 월드에 접속한 상태라면 채팅창에 `/reload` 명령어를 입력하여 데이터팩을 새로고침합니다.

### 2. 치지직 채널 ID 확인하기
치지직 스트리머의 방송국 URL 주소에서 채널 ID(32자리 해시값)를 복사합니다:
> 예: `https://chzzk.naver.com/live/0123456789abcdef0123456789abcdef`  
> ➔ 채널 ID: **`0123456789abcdef0123456789abcdef`**

### 3. 인게임에서 연동하기
1. 게임 내에서 **`F6`** 키를 눌러 치지직 연동 설정창을 엽니다.
2. 복사한 **채널 ID**를 입력하고 **[채널 ID 저장]**을 누릅니다.
3. **[연동 시작]** 버튼을 클릭하면 실시간 방송 채팅과 연동됩니다!

---

## 💻 인게임 명령어 목록

| 명령어 | 설명 |
| :--- | :--- |
| `/chklink connect` (또는 `/chklink start`) | 설정된 채널 ID로 치지직 연동을 시작합니다. |
| `/chklink disconnect` (또는 `/chklink stop`) | 치지직 연동을 해제합니다. |
| `/chklink status` | 현재 설정된 채널 ID와 연동 상태를 확인합니다. |
| `/chklink testchat <메시지>` | 가상 채팅을 생성하여 스토리지 및 데이터팩으로 테스트 전송합니다. |
| `/chklink testdonation <금액> <메시지>` | 가상 후원(치즈)을 생성하여 스토리지 및 데이터팩으로 테스트 전송합니다. |

---

## 🛠️ 데이터팩 제작자 가이드 (Datapack Integration)

모드는 치지직 이벤트가 발생할 때마다 스토리지에 데이터를 병합한 후, 해당하는 **펑션 태그**를 매크로와 함께 실행합니다.  
처음 제작하신다면 **[`example_datapacks/chklink_sample`](example_datapacks/chklink_sample)** 구조를 그대로 복사하여 커스텀해 보세요!

### 1. 펑션 태그 등록하기

데이터팩 안에 아래 경로로 태그 JSON 파일을 생성하여 실행할 함수들을 등록합니다:

#### 💬 채팅 태그: `data/chklink/tags/function/chat.json`
```json
{
  "values": [
    "chklink:example_chat",
    "chklink:example_chat2"
  ]
}
```

#### 🧀 후원 태그: `data/chklink/tags/function/donation.json`
```json
{
  "values": [
    "chklink:example_donation",
    "chklink:example_donation2"
  ]
}
```

---

### 2. 스토리지 NBT 구조 명세

#### 💬 `minecraft:chat` 스토리지
```snbt
{
  player_name: "플레이어닉네임", // 방송 연동을 켠 마인크래프트 플레이어 이름
  player_uuid: "12345678-...", // 마인크래프트 플레이어 UUID
  cmd: "투표",                // !명령어 명칭 (예: !투표 강아지 -> "투표")
  chat: "강아지",              // 실제 메시지 내용 (명령어 제외 텍스트)
  sender_nick: "시청자닉네임",  // 치지직 시청자(보낸 사람) 닉네임
  sender_id: "a1b2c3...",     // 치지직 시청자 고유 ID 해시값
  raw_msg: "!투표 강아지",      // 원본 전체 메시지
  time: 1724123456789L        // 메시지 타임스탬프 (밀리초)
}
```

#### 🧀 `minecraft:donation` 스토리지
```snbt
{
  player_name: "플레이어닉네임", // 방송 연동을 켠 마인크래프트 플레이어 이름
  player_uuid: "12345678-...", // 마인크래프트 플레이어 UUID
  cmd: "미션",                // !명령어 명칭
  chat: "다이아 10개 캐기",     // 후원 메시지 내용
  amount: 10000,              // 후원 금액 (치즈 개수 / 원)
  sender_nick: "후원자닉네임",  // 후원자 닉네임
  sender_id: "a1b2c3...",     // 치지직 후원자 고유 ID 해시값
  pay_type: "CHEESE",         // 결제 타입 (CHEESE 등)
  raw_msg: "!미션 다이아 10개 캐기",
  time: 1724123456789L
}
```

---

### 3. 매크로 펑션 작성 예시

#### 💬 채팅 예제 (1): `data/chklink/function/example_chat.mcfunction`
```mcfunction
# 1. 채팅 출력
$tellraw @a [{"text":"$(sender_nick)","color":"yellow"},{"text":": "},{"text":"$(cmd)","color":"aqua"},{"text":"$(chat)","color":"white"}]
```

#### 💬 채팅 명령어 감지 예제 (2): `data/chklink/function/example_chat2.mcfunction`
```mcfunction
# 2. 특정 채팅 !명령어 감지 (!다이아, !점프)
$execute if data storage minecraft:chat {cmd:"다이아"} run give $(player_name) diamond 1
$execute if data storage minecraft:chat {cmd:"점프"} run effect give $(player_name) jump_boost 5 2
```

#### 🧀 후원 알림 예제 (1): `data/chklink/function/example_donation.mcfunction`
```mcfunction
# 1. 후원 알림 출력 (어느 스트리머 방송의 후원인지 표시)
$tellraw @a [{"text":"[후원!] ","color":"green","bold":true},{"text":"$(player_name)님 방송에 ","color":"gray"},{"text":"$(sender_nick)","color":"yellow"},{"text":": ", "color":"yellow"},{"text":"$(cmd)","color":"aqua"},{"text":"$(chat)","color":"white"}," ",{"text":"$(amount)","color":"yellow"},{"text":"원","color":"yellow"}]
```

#### 🧀 후원 명령어 감지 예제 (2): `data/chklink/function/example_donation2.mcfunction`
```mcfunction
# 2. 후원 명령어 감지 및 금액 조건 (!소환 명령어 + 10,000원 후원 시 크리퍼 소환)
$execute if data storage minecraft:donation {cmd:"소환", amount:10000} as $(player_name) at @s run summon creeper ~ ~ ~
```

---

## 🤖 크레딧 및 안내 (Credits & AI Disclosure)

- 본 모드는 네이버(NAVER)의 공식 제품이 아니며, **AI 코딩 어시스턴트의 지원을 받아 개발된 오픈소스 비공식 모드**입니다.

## 📄 라이선스 (License)

This project is licensed under the [CC0 1.0 Universal (CC0-1.0)](LICENSE) License.
