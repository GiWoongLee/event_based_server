package event_based_server;

import java.util.ArrayList;

import org.apache.commons.collections4.MapIterator;
import org.apache.commons.collections4.map.LRUMap;

/**
 * Created by soo on 2017. 6. 10..
 */
class InMemoryCache<K, T> {
    private long timeToLive;
    private final LRUMap<K, CacheObject<T>> cacheMap;

    class CacheObject<CT> {
        long lastAccessed = System.currentTimeMillis();
        CT value;

        CacheObject(CT value) {
            this.value = value;
        }
    }

    InMemoryCache(long timeToLive, final long timerInterval, int maxItems) {
        this.timeToLive = timeToLive * 1000;

        cacheMap = new LRUMap<>(maxItems);

        if (timeToLive > 0 && timerInterval > 0) {

            Thread t = new Thread(new Runnable() {
                public void run() {
                    while (true) {
                        try {
                            Thread.sleep(timerInterval * 1000);
                        } catch (InterruptedException ex) {

                        }
                        cleanup();
                    }
                }
            });

            t.setDaemon(true);
            t.start();
        }
    }

    void put(K key, T value) {
        synchronized (cacheMap) {
            cacheMap.put(key, new CacheObject<>(value));
        }
    }

    T get(K key) {
        synchronized (cacheMap) {
            CacheObject<T> c = cacheMap.get(key);

            if (c == null)
                return null;
            else {
                c.lastAccessed = System.currentTimeMillis();
                return c.value;
            }
        }
    }

    void remove(K key) {
        synchronized (cacheMap) {
            cacheMap.remove(key);
        }
    }

    int size() {
        synchronized (cacheMap) {
            return cacheMap.size();
        }
    }

    void cleanup() {
        long now = System.currentTimeMillis();
        ArrayList<K> deleteKey = null;

        synchronized (cacheMap) {
            MapIterator<K, CacheObject<T>> itr = cacheMap.mapIterator();

            deleteKey = new ArrayList<K>((cacheMap.size() / 2) + 1);
            K key = null;
            CacheObject c = null;

            while (itr.hasNext()) {
                key = itr.next();
                c = itr.getValue();

                if (c != null && (now > (timeToLive + c.lastAccessed))) {
                    deleteKey.add(key);
                }
            }
        }

        for (K key : deleteKey) {
            synchronized (cacheMap) {
                cacheMap.remove(key);
            }

            Thread.yield();
        }
    }
}
