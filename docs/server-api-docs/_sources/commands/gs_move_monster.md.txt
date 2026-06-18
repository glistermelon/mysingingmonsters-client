# Move Monster

Moves a monster and sets its volume.

## Command

`gs_move_monster`

## Request

```{list-table}
:widths: 20 20
:header-rows: 1
* - Key
  - Type
* - user_monster_id
  - long
* - pos_x
  - integer
* - pos_y
  - integer
* - volume
  - double
```

### Typical Additional Requests

After a successful response, the client should send [Multi Update Monster](/commands/gs_multi_neighbors).

## Response

Though not listed here, the [standard response parameters](/topics/StandardResponseParameters) are included.
There are no other parameters.

### Additional Responses

On success, [Update Monster Event](/events/gs_update_monster) is also sent.