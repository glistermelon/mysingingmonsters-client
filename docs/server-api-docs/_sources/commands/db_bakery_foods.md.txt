# Query Bakery Foods Database

Queries the bakery recipe database.

## Command

`db_bakery_foods`

## Request

```{list-table}
:widths: 20 20
:header-rows: 1
* - Key
  - Type
* - last_updated
  - long
```

## Response

See [Database Queries](/topics/DatabaseQueries) for important information about the response.
```{list-table}
:widths: 20 20
:header-rows: 1
* - Key
  - Type
* - bakery_data
  - array\<[Bakery Recipe](/data/BakeryRecipe)\>?
* - last_updated
  - long
* - server_time
  - long
```