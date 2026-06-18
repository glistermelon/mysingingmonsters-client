# Bakery Recipe



## Fields

```{list-table}
:widths: 20 20 60
:header-rows: 1
* - Key
  - Type
  - Description
* - id
  - integer
  - 
* - label
  - string
  - The key for the local text resource corresponding to the recipe's name
* - food
  - integer
  - How much food one gets from baking it
* - cost
  - integer
  - How much it costs, in coins, to bake
* - xp
  - integer
  - How much XP one gets from baking it
* - time
  - integer
  - How long it takes to bake, in seconds
* - always_avail
  - integer
  - Whether or not the recipe is always available (i.e., not seasonal)
```

## Referenced By

* [Query Bakery Foods Database](/commands/db_bakery_foods)