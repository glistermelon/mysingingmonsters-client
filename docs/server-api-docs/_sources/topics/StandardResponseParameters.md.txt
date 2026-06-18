# Standard Response Parameters

For most requests, the following field is included to indicate success or failure:

```{list-table}
:widths: 20 20 60
:header-rows: 1
* - Key
  - Type
  - Description
* - success
  - boolean
  - Indicates whether or not the requested action was successfully performed.
```

If the response indicates failure, one or multiple of the
following error message fields is almost always present.
"message" is by far the most common, usually appearing alone.
Fields that you would expect if the action had succeeded are typically not present,
and information that correlates the failed response to a request,
like a user monster ID or a user structure ID, are only sometimes included.

```{list-table}
:widths: 20 20 60
:header-rows: 1
* - Key
  - Type
  - Description
* - message
  - string
  - An error message.
* - error_msg
  - string
  - Key for a local text resource for the error message.
* - errorMessage
  - string
  - An error message. Last time I checked, this one is used for the [Login](/commands/USER_LOGIN) response.
```