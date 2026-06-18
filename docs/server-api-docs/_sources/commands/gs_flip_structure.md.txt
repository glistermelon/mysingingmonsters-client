# Flip Structure

Flips a structure.

## Command

`gs_flip_structure`

## Request

```{list-table}
:widths: 20 20
:header-rows: 1
* - Key
  - Type
* - user_structure_id
  - long
* - flipped
  - boolean
```

## Response

Though not listed here, the [standard response parameters](/topics/StandardResponseParameters) are included.
There are no other parameters.

### Additional Responses

On success, [Update Structure Event](/events/gs_update_structure) is also sent.