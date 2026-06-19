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

* [Java Client Documentation](https://glistermelon.github.io/mysingingmonsters-client/java/client/v0.1.2/)
* [Server API Documentation](https://glistermelon.github.io/mysingingmonsters-client/server-api-docs/)

## Installation

Releases are [published to the Maven central repository](https://central.sonatype.com/artifact/com.glisterbyte/mysingingmonsters-client).

### Maven

Add the Maven dependency:
```
<dependency>
    <groupId>com.glisterbyte</groupId>
    <artifactId>mysingingmonsters-client</artifactId>
    <version>0.1.3</version>
</dependency>
```

Note the version number here may not be up to date. You can find the latest release [here](https://central.sonatype.com/artifact/com.glisterbyte/mysingingmonsters-client).

### Logging

If you haven't set up a logging backend, you'll see something like:
```
SLF4J(W): No SLF4J providers were found.
SLF4J(W): Defaulting to no-operation (NOP) logger implementation
SLF4J(W): See https://www.slf4j.org/codes.html#noProviders for further details.
```

I highly recommend setting one up because it provides a lot of information about what the client is actually doing, especially if something goes wrong.

With Maven, for example, you could use Logback by adding a dependency like this:
```
<dependency>
    <groupId>ch.qos.logback</groupId>
    <artifactId>logback-classic</artifactId>
    <version>1.5.18</version>
</dependency>
```

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

#### Small Complete Example

```java
public class Main {

    static void main() throws ClientException, InterruptedException {

        Client client = new Client();
        client.connectWithEmail("myemail@mail.com", "mypassword");

        for (Island island : client.getIslands()) {
            for (Monster monster : island.getMonsters()) {
                System.out.println(monster);
            }
        }

        Monster monster = client.getIslands().getFirst().getMonsters().getFirst();
        System.out.println(monster.getName() + " est un " + monster.getSpecies().getName(Language.FRENCH));

        /*
            If you don't disconnect the client like this,
            it will keep running forever (until you terminate the program)!
        */
        client.disconnect();

    }

}
```

#### Read basic monster properties

These are not all the available functions. See [the documentation](#documentation) for all of them.

```java
Monster monster = ...;
System.out.println(monster.getName()); // "Jimbo"
System.out.println(monster.getSpecies().getName()); // "Quibble"
System.out.println(monster.getHappiness()); // PERCENT_50 (MonsterHappiness)
System.out.println(monster.getSpecies().getLikes().getFirst()); // StructureType(name='Castanevine')
System.out.println(monster.getVolume()); // 1.0
System.out.println(monster.isInHotel()); // false
System.out.println(monster.getCollectionCurrencyType()); // COINS
System.out.println(monster.isMuted()); // false
System.out.println(monster.getPosition()); // Position[x=23, y=19]
System.out.println(monster.isFlipped()); // false
System.out.println(monster.getSpecies().getBedsRequired()); // 2
System.out.println(monster.getSpecies().getElements().getFirst()); // Element(Air)
```

#### Read basic structure properties

These are not all the available functions. See [the documentation](#documentation) for all of them.

```java
Structure structure = ...;
System.out.println(structure.getName()); // Flappy Flag
System.out.println(structure.getPosition()); // Position[x=10, y=2]
System.out.println(structure.getScale()); // 0.92341235
System.out.println(structure.getSize()); // Size[x=1, y=1]
System.out.println(structure.isMuted()); // false
System.out.println(structure.isUpgrading()); // false
System.out.println(structure.isInWarehouse()); // false
```

#### Read basic island properties

These are not all the available functions. See [the documentation](#documentation) for all of them.

```java
Island island = client.getIslands().getFirst();
System.out.println(island.getIslandType()); // COLD_ISLAND
System.out.println(island.getMonsters().size()); // 6
System.out.println(island.getStructures().size()); // 4
System.out.println(island.getScore()); // 0 :(
System.out.println(island.getTimeWarp()); // 1.0
```

#### Modify a monster

These are not all the available functions. See [the documentation](#documentation) for all of them.

```java
Monster monster = ...;

// Feed to level 4
monster.feedToLevel(4);

// Feed to level 5
for (int i = 0; i < 4; i++) {
    monster.feed();
}

// Feed to level 6
monster.feedToNextLevel();

monster.move(new Position(10, 10));

monster.flip();
monster.setFlipped(false);

monster.mute();
monster.unmute();
monster.setMuted(false);

monster.setVolume(1.0);

System.out.println("Collected " + monster.collect());

// Goodbye!
monster.sell();
```

#### Wublin functionality

These are not all the available functions. See [the documentation](#documentation) for all of them.

```java
EggBoxMonster wublin = ...;

if (wublin.isActivated()) {
    if (wublin.isReadyToCollect()) {
        wublin.collect();
    }
    else {
        System.out.println(wublin.getNextCollectionTime());
    }
}
else if (wublin.isReadyToActivate()) {
    wublin.activate();
}
else {
    System.out.println(wublin.getFillTimer());
    wublin.sellEggs();
    System.out.println(wublin.needsEgg(client.getMonsterCatalog().noggin()));
}
```

#### Bake cupcakes

```java
Bakery bakery = ...;
bakery.bake(client.getBakingCatalog().cupcakes());
bakery.waitUntilDone();
int food = bakery.collect();
System.out.println("Collected " + food + " treats!");
```

#### Breed monsters

```java
Island plantIsland = client.getIsland(IslandType.PLANT_ISLAND);
MonsterCatalog monsters = client.getMonsterCatalog();
Monster entbrat = plantIsland.getMonsterOfSpecies(monsters.entbrat());
Monster rareEntbrat = plantIsland.getMonsterOfSpecies(monsters.rareEntbrat());

BreedingStructure breeder = plantIsland.getBreedingStructures().getFirst();
breeder.breed(entbrat, rareEntbrat);
System.out.println(breeder.getRemainingTime());

// It's going to take a while!
client.disconnect();
breeder.waitUntilDone();
client.reconnect();

Nursery nursery = breeder.collectEgg();
System.out.println(nursery.getRemainingTime());
client.disconnect();
nursery.waitUntilDone();
client.reconnect();

// Don't mix up 'sellEgg', which sells the egg, with 'sell', which attempts to sell the nursery.
nursery.sellEgg();
```

#### Buy and place a noggin

```java
Island plantIsland = client.getIsland(IslandType.PLANT_ISLAND);
// You can also get the nursery first and then call nursery.buyEgg
Nursery nursery = plantIsland.buyMonsterEgg(client.getMonsterCatalog().noggin());
nursery.waitUntilDone();
Monster monster = nursery.hatchEgg(new MonsterPlacement(new Position(2, 3), false));
```

#### Collect from a mine

```java
Mine mine = client.getIsland(IslandType.PLANT_ISLAND).getMine();
mine.collect();
```

#### Visit another user

```java
VisitData visitData = client.visitUser(client.getFriends().getFirst());
for (UnownedIsland island : visitData.getIslands()) {
    for (UnownedMonster monster : island.getMonsters()) {
        System.out.println(monster);
    }
}
```