# Login

The first request to be sent by the client to the server.
 Authenticates the user.

## Command

`USER_LOGIN`

## Request

```{list-table}
:widths: 20 20 60
:header-rows: 1
* - Key
  - Type
  - Description
* - data
  - [Login Data](/data/[Login Data](/data/LoginData))
  - 
* - password
  - string
  - Always an empty string
* - user
  - string
  - A username provided by the login API
* - zone
  - string
  - Always "MySingingMonsters"
```

## Response

Though not listed here, the [standard response parameters](/topics/StandardResponseParameters) are included.
There are no other parameters.