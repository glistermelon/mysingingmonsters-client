# Breeding



## Fields

```{list-table}
:widths: 20 20 60
:header-rows: 1
* - Key
  - Type
  - Description
* - user_breeding_id
  - long
  - 
* - island
  - long
  - User island id of the island where the breeding structure is
* - structure
  - long
  - User structure id of the breeding structure
* - monster_1
  - integer
  - Species ID of the first monster bred
* - monster_2
  - integer
  - Species ID of the second monster bred
* - new_monster
  - integer
  - Species ID of the resulting monster
* - started_on
  - long
  - Timestamp at which breeding started
* - complete_on
  - long
  - Timestamp at which breeding finishes
```

## Referenced By

* [Breed Monsters](/commands/gs_breed_monsters)