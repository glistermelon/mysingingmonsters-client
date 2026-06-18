# Start Baking

Starts baking a recipe of choice in a bakery of choice.

## Command

`gs_start_baking`

## Request

```{list-table}
:widths: 20 20 60
:header-rows: 1
* - Key
  - Type
  - Description
* - user_structure_id
  - long
  - User structure ID of the bakery
* - food_id
  - integer
  - The bakery recipe ID
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
* - user_baking
  - [Baking](/data/Baking)
```