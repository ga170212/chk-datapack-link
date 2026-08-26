# cmd가 "설치"일 때만 안전하게 인자 매크로로 블록 설치
$execute as $(player_name) at @s run setblock ~$(arg1) ~ ~$(arg2) $(arg0)
