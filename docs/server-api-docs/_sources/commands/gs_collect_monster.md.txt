# Collect Monster

Collects from a monster.
 Fails if there is nothing to be collected.
 ```{warning}
 If a monster has accumulated 0 coins/shards/etc., the action is considered a failure by the server,
 and it sets an error message containing "nothing to collect".
 The full error message is typically something like "Normal monster: nothing to collect".
 It is easier to catch this error and deal with it than it is to approximate when a monster will be ready to collect.
 ```
 ```{warning}
 For a deactivated Wublin, this command sells all its eggs.
 ```

## Command

`gs_collect_monster`

## Request

```{list-table}
:widths: 20 20
:header-rows: 1
* - Key
  - Type
* - user_monster_id
  - long
```

## Response

Though not listed here, the [standard response parameters](/topics/StandardResponseParameters) are included.
```{list-table}
:widths: 20 20
:header-rows: 1
* - Key
  - Type
* - coins
  - integer?
* - food
  - integer?
* - keys
  - integer?
* - relics
  - integer?
* - diamonds
  - integer?
* - starpower
  - integer?
* - medals
  - integer?
* - ethereal_currency
  - integer?
* - egg_wildcards
  - integer?
* - user_monster_id
  - long
```

### Additional Responses

On success, [Update Monster Event](/events/gs_update_monster) is also sent.