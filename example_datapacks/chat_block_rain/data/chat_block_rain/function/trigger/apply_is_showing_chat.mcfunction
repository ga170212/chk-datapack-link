execute as @a[scores={is_showing_chat=2..}] run scoreboard players set .is_showing_chat cbr_math 2
execute as @a[scores={is_showing_chat=1}] run scoreboard players set .is_showing_chat cbr_math 1

execute as @a[scores={is_showing_chat=2..}] run tellraw @a [{"color":white,"text":""},"채팅이 표시됩니다"]
execute as @a[scores={is_showing_chat=1}] run tellraw @a [{"color":white,"text":""},"채팅이 표시되지 않습니다"]

execute as @a[scores={is_showing_chat=1..}] store result score .is_showing_chat cbr_math run scoreboard players get @s is_showing_chat