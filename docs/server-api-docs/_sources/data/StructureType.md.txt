# Structure Type



## Fields

```{list-table}
:widths: 20 20 60
:header-rows: 1
* - Key
  - Type
  - Description
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
* - structure_type
  - string
  - One of `nursery`, `breeding`, `mine`, `castle`, `bakery`, `decoration`, `obstacle`, `time_machine`, `happiness_tree` (unity tree), `warehouse`, `hotel`, `torch` (wishing torch), `recording_studio`, `buddy` (glowbe), `fuzer`, `battle_gym`, `crucible`, `awakener`, `attuner`, `synthesizer`, `nucleus`, `dish_harmonizer`, `polarity_amplifier`, `fugue`
* - size_x
  - integer
  - 
* - size_y
  - integer
  - 
* - build_time
  - integer
  - 
* - name
  - string
  - 
* - description
  - string
  - 
* - structure_id
  - integer
  - 
* - entity_id
  - integer
  - 
* - xp
  - integer
  - 
* - movable
  - integer
  - 
* - sellable
  - integer
  - 
* - upgrades_to
  - integer
  - Structure type ID that this structure can be upgraded to, or 0 if it can't upgrade
* - allowed_on_island
  - string?
  - Serialized JSON to be deserialized as array\<integer\>. Serialized JSON array of island type IDs
```

## Referenced By

* [Query Structures Database](/commands/db_structure)