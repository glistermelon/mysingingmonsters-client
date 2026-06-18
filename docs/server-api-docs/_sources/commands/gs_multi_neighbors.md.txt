# Multi Update Monster

Updates monster happiness.

## Request



### Command

`gs_multi_neighbors`

### Fields

```{list-table}
:widths: 20 20 60
:header-rows: 1
* - Key
  - Type
  - Description
* - entity_array
  - array\<sfsobject\>
  - 
* - island_needs_happiness_update
  - boolean
  - I haven't seen the authentic client pass true even when happiness updates. Maybe if you buy a unity tree? I haven't tried it.
```
The `entity_array` is an array of the following object:
 ```
 {
   user_monster_id: long?
   neighbors: array<NeighborData>
 }
 

 NeighborData {
   entity_id: integer
   distance: integer
 }
 ```
 Each item in `entity_array` represents either a monster or a structure
 and how it is positioned relative to other monsters and structures.
 If an item corresponds to a monster, `user_monster_id` should be present, and will be the monster's user monster ID.
 Otherwise, if an item corresponds to a structure, `user_monster_id` should not be present.
 Each item's `neighbors` array should only include distances less than or equal to 2,
 so farther monsters or structures are disregarded.
  `entity_id` is the entity ID of either a [structure type](/data/StructureType)
  or [monster species](/data/MonsterSpecies).

## Response



### Command

`gs_multi_update_monster`

### Fields

Though not listed here, the [standard response parameters](/topics/StandardResponseParameters) are included.
```{list-table}
:widths: 20 20
:header-rows: 1
* - Key
  - Type
* - user_monster_id
  - long
* - update_monster_list
  - array\<[Monster Multi Update](/data/MonsterMultiUpdate)\>
```