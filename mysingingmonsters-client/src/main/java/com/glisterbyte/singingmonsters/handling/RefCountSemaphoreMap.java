package com.glisterbyte.singingmonsters.handling;

import com.glisterbyte.singingmonsters.common.StringUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

public class RefCountSemaphoreMap<K> {

    private static class Entry {
        final Semaphore sem = new Semaphore(1);
        AtomicInteger refCount = new AtomicInteger(0);
    }

    public class Lock {

        private final RefCountSemaphoreMap<K> map;
        private final K key;

        public Lock(RefCountSemaphoreMap<K> map, K key) {
            this.map = map;
            this.key = key;
        }

        public void unlock() {
            map.unlock(key);
        }

    }

    private final Map<K, Entry> map = new HashMap<>();

    public Lock lock(K key) {
        Entry entry;
        synchronized (map) {
            entry = map.computeIfAbsent(key, x -> new Entry());
            entry.refCount.incrementAndGet();
        }
        try {
            entry.sem.acquire();
        }
        catch (InterruptedException ex) {
            entry.refCount.decrementAndGet();
            throw new RuntimeException(ex);
        }
        return new Lock(this, key);
    }

    private void unlock(K key) {
        synchronized (map) {
            Entry entry = map.get(key);
            if (entry == null) throw new IllegalStateException("Attempt to unlock already unlocked key '" + key + "'");
            entry.sem.release();
            int refCount = entry.refCount.decrementAndGet();
            if (refCount == 0) map.remove(key);
        }
    }
}