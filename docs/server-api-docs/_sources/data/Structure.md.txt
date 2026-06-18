# Structure



## Fields

```{list-table}
:widths: 20 20 60
:header-rows: 1
* - Key
  - Type
  - Description
* - structure
  - integer
  - The structure type iD
* - island
  - long
  - The user island ID of the structure's island
* - user_structure_id
  - long
  - 
* - pos_x
  - integer
  - 
* - pos_y
  - integer
  - 
* - flip
  - integer
  - 
* - muted
  - integer
  - 
* - scale
  - double
  - 
* - in_warehouse
  - integer
  - 
* - is_upgrading
  - integer
  - 
* - is_complete
  - integer
  - 
* - last_collection
  - long?
  - A timestamp
* - date_created
  - long?
  - The timestamp at which a timer on the structure starts (for example, clearing an obstacle)
* - building_completed
  - long?
  - The timestamp at which a timer on the structure will finish (for example, clearing an obstacle)
```

## Referenced By

* [Buy Structure](/commands/gs_buy_structure)
* [Start Remove Obstacle](/commands/gs_start_obstacle)