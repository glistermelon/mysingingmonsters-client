# Monster Species



## Fields

```{list-table}
:widths: 20 20 60
:header-rows: 1
* - Key
  - Type
  - Description
* - monster_id
  - integer
  - Monster species ID
* - entity_id
  - integer
  - 
* - cost_coins
  - integer
  - 
* - cost_keys
  - integer
  - 
* - cost_relics
  - integer
  - 
* - cost_diamonds
  - integer
  - 
* - cost_starpower
  - integer
  - 
* - cost_medals
  - integer
  - 
* - cost_eth_currency
  - integer
  - 
* - size_x
  - integer
  - 
* - size_y
  - integer
  - 
* - build_time
  - integer
  - Incubation time (without enhancements)
* - name
  - string
  - Key for the local text resource corresponding to the monster's name
* - description
  - string
  - Key for the local text resource corresponding to the monster's description; typically name+"_DESC"
* - common_name
  - string
  - A distinct English name for the species
* - fam
  - string
  - Family
* - class
  - string
  - 
* - genes
  - string
  - Each letter in the string corresponds to an <[Gene](/data/Gene)> (an element).
* - view_in_market
  - integer
  - Whether or not the monster is visible in the market (not including the starmarket or buybacks).
* - view_in_starmarket
  - integer
  - Whether or not the monster is visible in the starmarket.
* - happiness
  - array\<[Monster Like](/data/MonsterLike)\>
  - The monster's likes.
* - xp
  - integer
  - 
* - beds
  - integer
  - 
* - discovered_by
  - string?
  - Who "discovered" the monster. Defaults to "DISCOVERED_BY_COMMUNITY"
* - names
  - array\<string\>
  - The default names for the monster.
* - levels
  - array\<[Monster Level](/data/MonsterLevel)\>?
  - 
* - time_to_fill_sec
  - integer
  - For monsters that have an activation time limit, the number of seconds the user has to fill the monster after starting.
```

## Referenced By

* [Query Monsters Database](/commands/db_monster)