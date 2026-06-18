# Move Structure

Moves and scales a structure and sets its volume.

## Command

`gs_move_structure`

## Request

```{list-table}
:widths: 20 20
:header-rows: 1
* - Key
  - Type
* - user_structure_id
  - long
* - pos_x
  - integer
* - pos_y
  - integer
* - scale
  - double
* - volume
  - float
```

### Typical Additional Requests

After a successful response, the client should send [Multi Update Monster](/commands/gs_multi_neighbors).

## Response

Though not listed here, the [standard response parameters](/topics/StandardResponseParameters) are included.
There are no other parameters.

### Additional Responses

On success, [Update Structure Event](/events/gs_update_structure) is also sent.