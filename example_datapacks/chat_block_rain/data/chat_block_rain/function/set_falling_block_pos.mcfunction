execute store result storage cbr_dict temp.offset_x int 1 run random value -24..24
execute store result storage cbr_dict temp.offset_z int 1 run random value -24..24

execute unless dimension the_nether align xz positioned ~0.5 ~30 ~0.5 run function chat_block_rain:summon_falling_block with storage cbr_dict temp

execute if dimension the_nether align xz positioned ~0.5 ~30 ~0.5 if predicate {"condition":"minecraft:location_check","predicate":{"position":{"y":{"min":127}}}} positioned ~ 127 ~ run function chat_block_rain:summon_falling_block with storage cbr_dict temp

execute if dimension the_nether align xz positioned ~0.5 ~30 ~0.5 unless predicate {"condition":"minecraft:location_check","predicate":{"position":{"y":{"min":127}}}} run function chat_block_rain:summon_falling_block with storage cbr_dict temp
