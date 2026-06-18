# Hatch Egg

On islands that have nurseries, this hatches an egg that has finished incubating.
 On islands that do not have nurseries, this is used directly to buy a monster.

## Command

`gs_hatch_egg`

## Request

```{list-table}
:widths: 20 20 60
:header-rows: 1
* - Key
  - Type
  - Description
* - costume
  - integer
  - 
* - flip
  - integer
  - 
* - pos_x
  - integer
  - 
* - pos_y
  - integer
  - 
* - store_in_hotel
  - boolean
  - 
* - user_egg_id
  - long
  - On islands that have nurseries, the nursery egg's user egg ID. On islands without nurseries, the monster species ID.
```

### Typical Additional Requests

After a successful response, the client should send [Multi Update Monster](/commands/gs_multi_neighbors).

## Response

Though not listed here, the [standard response parameters](/topics/StandardResponseParameters) are included.
```{list-table}
:widths: 20 20
:header-rows: 1
* - Key
  - Type
* - directPlace
  - boolean
* - island
  - long
* - user_egg_id
  - long
* - create_in_storage
  - boolean
* - monster
  - [Monster](/data/Monster)
```