$execute positioned ~$(offset_x) ~ ~$(offset_z) if block ~ ~ ~ #air run return run summon falling_block ~ ~ ~ {BlockState:{Name:"minecraft:$(block)"},Time:1,DropItem:0b}

execute unless block ~ ~ ~ #air positioned ~ ~-2 ~ run function chat_block_rain:summon_falling_block with storage cbr_dict temp