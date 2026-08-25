execute store result score .current_time cbr_math run time query gametime

scoreboard players enable @a cooltime
scoreboard players enable @a blocks_per_chat
scoreboard players enable @a all_setting

# 전체 설정 변경 트리거
execute as @a[scores={all_setting=1..}] run function chat_block_rain:trigger/apply_all_setting
# 채팅당 블럭 수 변경 트리거
execute as @a[scores={blocks_per_chat=1..}] run function chat_block_rain:trigger/apply_blocks_per_chat
# 채팅 쿨타임 변경 트리거
execute as @a[scores={cooltime=1..}] run function chat_block_rain:trigger/apply_cooltime

scoreboard players set @a cooltime 0
scoreboard players set @a blocks_per_chat 0
scoreboard players set @a all_setting 0