# Buy Island

Buys an island.

## Command

`gs_buy_island`

## Request

```{list-table}
:widths: 20 20 60
:header-rows: 1
* - Key
  - Type
  - Description
* - island_id
  - integer
  - Island type ID
* - island_name
  - string
  - If purchasing Tribal Island, the name of the tribe to be created. Otherwise, any value is accepted, but the authentic client would pass an empty string.
* - starpower_purchase
  - boolean
  - 
```

## Response

Though not listed here, the [standard response parameters](/topics/StandardResponseParameters) are included.
```{list-table}
:widths: 20 20
:header-rows: 1
* - Key
  - Type
* - user_island
  - [Island](/data/Island)
```