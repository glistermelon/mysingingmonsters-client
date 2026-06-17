# Monster



## Fields

```{list-table}
:widths: 20 20 60
:header-rows: 1
* - Key
  - Type
  - Description
* - monster
  - integer
  - The monster's species ID
* - island
  - long
  - The user island ID of the monster's island
* - user_monster_id
  - long
  - 
* - name
  - string
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
* - volume
  - double
  - 
* - level
  - integer
  - 
* - times_fed
  - integer
  - Not cumulative; indicates progress towards the next level
* - happiness
  - integer
  - The monster's happiness as a percentage. Can be negative (Wublin polarity is happiness under the hood)
* - in_hotel
  - integer
  - 
* - last_collection
  - long
  - 
* - last_feeding
  - long
  - 
* - collection_type
  - string?
  - Certain monsters, like Wublins, indicate what kind of currency will be collected at next collection
* - egg_timer_start
  - long?
  - When the timer started on filling a timed deactivated monster (like a Wublin)
* - box_requirements
  - string?
  - Serialized JSON to be deserialized as array\<integer\>. An array of monster species IDs, where an ID occurs as many times in the list as it is required to be zapped
* - boxed_eggs
  - string?
  - Serialized JSON to be deserialized as array\<integer\>. An array of monster species IDs, where an ID occurs as many times in the list as it has already been zapped. This field is no longer present after activation (before activation, it is always present and can be empty).
* - random_underling_collection_min
  - integer?
  - Duration (in minutes) after the last collection time at which currency is available for collection
```

## Referenced By

* [Hatch Egg](/commands/gs_hatch_egg)