# Buy Structure

Buys and places a structure.

## Command

`gs_buy_structure`

## Request

```{list-table}
:widths: 20 20 60
:header-rows: 1
* - Key
  - Type
  - Description
* - flip
  - integer
  - 
* - pos_x
  - integer
  - 
* - pos_y
  - integer
  - 
* - quest_claim_id
  - long
  - 
* - scale
  - double
  - 
* - starpower_purchase
  - boolean
  - 
* - structure_id
  - integer
  - Structure type ID
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
* - user_structure
  - [Structure](/data/Structure)
```