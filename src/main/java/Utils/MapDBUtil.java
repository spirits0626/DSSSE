package Utils;

import Base.Label;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.Serializer;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

/**
 * mapDB缓存工具类
 **/
public class MapDBUtil {

    /**
     * 缓存回收策略/基于容量
     * expireMaxSize：设置缓存的容量，当超出expireMaxSize时，按照LRU进行缓存回收。
     * 缓存回收策略/基于时间
     * expireAfterCreate/expireAfterUpdate：设置TTL，缓存数据在给定的时间内没有写（创建/覆盖）时，则被回收。即定期的会回收缓存数据。
     * expireAfterGet：设置TTI， 缓存数据在给定的时间内没有读/写时，则被回收。每次访问时都会更新它的TTI，从而如果该缓存是非常热的数据，则将一直不过期，
     * 可能会导致脏数据存在很长的时间（因此，建议要设置expireAfterCreate/expireAfterUpdate）
     * SECONDS          秒
     * MINUTES     分钟
     * HOURS      小时
     */

    private static final ConcurrentMap defaultMap = DBMaker.fileDB("file.db").make()
            .hashMap("CaseCache", Serializer.STRING, Serializer.STRING)
            .expireMaxSize(10000)
            .expireAfterCreate(10, TimeUnit.MINUTES)
            .expireAfterUpdate(10, TimeUnit.MINUTES)
            .createOrOpen();

    private static final DB db = DBMaker.memoryDB().make();


    public static void put(String key, Object value, String... strings) {
        save(key, value, null, strings);
    }

    public static void put(String key, Object value, Integer expireTime, String... strings) {
        save(key, value, expireTime, strings);
    }


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

    public static void main(String[] args) {
        List<Label> list = new ArrayList<Label>();
        Label label = new Label();
        list.add(label);
        MapDBUtil.put("style", 1111);
        MapDBUtil.put("style", list);
        MapDBUtil.put("gardenSettingBO", label);
        List<Label> list2 = MapDBUtil.get("style", new TypeReference<List<Label>>() {
        }.getType());
        Label labelnew = list2.get(0);

        MapDBUtil.put("style", 1111, "testmap");
        System.out.println("-----" + MapDBUtil.get("style", Integer.TYPE, "testmap"));
        System.out.println("-----" + MapDBUtil.get("style", "testmap"));
    }
}
