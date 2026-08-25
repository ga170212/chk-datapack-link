execute as @a[scores={cooltime=1..60}] run execute store result score .cooltime cbr_math run scoreboard players get @s cooltime
execute as @a[scores={cooltime=61..}] run scoreboard players set .cooltime cbr_math 60
execute as @a[scores={cooltime=1..}] run tellraw @a [{"color":white,"text":""},"채팅의 쿨타임은 이제 ",{"color":"aqua","score":{"name":".cooltime","objective":"cbr_math"}},"초 입니다 (기본 10초)"]
execute as @a[scores={cooltime=1..}] run scoreboard players operation .cooltime cbr_math *= #20 cbr_math