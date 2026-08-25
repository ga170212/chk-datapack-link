# $execute if data storage minecraft:chat {cmd:"블록소환"} as $(player_name) at @s align xz run summon falling_block ~0.5 ~6 ~0.5 {BlockState:{Name:"minecraft:$(chat)"},Time:1}

# $cmd가 !블록소환 인지 검사
    # !블록소환이 아니면 리턴
    execute unless data storage minecraft:chat {cmd:"블록소환"} run return 0

# 블럭 소환 쿨타임 검사
scoreboard players operation .time_diff cbr_math = .current_time cbr_math
$scoreboard players operation .time_diff cbr_math -= $(sender_id) cbr_sender_cooltimes
    # 쿨타임이면 리턴 -- $sender님은 쿨타임 중입니다 -- tellraw로 수정하기
    # execute unless score .time_diff cbr_math > .cooltime cbr_math run return run say 쿨타임!
    $execute unless score .time_diff cbr_math > .cooltime cbr_math run return run tellraw $(player_name) [{"color":"green","interpret":true,"nbt":"sender_nick","storage":"chat"},{"color":"white","text":"님 쿨타임 중입니다"}]
# 금지 블럭인지 검사
data remove storage cbr_dict temp.block
$data modify storage minecraft:cbr_dict temp.block set from storage minecraft:cbr_dict map.banned_blocks.$(chat)
    # 금지 블럭이면 리턴 -- $sender님 $chat은 금지 블럭입니다
    $execute if data storage cbr_dict temp.block run return run tellraw $(player_name) [{"color":"green","interpret":true,"nbt":"sender_nick","storage":"chat"},{"color":"white","text":"님 "},{"color":"dark_red","interpret":true,"nbt":"chat","storage":"chat"},{"color":"white","text":"은 금지 블록입니다"}]

# 있는 블럭인지 검사
data remove storage cbr_dict temp.block
$data modify storage minecraft:cbr_dict temp.block set from storage minecraft:cbr_dict map.blocks.$(chat)
    # 없는 블럭이면 리턴 -- $sender님 $chat은 없는 블럭입니다
    $execute unless data storage cbr_dict temp.block run return run tellraw $(player_name) [{"color":"green","interpret":true,"nbt":"sender_nick","storage":"chat"},{"color":"white","text":"님 "},{"color":"red","interpret":true,"nbt":"chat","storage":"chat"},{"color":"white","text":"은 없는 블록입니다"}]

# 블럭 소환 실행 -- $sender님이 $chat 블럭을 소환하셨습니다
$tellraw $(player_name) [{"color":"green","interpret":true,"nbt":"sender_nick","storage":"chat"},{"color":"white","text":"님이 "},{"color":"aqua","interpret":true,"nbt":"chat","storage":"chat"},{"color":"white","text":"을 소환했습니다"}]

$execute as $(player_name) at @s run function chat_block_rain:set_falling_block_pos
$execute if score .blocks_per_chat cbr_math matches 2.. as $(player_name) at @s run function chat_block_rain:set_falling_block_pos
$execute if score .blocks_per_chat cbr_math matches 3.. as $(player_name) at @s run function chat_block_rain:set_falling_block_pos
$execute if score .blocks_per_chat cbr_math matches 4.. as $(player_name) at @s run function chat_block_rain:set_falling_block_pos
$execute if score .blocks_per_chat cbr_math matches 5.. as $(player_name) at @s run function chat_block_rain:set_falling_block_pos
$execute if score .blocks_per_chat cbr_math matches 6.. as $(player_name) at @s run function chat_block_rain:set_falling_block_pos


# 쿨타임 기록 -- 테스트계정은 쿨타임 비활성화해둠
$execute unless data storage minecraft:chat {sender_id:"test_hash"} store result score $(sender_id) cbr_sender_cooltimes run scoreboard players get .current_time cbr_math