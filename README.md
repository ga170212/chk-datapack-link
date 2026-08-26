# 🧀 치지직 데이터팩 링크 (Chzzk Datapack Link)

[![Minecraft](https://img.shields.io/badge/Minecraft-26.2-blue?logo=minecraft)](https://minecraft.net/)
[![Fabric](https://img.shields.io/badge/Modloader-Fabric-orange?logo=fabric)](https://fabricmc.net/)
[![AI Assisted](https://img.shields.io/badge/AI-Assisted-8A2BE2?logo=google&logoColor=white)](https://github.com/ga170212/chk-datapack-link)
[![License](https://img.shields.io/badge/License-CC0--1.0-green)](LICENSE)

치지직(Chzzk) 라이브 방송의 채팅과 후원 이벤트를 마인크래프트 **NBT 스토리지 및 데이터팩과 실시간으로 연동**해주는 패브릭(Fabric) 모드입니다.

> ⚠️ **안내**: 본 모드는 네이버(NAVER)의 공식 모드가 아닌 비공식 오픈소스 서드파티 모드이며, AI 코딩 어시스턴트의 지원을 받아 제작되었습니다.  
> 💡 **필수**: 본 모드는 치지직 이벤트를 마인크래프트 스토리지로 전달하는 **브릿지(Bridge) 모드**이므로, 이벤트를 받아 실행할 **데이터팩이 필요**합니다.

---

## 🌐 English Overview

A Minecraft Fabric bridge mod that connects **Chzzk (NAVER live streaming)** chat & donation events directly to Minecraft **NBT storage and datapacks** in real time.

- **Datapack-Driven**: Triggers `#chklink:chat` and `#chklink:donation` function tags with macro storage data when chat or donation events occur.
- **Smart Parsing**: Automatically parses commands (`cmd`), messages (`chat`), and comma-separated arguments (`arg0`, `arg1`, `arg2`, `arg3`, `arg4`, `arg5`).
- **Multiplayer Support**: Each player on a server can connect their own Chzzk channel independently.
- **Mod Menu & Keybinding**: Open the channel configuration GUI easily via **`F6`** or through **Mod Menu**.
- **In-Game Testing**: Simulate incoming chats and donations using `/chklink testchat` and `/chklink testdonation`.

---

## 📖 개요

치지직 방송의 채팅과 후원을 마인크래프트 데이터팩으로 받아 처리할 수 있게 해주는 모드입니다.

별도의 자바 코딩 없이 데이터팩 펑션(`.mcfunction`)만 작성하면 시청자 참여 콘텐츠(채팅으로 블록 소환, 후원 시 몹 소환, 투표 등)를 바로 구현할 수 있습니다.

---

## ✨ 주요 기능

- **데이터팩 연동**: 채팅이나 후원이 오면 스토리지에 데이터를 병합하고 `#chklink:chat`, `#chklink:donation` 태그에 등록된 펑션들을 매크로와 함께 자동 실행합니다.
- **명령어 및 인자 자동 분리**:
  - `!소환 좀비 3` ➔ `cmd: "소환"`, `chat: "좀비 3"`
  - `!소환, 좀비, 3` ➔ `cmd: "소환"`, `arg0: "좀비"`, `arg1: "3"`, `arg_count: 2` 자동 제공
- **멀티플레이 지원**: 서버에 여러 플레이어가 있어도 각자 본인의 치지직 채널 ID로 개별 연동할 수 있습니다.
- **인게임 설정창 & Mod Menu 연동**: 인게임에서 **`F6`** 키를 누르거나 **Mod Menu**의 모드 목록 설정 버튼을 통해 채널 ID를 입력하고 연동을 켤 수 있습니다.
- **테스트 명령어**: `/chklink testchat`, `/chklink testdonation` 명령어로 방송을 켜지 않고도 데이터팩 동작을 바로 테스트할 수 있습니다.

---

## 📋 요구 사항

- **Minecraft**: 26.2
- **Mod Loader**: [Fabric Loader](https://fabricmc.net/)
- **필수 모드**: [Fabric API](https://modrinth.com/mod/fabric-api)
- **필수 데이터팩**: 치지직 이벤트를 처리할 데이터팩 ([`example_datapacks`](example_datapacks) 내 샘플 데이터팩 참고)

---

## 🎮 사용 방법

**1. 데이터팩 적용하기 (필수)**
- 레포지토리의 **[`example_datapacks/chklink_sample`](example_datapacks/chklink_sample)** 폴더(또는 나만의 커스텀 데이터팩)를 마인크래프트 월드의 `datapacks/` 폴더에 넣고 월드 내에서 `/reload`를 입력합니다.

**2. 치지직 채널 ID 확인하기**
- 치지직 스트리머의 방송국 URL 주소에서 채널 ID(32자리 해시값)를 복사합니다.  
  *(예: `https://chzzk.naver.com/live/0123456789abcdef0123456789abcdef` ➔ `0123456789abcdef0123456789abcdef`)*

**3. 인게임에서 연동하기**
- 게임 내에서 **`F6`** 키(또는 Mod Menu 설정)를 눌러 설정창을 엽니다.
- 복사한 **채널 ID**를 입력하고 **[채널 ID 저장]**을 누른 뒤 **[연동 시작]**을 클릭합니다.

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

## 🛠️ 데이터팩 가이드

모드는 치지직 이벤트가 발생할 때마다 스토리지에 데이터를 병합한 후, 해당하는 **펑션 태그**를 매크로와 함께 실행합니다.  
처음 제작하신다면 **[`example_datapacks/chklink_sample`](example_datapacks/chklink_sample)** 구조를 참고하여 제작해 보세요!

**1. 펑션 태그 등록하기**

💬 **채팅 태그**: `data/chklink/tags/function/chat.json`
```json
{
  "values": [
    "chklink:example_chat",
    "chklink:example_chat2"
  ]
}
```

🧀 **후원 태그**: `data/chklink/tags/function/donation.json`
```json
{
  "values": [
    "chklink:example_donation",
    "chklink:example_donation2"
  ]
}
```

---

**2. 스토리지 NBT 구조 명세**

💬 **`minecraft:chat` 스토리지**
```snbt
{
  player_name: "플레이어닉네임", // 방송 연동을 켠 마인크래프트 플레이어 이름
  player_uuid: "12345678-...", // 마인크래프트 플레이어 UUID
  cmd: "설치",                // !명령어 명칭 (예: !설치 sand, 10, 20 -> "설치")
  chat: "sand, 10, 20",       // 명령어 제외 전체 본문
  
  // 쉼표(,) 구분 인자 필드
  args: ["sand", "10", "20"], // 인자 전체 NBT 문자열 리스트
  arg0: "sand",               // 0번째 인자
  arg1: "10",                 // 1번째 인자
  arg2: "20",                 // 2번째 인자
  arg3: "",                   // (없으면 빈 문자열)
  arg4: "",
  arg5: "",
  arg_count: 3,               // 총 인자 개수
  
  sender_nick: "시청자닉네임",  // 치지직 시청자(보낸 사람) 닉네임
  sender_id: "a1b2c3...",     // 치지직 시청자 고유 ID 해시값
  raw_msg: "!설치 sand, 10, 20",// 원본 전체 메시지
  time: 1724123456789L        // 메시지 타임스탬프 (밀리초)
}
```

🧀 **`minecraft:donation` 스토리지**
```snbt
{
  player_name: "플레이어닉네임", // 방송 연동을 켠 마인크래프트 플레이어 이름
  player_uuid: "12345678-...", // 마인크래프트 플레이어 UUID
  cmd: "미션",                // !명령어 명칭
  chat: "다이아, 10",         // 후원 메시지 내용
  amount: 10000,              // 후원 금액 (치즈 개수 / 원)
  
  // 쉼표(,) 구분 인자 필드
  args: ["다이아", "10"],
  arg0: "다이아",
  arg1: "10",
  arg2: "",
  arg3: "",
  arg4: "",
  arg5: "",
  arg_count: 2,
  
  sender_nick: "후원자닉네임",  // 후원자 닉네임
  sender_id: "a1b2c3...",     // 치지직 후원자 고유 ID 해시값
  pay_type: "CHEESE",         // 결제 타입 (CHEESE 등)
  raw_msg: "!미션 다이아, 10",
  time: 1724123456789L
}
```

---

**3. 매크로 펑션 작성 예시**

💬 **채팅 출력 예제**: `data/chklink/function/example_chat.mcfunction`
```mcfunction
$tellraw @a [{"text":"$(sender_nick)","color":"yellow"},{"text":": "},{"text":"$(cmd)","color":"aqua"},{"text":"$(chat)","color":"white"}]
```

💬 **채팅 인자 기반 명령어 예제**: `data/chklink/function/example_chat2.mcfunction`
> 💡 **안전 팁**: 잘못된 인자 입력 시 매크로 구문 에러로 인한 실행 중단을 방지하기 위해, 인자 매크로는 `cmd` 검사 후 서브 펑션으로 실행하는 것이 안전합니다.
```mcfunction
// cmd가 "설치"일 때만 서브 펑션을 호출하여 안전하게 블록 설치
execute if data storage minecraft:chat {cmd:"설치"} run function chklink:do_setblock with storage minecraft:chat

// 특정 단일 채팅 !명령어 감지 (!다이아, !점프)
$execute if data storage minecraft:chat {cmd:"다이아"} run give $(player_name) diamond 1
$execute if data storage minecraft:chat {cmd:"점프"} run effect give $(player_name) jump_boost 5 2
```

💬 **블록 설치 서브 펑션**: `data/chklink/function/do_setblock.mcfunction`
```mcfunction
// cmd가 "설치"일 때만 안전하게 호출되어 블록 설치 실행
$execute as $(player_name) at @s run setblock ~$(arg1) ~ ~$(arg2) $(arg0)
```

🧀 **후원 알림 예제**: `data/chklink/function/example_donation.mcfunction`
```mcfunction
$tellraw @a [{"text":"[후원!] ","color":"green","bold":true},{"text":"$(player_name)님 방송에 ","color":"gray"},{"text":"$(sender_nick)","color":"yellow"},{"text":": ", "color":"yellow"},{"text":"$(cmd)","color":"aqua"},{"text":"$(chat)","color":"white"}," ",{"text":"$(amount)","color":"yellow"},{"text":"원","color":"yellow"}]
```

🧀 **후원 명령어 감지 예제**: `data/chklink/function/example_donation2.mcfunction`
```mcfunction
// 후원 명령어 감지 및 금액 조건 (!소환 + 10,000원 후원 시 크리퍼 소환)
$execute if data storage minecraft:donation {cmd:"소환", amount:10000} as $(player_name) at @s run summon creeper ~ ~ ~
```

---

## 🤖 크레딧 (Credits)

- 본 모드는 네이버(NAVER)의 공식 제품이 아니며, **AI 코딩 어시스턴트의 지원을 받아 개발된 오픈소스 비공식 모드**입니다.

## 📄 라이선스 (License)

This project is licensed under the [CC0 1.0 Universal (CC0-1.0)](LICENSE) License.
