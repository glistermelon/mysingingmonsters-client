# Box Activate Monster

Activates a monster, like a Wublin.
 The monster must be completely filled, or the request will fail.

## Command

`gs_box_activate_monster`

## Request

```{list-table}
:widths: 20 20
:header-rows: 1
* - Key
  - Type
* - user_monster_id
  - long
* - validate_only
  - integer
```

## Response

Though not listed here, the [standard response parameters](/topics/StandardResponseParameters) are included.
```{list-table}
:widths: 20 20
:header-rows: 1
* - Key
  - Type
* - user_monster_id
  - long
```

### Additional Responses

On success, [Update Monster Event](/events/gs_update_monster) is also sent.