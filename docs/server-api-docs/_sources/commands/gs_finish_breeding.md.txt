# Finish Breeding

For a breeding structure that has finished breeding,
 moves the egg to a nursery (not of the user's choice).

## Command

`gs_finish_breeding`

## Request

```{list-table}
:widths: 20 20 60
:header-rows: 1
* - Key
  - Type
  - Description
* - user_breeding_id
  - long
  - 
* - speedup
  - boolean
  - 
* - purchase_type
  - integer
  - In my experience this has always been 47.
```

## Response

Though not listed here, the [standard response parameters](/topics/StandardResponseParameters) are included.
```{list-table}
:widths: 20 20
:header-rows: 1
* - Key
  - Type
* - user_breeding_id
  - long
* - user_structure_id
  - long
* - user_egg
  - [Monster Egg](/data/MonsterEgg)
```