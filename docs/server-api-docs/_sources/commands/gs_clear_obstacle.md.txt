# Clear Obstacle

Finishes clearing of an obstacle.
 Fails if the removal timer hasn't finished yet.

## Command

`gs_clear_obstacle`

## Request

```{list-table}
:widths: 20 20
:header-rows: 1
* - Key
  - Type
* - user_structure_id
  - long
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
* - user_structure_id
  - long
```