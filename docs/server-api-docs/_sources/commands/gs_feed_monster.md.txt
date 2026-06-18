# Feed Monster

Feeds a monster once.

## Command

`gs_feed_monster`

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
There are no other parameters.

### Additional Responses

On success, [Update Monster Event](/events/gs_update_monster) is also sent.