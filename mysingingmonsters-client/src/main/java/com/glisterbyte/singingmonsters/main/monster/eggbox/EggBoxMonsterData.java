package com.glisterbyte.singingmonsters.main.monster.eggbox;

import com.glisterbyte.singingmonsters.main.catalog.MonsterSpecies;
import com.glisterbyte.singingmonsters.main.catalog.MultiMonsterSpecies;
import com.glisterbyte.singingmonsters.main.common.Timer;
import com.glisterbyte.singingmonsters.main.catalog.Catalog;
import com.glisterbyte.singingmonsters.sfsmodels.BlankJsonList;
import com.glisterbyte.singingmonsters.sfsmodels.data.SfsMonster;

import javax.annotation.Nullable;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

public final class EggBoxMonsterData {

    private final Catalog catalog;
    private final MonsterSpecies species;

    private Timer fillTimer;
    private final Map<MultiMonsterSpecies, Integer> zappedEggs;
    private final Map<MultiMonsterSpecies, Integer> requiredEggs;
    private Instant nextCollectionTime;

    public EggBoxMonsterData(ReadableEggBoxMonster monster, SfsMonster sfsMonster) {

        catalog = monster.getCatalog();
        species = monster.getSpecies();

        requiredEggs = computeEggCountMap(sfsMonster.box_requirements);

        if (monster.isActivated()) {
            fillTimer = null;
            zappedEggs = new HashMap<>(requiredEggs);
        }
        else {
            fillTimer = computeFillTimer(sfsMonster.egg_timer_start);
            zappedEggs = computeEggCountMap(sfsMonster.boxed_eggs);
        }

        nextCollectionTime = Instant.ofEpochMilli(sfsMonster.last_collection)
                .plus(Duration.ofMinutes(sfsMonster.random_underling_collection_min));

    }

    private Timer computeFillTimer(Long eggTimerStart) {

        /*
            TODO
            For Amber Island vessels, there is no 'fill time limit' attached to the species
            because the time limit is attached to some limited time event instead.
            If there's a way to get THAT time limit, this shouldn't return null.
         */
        if (species.getFillTimeLimit() == null) return null;

        if (eggTimerStart == null) return null;
        else {
            return new Timer(
                    Instant.ofEpochMilli(eggTimerStart),
                    species.getFillTimeLimit()
            );
        }

    }

    private Map<MultiMonsterSpecies, Integer> computeEggCountMap(@Nullable List<Integer> zappedEggList) {
        Map<MultiMonsterSpecies, Integer> map = new HashMap<>();
        for (int speciesId : Optional.ofNullable(zappedEggList).orElse(List.of())) {
            MonsterSpecies species = catalog.getMonsterSpecies(speciesId);
            MultiMonsterSpecies multiSpecies = MultiMonsterSpecies.fromSpecies(species);
            map.compute(multiSpecies, (x, n) -> n == null ? 1 : (n + 1));
        }
        return map;
    }

    public synchronized Timer getFillTimer() {
        return fillTimer;
    }

    public synchronized Map<MultiMonsterSpecies, Integer> getZappedEggCounterMap() {
        return zappedEggs;
    }

    public synchronized Map<MultiMonsterSpecies, Integer> getRequiredEggCounterMap() {
        return requiredEggs;
    }

    public synchronized Instant getNextCollectionTime() {
        return nextCollectionTime;
    }

    public synchronized void updateFillTimer(Long eggTimerStart) {
        fillTimer = computeFillTimer(eggTimerStart);
    }

    public synchronized void removeFillTimer() {
        fillTimer = null;
    }

    public synchronized void updateZappedEggs(Map<MultiMonsterSpecies, Integer> counterMap) {
        zappedEggs.clear();
        zappedEggs.putAll(counterMap);
    }

    public synchronized void updateZappedEggs(List<Integer> zappedEggList) {
        zappedEggs.clear();
        if (zappedEggList instanceof BlankJsonList) {
            updateZappedEggs(getRequiredEggCounterMap());
        }
        else {
            updateZappedEggs(computeEggCountMap(zappedEggList));
        }
    }

    public synchronized void setNextCollectionTime(Instant newNextCollectionTime) {
        nextCollectionTime = newNextCollectionTime;
    }

}