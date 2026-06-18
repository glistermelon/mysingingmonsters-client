# Query Structures Database

Queries the database of structure types.

## Command

`db_structure`

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
* - structures_data
  - array\<[Structure Type](/data/StructureType)\>?
* - last_updated
  - long
* - server_time
  - long
```