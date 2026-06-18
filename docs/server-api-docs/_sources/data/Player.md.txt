# Player



## Fields

```{list-table}
:widths: 20 20 60
:header-rows: 1
* - Key
  - Type
  - Description
* - user_id
  - integer
  - 
* - country
  - string
  - The user's country's [ISO 3166-1 alpha-2](https://en.wikipedia.org/wiki/ISO_3166-1_alpha-2) code
* - display_name
  - string
  - 
* - coins_actual
  - long
  - 
* - diamonds_actual
  - long
  - 
* - ethereal_currency_actual
  - long
  - 
* - food_actual
  - long
  - 
* - keys_actual
  - long
  - 
* - relics_actual
  - long
  - 
* - starpower_actual
  - long
  - 
* - egg_wildcards_actual
  - long
  - 
* - battle
  - [Battle Data](/data/[Battle Data](/data/BattleData))
  - 
* - level
  - integer
  - 
* - xp
  - integer
  - 
* - islands
  - array\<[Island](/data/Island)\>
  - 
* - active_island
  - long
  - 
* - profile
  - [Player Profile](/data/PlayerProfile)
  - 
* - diamonds_spent
  - integer
  - Currently, the below aren't actually used by the client.
```

## Referenced By

* [Player](/commands/gs_player)