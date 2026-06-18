# Box Egg

Zaps an egg.

## Command

`gs_box_add_egg`

## Request

```{list-table}
:widths: 20 20 60
:header-rows: 1
* - Key
  - Type
  - Description
* - user_egg_id
  - long
  - Despite the name, this is actually the user breeding ID corresponding to the egg.
* - user_monster_id
  - long
  - The user monster ID of the monster to zap the egg into
* - underling
  - boolean
  - When tested with Wublins, this is always true.
```

## Response

Though not listed here, the [standard response parameters](/topics/StandardResponseParameters) are included.
```{list-table}
:widths: 20 20
:header-rows: 1
* - Key
  - Type
* - user_egg_id
  - long
* - user_monster_id
  - long
* - underling
  - boolean
* - dest_island_id
  - long
* - egg_type
  - integer
* - isWublin
  - boolean
* - has_all
  - boolean?
```

### Additional Responses

On success, [Update Monster Event](/events/gs_update_monster) is also sent.