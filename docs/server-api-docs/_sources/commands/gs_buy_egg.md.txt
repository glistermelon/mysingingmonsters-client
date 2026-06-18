# Buy Egg

Buys an egg from the market and starts incubating it in a nursery.
 ```{warning}
 On islands where incubation of an egg is not required, like Wublin Island,
 monsters are bought via Hatch Egg.
 ```

## Command

`gs_buy_egg`

## Request

```{list-table}
:widths: 20 20 60
:header-rows: 1
* - Key
  - Type
  - Description
* - monster_id
  - integer
  - Monster species ID
* - quest_claim_id
  - long
  - 
* - starpower_purchase
  - boolean
  - 
* - structure_id
  - long
  - Nursery user structure id
```

## Response

Though not listed here, the [standard response parameters](/topics/StandardResponseParameters) are included.
```{list-table}
:widths: 20 20
:header-rows: 1
* - Key
  - Type
* - user_egg
  - [Monster Egg](/data/MonsterEgg)
* - remove_buyback
  - boolean
```