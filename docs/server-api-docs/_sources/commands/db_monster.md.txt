# Query Monsters Database

Queries the database of monster species.

## Command

`db_monster`

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
* - monsters_data
  - array\<[Monster Species](/data/MonsterSpecies)\>?
* - last_updated
  - long
* - server_time
  - long
```