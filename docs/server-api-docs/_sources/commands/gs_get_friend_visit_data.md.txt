# Visit Friend

Gets visit data for a user.

## Command

`gs_get_friend_visit_data`

## Request

```{list-table}
:widths: 20 20
:header-rows: 1
* - Key
  - Type
* - user_id
  - long
```

## Response

Though not listed here, the [standard response parameters](/topics/StandardResponseParameters) are included.
```{list-table}
:widths: 20 20
:header-rows: 1
* - Key
  - Type
* - friend_object
  - [Visit Data](/data/VisitData)
```