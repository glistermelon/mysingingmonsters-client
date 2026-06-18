# Mute Monster

Mutes or unmutes a monster.

## Command

`gs_mute_monster`

## Request

```{list-table}
:widths: 20 20
:header-rows: 1
* - Key
  - Type
* - user_monster_id
  - long
* - muted
  - integer
```

## Response

Though not listed here, the [standard response parameters](/topics/StandardResponseParameters) are included.
There are no other parameters.

### Additional Responses

On success, [Update Monster Event](/events/gs_update_monster) is also sent.