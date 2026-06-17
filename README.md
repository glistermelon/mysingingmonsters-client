# This isn't done yet but it's almost done

# My Singing Monsters Client for Java

A *My Singing Monsters* client written in Java. See the documentation for how much 'readable' information (i.e., info available via getter methods) is available for different types. Currently supported non-read-only features are:
* Buy an island
* Buy a structure (or decoration) (but not from the starshop)
* Buy a monster
* Collect from monsters (collect all isn't supported yet)
* Sell monsters
* Feed monsters
* Move/flip monsters
* Change monster volume
* Change monster names
* Mute monsters
* Breed monsters
* Hatch eggs (or sell them)
* Move/flip/scale structures
* Sell structures
* Remove obstacles
* Bake treats
* Collect from mines
* Zap eggs (only tested with Wublins)
* Activate a Wublin
* Sell the eggs in a Wublin
* Visit a friend's island

**This client is in a very early release and probably has a lot of problems I haven't detected yet.**

## ⚠️️⚠️️⚠️️⚠️️ Disclaimers and Warnings ⚠️️⚠️️⚠️️⚠️️

#### **Using this client probably breaks My Singing Monster's TOS and puts your account at <u>risk of being banned</u>!**

I personally have not used the client on my own account.

#### **Do not share any logs with Apache HttpComponents Client debug-level logging enabled anywhere! They contain your username and password in plain text!**

## Documentation

* [Java Client Documentation](https://glistermelon.github.io/mysingingmonsters-client/)
* [Server API Documentation](https://msm-api-docs.glisterbyte.com/)

## Installation

## Usage

Refer to the examples below (you may need to scroll down) on how to do most of the things the library supports. You can also read the documentation.

### What is a Structure

In the game, a "structure" is specifically the stuff you find in the market's Structures menu, stuff like the mine, hotel, bonus nursery, and whatnot. Under the hood, and in this library, essentially every interactive object on an island that isn't a monster is a structure. The castle, all breeding structures, all nurseries, obstacles, decorations, etc., are all structures, which are represented by `Structure` in this library. You can distinguish between whether a structure is a nursery, obstacle, decoration, or whatever else using `Structure::getStructureCategory`.

### Catalogs

The `Catalog` class is effectively a local storage of bulk data from the server. In particular, it provides:
* Elements
* Monster Species
* Structure Types
* Bakery Recipes

The `Catalog` primarily provides methods to get all of the items for a respective one of those categories or a specific item by a kind of ID. However, it also provides four specialized catalog instances of types:
* `ElementCatalog`
* `MonsterCatalog`
* `StructureCatalog`
* `BakeryCatalog`

These have named methods for recognized items that you would probably be looking for, like the air element or the cupcakes recipe.

You can get `Catalog` or any of the specialized catalog instances using getter methods on `Client`, e.g., `client.getCatalog()`, `client.getMonsterCatalog()`, etc..

#### Cache

If you've already successfully connected the client once, the catalog will be cached. You can load a `Catalog` from cache without connecting at all like this:
```java
Catalog catalog = new Catalog();
catalog.loadFromCache();
```

You can clear the cache like this:
```java
CacheManager.clear();
```

### Confusing Monster Species

Different species of monster (noggin, tweedle, etc.) are represented by `MonsterSpecies`. However, the API often considers two species that are visually identical to be different species under the hood- for instance, on the natural islands, you have a species named "Tweedle," but the tweedle on Fire Island is actually a distinct species called something like "Tweedle Fire Island." This library attempts to bridge the gap with `MultiMonsterSpecies`, which represents a single species across all islands. You can get an instance from a `MonsterCatalog` like this:
```java
MultiMonsterSpecies noggin = monsterCatalog.noggin();
```
And then you can get the Noggin `MonsterSpecies` for a specific island like this:
```java
MonsterSpecies nogginPlantIsland = noggin.getSpecies(IslandType.PLANT_ISLAND);
```
Where it is sensible, most methods that take a `MonsterSpecies` argument can also take a `MultiMonsterSpecies` argument instead.

### The Active Island

When actually playing the game, you're always "on" a specific island. The same thing is true for this client. You can use `Client::getActiveIsland` to get that island and `Client::setActiveIsland` to change it. For concurrent code, there are also two variants of `Client::withActiveIsland` to perform an action with the assurance that a particular island is the active island while the action is performed.

**For nearly every action involving a monster or structure, the client automatically switches the active island**, as it is almost always necessary. It should be a very lightweight action server-side, so I don't worry about it, but nonetheless, you should know that if you write code that performs a lot of actions on a lot of monsters and/or structures that aren't on the same island, **the client may rapidly hop between islands**, and most of the time, actions on monsters/structures on different islands cannot be performed concurrently.

### Disconnecting and Reconnecting

As I'm sure you know, the game is typically played in short sessions separated by long durations of time. It wouldn't be fitting for the client to be connected 24/7, or for it to be connected for 12 hours straight if the goal is to breed a Clamble. Instead, the client is written to support code like the following:

```java
client.connectWithEmail(...);
BreedingStructure breeder = (...); // breed something
client.disconnect();
breeder.waitUntilDone();
client.reconnect();
breeder.collectEgg();
```

`Client::reconnect` refreshes all islands, monsters, and structures that still exist (i.e., those which haven't been sold, boxed, or otherwise discarded).

At the moment, is there no mechanism to try to automatically reconnect if you get unexpectedly disconnected.

### Concurrency

The client is designed with concurrency in mind; everything should be thread-safe. However, I haven't tested any concurrent code.

Note that concurrent code may not always be more efficient anyway due to server design choices beyond my control. For instance, it is impossible to concurrently collect from mines because the relevant endpoint determines what mine to collect from by checking what island you're on. So, if you were to write a concurrent mine-collecting program, the client would ultimately force each collect call to wait for a previous one to finish, so they all effectively run synchronously.

Additionally, some endpoints that can be called concurrently do not provide enough information in error responses to figure out what call actually caused the error; in such cases, the error is logged but the calling thread blocks for some time before throwing a timeout exception.

### Examples

I will add these soon