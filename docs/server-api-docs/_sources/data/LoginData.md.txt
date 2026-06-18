# Login Data



## Fields

```{list-table}
:widths: 20 20 60
:header-rows: 1
* - Key
  - Type
  - Description
* - access_key
  - string
  - A static access key
* - attempt_recovery
  - boolean
  - 
* - client_device
  - string
  - What device the user is on (for instance, a specific model of iPhone)
* - client_lang
  - string
  - Two-letter code for the user's language
* - client_os
  - string
  - The version of the user's operating system (Android, iOS, etc.)
* - client_platform
  - string
  - Basically what platform the user is on (e.g., "android")
* - client_version
  - string
  - What version of the game the user has
* - last_update_version
  - string
  - 
* - last_updated
  - long
  - Likely the timestamp at which the user last updated the game
* - raw_device_id
  - string
  - An ID of the user's device
* - token
  - string
  - A login token granted by the login API.
```

## Referenced By

* [Login](/commands/USER_LOGIN)