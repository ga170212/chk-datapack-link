say 1

scoreboard players operation temp cbr_math = @s all_setting
scoreboard players operation temp cbr_math /= #10 cbr_math
scoreboard players operation @s cooltime = temp cbr_math

scoreboard players operation temp cbr_math = @s all_setting
scoreboard players operation temp cbr_math %= #10 cbr_math
scoreboard players operation @s blocks_per_chat = temp cbr_math

scoreboard players reset temp cbr_math