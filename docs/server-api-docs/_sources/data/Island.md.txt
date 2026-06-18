# Island



## Fields

```{list-table}
:widths: 20 20 60
:header-rows: 1
* - Key
  - Type
  - Description
* - island
  - integer
  - The island's type ID
* - date_created
  - long
  - 
* - user_island_id
  - long
  - 
* - likes
  - integer
  - 
* - dislikes
  - integer
  - 
* - light_torch_flag
  - boolean
  - Whether the island is marked for wishing torch lighting
* - warp_speed
  - double
  - The Time Machine speed
* - monsters
  - array\<[Monster](/data/Monster)\>
  - 
* - structures
  - array\<[Structure](/data/Structure)\>
  - 
* - baking
  - array\<[Baking](/data/Baking)\>
  - 
* - eggs
  - array\<[Monster Egg](/data/MonsterEgg)\>
  - 
* - breeding
  - array\<[Breeding](/data/Breeding)\>
  - 
```

## Referenced By

* [Buy Island](/commands/gs_buy_island)
* [Visit Data](/data/VisitData)
* [Player](/data/Player)