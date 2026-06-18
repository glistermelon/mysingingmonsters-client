# Breed Monsters

Breeds two monsters with a breeding structure.

## Command

`gs_breed_monsters`

## Request

```{list-table}
:widths: 20 20 60
:header-rows: 1
* - Key
  - Type
  - Description
* - structure_id
  - long
  - Breeding structure user structure ID
* - user_monster_id_1
  - long
  - User monster ID
* - user_monster_id_2
  - long
  - User monster ID
* - time_mask
  - short
  - I don't know what a time mask is.
```

## Response

Though not listed here, the [standard response parameters](/topics/StandardResponseParameters) are included.
```{list-table}
:widths: 20 20
:header-rows: 1
* - Key
  - Type
* - user_structure_id
  - long
* - user_monster_1
  - long
* - user_monster_2
  - long
* - user_breeding
  - [Breeding](/data/Breeding)
```