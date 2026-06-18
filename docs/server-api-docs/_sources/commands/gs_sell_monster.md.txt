# Sell Monster

Sells a monster.

## Command

`gs_sell_monster`

## Request

```{list-table}
:widths: 20 20
:header-rows: 1
* - Key
  - Type
* - user_monster_id
  - long
* - pure_destroy
  - boolean
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
* - user_monster_id
  - long
```