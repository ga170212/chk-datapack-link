$execute if data storage minecraft:chat {cmd:"설치"} as $(player_name) at @s run setblock ~$(arg1) ~ ~$(arg2) $(arg0)
$execute if data storage minecraft:chat {cmd:"다이아"} run give $(player_name) diamond 1
$execute if data storage minecraft:chat {cmd:"점프"} run effect give $(player_name) jump_boost 5 2