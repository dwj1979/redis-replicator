package com.moilioncircle.redis.replicator.rdb;

import com.moilioncircle.redis.replicator.Configuration;
import com.moilioncircle.redis.replicator.FileType;
import com.moilioncircle.redis.replicator.RedisReplicator;
import com.moilioncircle.redis.replicator.Replicator;
import com.moilioncircle.redis.replicator.rdb.datatype.KeyValuePair;
import com.moilioncircle.redis.replicator.rdb.datatype.ZSetEntry;
import junit.framework.TestCase;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static junit.framework.TestCase.assertEquals;

/**
 * Created by leon on 3/17/17.
 */
public class RdbV8ParserTest {
    @Test
    public void testParse() throws Exception {
        ConcurrentHashMap<String, KeyValuePair> map = new ConcurrentHashMap<>();
        String[] resources = new String[]{"rdb_version_8_with_64b_length_and_scores.rdb", "non_ascii_values.rdb"};
        for (String resource : resources) {
            template(resource, map);
        }
        assertEquals("bar", map.get("foo").getValue());
        List<ZSetEntry> zset = new ArrayList(((Set<ZSetEntry>) map.get("bigset").getValue()));
        assertEquals(1000, zset.size());
        for (ZSetEntry entry : zset) {
            if (entry.getElement().equals("finalfield")) {
                assertEquals(2.718d, entry.getScore());
            }
        }
    }

    public void template(String filename, final ConcurrentHashMap<String, KeyValuePair> map) {
        try {
            Replicator replicator = new RedisReplicator(RdbParserTest.class.
                    getClassLoader().getResourceAsStream(filename)
                    , FileType.RDB, Configuration.defaultSetting());
            replicator.addRdbListener(new RdbListener.Adaptor() {
                @Override
                public void handle(Replicator replicator, KeyValuePair<?> kv) {
                    System.out.println(kv);
                    map.put(kv.getKey(), kv);
                }
            });
            replicator.open();
        } catch (Exception e) {
            TestCase.fail();
        }
    }
}
