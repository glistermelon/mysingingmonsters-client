# Player

Requests the user's player data.

## Command

`gs_player`

## Request

```{list-table}
:widths: 20 20
:header-rows: 1
* - Key
  - Type
* - last_updated
  - string
```

## Response

```{list-table}
:widths: 20 20
:header-rows: 1
* - Key
  - Type
* - player_object
  - [Player](/data/Player)
```
An extra response with command gs_get_friends is also sent with the following fields.
Though not listed here, the [standard response parameters](/topics/StandardResponseParameters) are included.
```{list-table}
:widths: 20 20
:header-rows: 1
* - Key
  - Type
* - pending
  - array\<[Friend Request](/data/FriendRequest)\>
* - friends
  - array\<[Friend](/data/Friend)\>
* - tribes
  - array\<[Tribe](/data/Tribe)\>
* - top_tribes
  - array\<[Tribe](/data/Tribe)\>
```