package event_based_server;

import org.apache.commons.collections4.MapIterator;
import org.apache.commons.collections4.map.LRUMap;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by soo on 2017. 6. 10..
 */
class InMemoryCache<K> {
    private long timeToLive;
    private final LRUMap<K, CacheObject> cacheMap;
    private final long maxByteSize;
    private long currentByteSize;

    class CacheObject {
        long lastAccessed = System.currentTimeMillis();
        ByteArrayWrapper value;

        CacheObject(ByteArrayWrapper value) {
            this.value = value;
        }
    }

    InMemoryCache(long timeToLive, final long timerInterval, int maxItems, long maxByteSize) {
        this.timeToLive = timeToLive * 1000;
        this.cacheMap = new LRUMap<>(maxItems);
        Collections.synchronizedMap(this.cacheMap);
        this.maxByteSize = maxByteSize;
        this.currentByteSize = 0;

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

    void put(K key, ByteArrayWrapper value) {
        synchronized (cacheMap) {
            if (maxByteSize >= currentByteSize + value.getByteArray().length) {
                currentByteSize += value.getByteArray().length;
                cacheMap.put(key, new CacheObject(value));
            }
        }
    }

    ByteArrayWrapper get(K key) {
        synchronized (cacheMap) {
            CacheObject c = cacheMap.get(key);

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
            currentByteSize -= cacheMap.get(key).value.getByteArray().length;
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
            MapIterator<K, CacheObject> itr = cacheMap.mapIterator();

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
