# Start Remove Obstacle

Begins clearing of an obstacle (for a price).

## Command

`gs_start_obstacle`

## Request

```{list-table}
:widths: 20 20
:header-rows: 1
* - Key
  - Type
* - user_structure_id
  - long
```

## Response

Though not listed here, the [standard response parameters](/topics/StandardResponseParameters) are included.
```{list-table}
:widths: 20 20
:header-rows: 1
* - Key
  - Type
* - user_structure_id
  - long
* - user_structure
  - [Structure](/data/Structure)
```