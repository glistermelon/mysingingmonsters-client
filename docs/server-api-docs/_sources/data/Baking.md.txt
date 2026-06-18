# Baking



## Fields

```{list-table}
:widths: 20 20 60
:header-rows: 1
* - Key
  - Type
  - Description
* - user_baking_id
  - long
  - 
* - started_at
  - long
  - Timestamp when baking started
* - finished_at
  - long
  - Timestamp at which baking finishes
* - island
  - long
  - User island id of the island where the corresponding bakery is
* - user_structure
  - long
  - User structure id of the corresponding bakery
* - food_count
  - integer
  - How much food the baking will produce
* - food_option_id
  - integer
  - The ID of the bakery recipe
```

## Referenced By

* [Start Baking](/commands/gs_start_baking)