# Request Friend

Sends a friend request.

## Command

`gs_friend_request`

## Request

```{list-table}
:widths: 20 20
:header-rows: 1
* - Key
  - Type
* - friend_code
  - string
```

## Response

Though not listed here, the [standard response parameters](/topics/StandardResponseParameters) are included.
```{list-table}
:widths: 20 20
:header-rows: 1
* - Key
  - Type
* - request
  - array\<[Friend Request](/data/FriendRequest)\>?
* - friends
  - array\<[Friend](/data/Friend)\>?
```