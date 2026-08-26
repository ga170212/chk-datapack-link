# 💡 [안전 팁] 잘못된 인자 입력 시 매크로 구문 에러로 다음 펑션이 멈추는 것을 방지하기 위해 서브 펑션으로 분리합니다.
execute if data storage minecraft:chat {cmd:"설치"} run function chklink:do_setblock with storage minecraft:chat

# 특정 단일 채팅 !명령어 감지 (!다이아, !점프)
$execute if data storage minecraft:chat {cmd:"다이아"} run give $(player_name) diamond 1
$execute if data storage minecraft:chat {cmd:"점프"} run effect give $(player_name) jump_boost 5 2