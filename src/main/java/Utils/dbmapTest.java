package Utils;

import com.alibaba.fastjson.JSON;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.Serializer;

import java.lang.reflect.Type;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

public class dbmapTest {

    private static final ConcurrentMap defaultMap = DBMaker.fileDB("file.db").make()
            .hashMap("CaseCache", Serializer.STRING, Serializer.STRING)
            .expireMaxSize(10000)
            .expireAfterCreate(10, TimeUnit.MINUTES)
            .expireAfterUpdate(10, TimeUnit.MINUTES)
            .createOrOpen();


    private static final DB db = DBMaker.memoryDB().make();

    /**
     * 返回集合
     *
     * @param key
     * @param type
     * @param <D>
     * @return
     */
    public static <D> D get(String key, Type type, String... strings) {
        return loadType(key, type, strings);
    }


    /**
     * 返回对象
     *
     * @param key
     * @param D
     * @param <D>
     * @return
     */
    public static <D> D get(String key, Class<D> D, String... strings) {
        return load(key, D, strings);
    }

    public static Object get(String key, String... strings) {
        String mapName = null;
        if (strings != null && strings.length > 0) {
            mapName = strings[0];
        }
        return load(key, mapName);
    }

    private static void save(String key, Object value, Integer expireTime, String... strings) {
        if (key != null && value != null) {
            if (strings != null && strings.length > 0) {
                String mapName = strings[0];
                ConcurrentMap concurrentMap = null;
                if (expireTime != null) {
                    concurrentMap = db.hashMap(mapName, Serializer.STRING, Serializer.STRING)
                            .expireMaxSize(10000)
                            .expireAfterCreate(expireTime, TimeUnit.MINUTES)
                            .expireAfterUpdate(expireTime, TimeUnit.MINUTES)
                            .createOrOpen();
                } else {
                    concurrentMap = db.hashMap(mapName, Serializer.STRING, Serializer.STRING).createOrOpen();
                }
                concurrentMap.put(key, JSON.toJSONString(value));
                return;
            }
            defaultMap.put(key, JSON.toJSONString(value));
        }
    }

    private static Object load(String key, String mapName) {
        if (key != null) {
            return null;
        }
        if (mapName != null) {
            ConcurrentMap concurrentMap = db.hashMap(mapName, Serializer.STRING, Serializer.STRING).createOrOpen();
            if (concurrentMap != null) {
                return concurrentMap.get(key);
            }
        }
        return defaultMap.get(key);
    }

    private static <D> D load(String key, Class<D> D, String... strings) {
        if (key != null || D == null) {
            return null;
        }
        String keyStr = null;
        if (strings != null && strings.length > 0) {
            String mapName = strings[0];
            ConcurrentMap concurrentMap = db.hashMap(mapName, Serializer.STRING, Serializer.STRING).createOrOpen();
            if (concurrentMap != null) {
                keyStr = (String) concurrentMap.get(key);
                return JSON.parseObject(keyStr, D);
            }
        }
        keyStr = (String) defaultMap.get(key);
        if (keyStr == null) {
            return null;
        }
        return JSON.parseObject(keyStr, D);
    }

    private static <D> D loadType(String key, Type type, String... strings) {
        if (key == null || type == null) {
            return null;
        }
        String keyStr = null;
        if (strings != null && strings.length > 0) {
            String mapName = strings[0];
            ConcurrentMap concurrentMap = db.hashMap(mapName, Serializer.STRING, Serializer.STRING).createOrOpen();
            if (concurrentMap != null) {
                keyStr = (String) concurrentMap.get(key);
                return JSON.parseObject(keyStr, type);
            }
        }
        keyStr = (String) defaultMap.get(key);
        if (keyStr == null) {
            return null;
        }
        return JSON.parseObject(keyStr, type);
    }

    public static void main(String args[]) {
        DB db = DBMaker.fileDB("file.db").fileMmapEnable().make();
        ConcurrentMap<String, Long> map =
                db.hashMap("map", Serializer.STRING, Serializer.LONG)
                        .createOrOpen();
        map.put("something", 111L);
        System.out.println(map.get("something"));
        db.close();
    }
}
