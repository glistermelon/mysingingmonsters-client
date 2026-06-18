# Query Genes Database

Queries the database of elements.

## Command

`db_gene`

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
* - genes_data
  - array\<[Gene](/data/Gene)\>?
* - last_updated
  - long
* - server_time
  - long
```