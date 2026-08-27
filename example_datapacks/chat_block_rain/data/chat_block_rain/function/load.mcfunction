scoreboard objectives add cbr_math dummy
scoreboard objectives add cbr_sender_cooltimes dummy

scoreboard objectives add blocks_per_chat trigger
scoreboard objectives add cooltime trigger
scoreboard objectives add is_showing_chat trigger
scoreboard objectives add all_setting trigger

execute unless score .cooltime cbr_math = .cooltime cbr_math \
run scoreboard players set .cooltime cbr_math 200
execute unless score .blocks_per_chat cbr_math = .blocks_per_chat cbr_math \
run scoreboard players set .blocks_per_chat cbr_math 2
execute unless score .is_showing_chat cbr_math = .is_showing_chat cbr_math \
run scoreboard players set .is_showing_chat cbr_math 2

scoreboard players set #20 cbr_math 20
scoreboard players set #10 cbr_math 10
scoreboard players reset .temp cbr_math
scoreboard players set .time_diff cbr_math 0
execute store result score .current_time cbr_math run time query gametime

# 스토리지 생성
function chat_block_rain:init/block_dict
data modify storage cbr_dict temp set value {block:"a", offset_x:"0", offset_z:"0"}

# 안내문구 출력
tellraw @a ["",\
{"text":"\n===============================\n","color":"gold","bold":true},\
{"text":"  [ 🌧️ 치지직 채팅 블록 소환 데이터팩 ]\n","color":"yellow","bold":true},\
{"text":" F6키로 채널 연동 필수 (재접속시 재연동 필요)\n"},\
{"text":" 📺 시청자 참여 안내\n","color":"aqua","bold":true},\
{"text":"  • ","color":"dark_gray"},{"text":"!블록소환 [블록이름]","color":"green","bold":true},{"text":" : 띄어쓰기 없는 공식 한국어 명칭 (참나무반블록) 또는 블록 ID (oak_slab)\n","color":"white"},\
{"text":" ⚙️ 스트리머 설정 (단축키 : G)\n","color":"light_purple","bold":true},\
{"text":"  • ","color":"dark_gray"},{"text":"[소환 개수 설정]","color":"yellow","underlined":true,"click_event":{"action":"suggest_command","command":"/trigger blocks_per_chat set "},"hover_event":{"action":"show_text","value":[{"text":"채팅 1회당 떨어질 블록 개수 (1~6개, 기본 2개)"}]}},{"text":" : 기본 2개 (최대 6개)\n","color":"gray"},\
{"text":"  • ","color":"dark_gray"},{"text":"[쿨타임 설정]","color":"yellow","underlined":true,"click_event":{"action":"suggest_command","command":"/trigger cooltime set "},"hover_event":{"action":"show_text","value":[{"text":"시청자별 쿨타임 설정 (초 단위, 기본 10초)"}]}},{"text":" : 기본 10초 (최대 60초)\n","color":"gray"},\
{"text":"===============================","color":"gold","bold":true}]
