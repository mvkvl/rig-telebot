package ws.slink.rig.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import redis.clients.jedis.Jedis;

public class RedisDAO {

	private String host = null;
	private int    port = 0;
	
	public void init(String redisHost, int redisPort) {
		this.host = redisHost;
		this.port = redisPort;
	}

	public List<String> keys(String template) {
		return keys(template, -1, false);
	}

	public List<String> keys(String template, int idx) {
		return keys(template, idx, true);
	}

	public List<String> keys(String template, int idx, boolean splitOnMetric) {
		
		// get all keys by template
//		System.out.println("host: " + host + "\nport: " + port);
		Jedis jedis = new Jedis(host, port);
		Set<String> skeys = jedis.keys(template);
		jedis.close();
		Set<String> keysSet;
		
		// if needed, split them on metric (":") 
		if (splitOnMetric) {
			keysSet = new HashSet<>();	
			for (String k : skeys)
				keysSet.add(k.split(":")[1].trim().toLowerCase());
		} else {
			keysSet = skeys;
		}

		List<String> keysList;

		// if needed get part of key by index
		if (idx >= 0) {
			keysList = new ArrayList<>();
			for (String k : keysSet) {
				keysList.add(k.split("\\.")[idx]);
			}
		} else {
			keysList = new ArrayList<>(keysSet);
		}

		// sort resulting list alphabetically 
		Collections.sort(keysList);
		return keysList;
	}

	public String getString(String key) {
		Jedis jedis = new Jedis(host, port);
		String value = jedis.get(key);
		jedis.close();
		if (value == null) value = "NA";			
		return value;
	}
	public int getInt(String key) {
		try {
			return (int)Double.parseDouble(getString(key));
		} catch(Exception ex) {
			return Integer.MIN_VALUE;			
		}		
	}
	public double getDouble(String key) {
		try {
			return Double.parseDouble(getString(key));
		} catch(Exception ex) {
			return Double.MIN_VALUE;			
		}		
	}

	/*
	 *   SINGLETON PATTERN
	 */
	private RedisDAO () {}
	private static class LazyHolder {
	    private static final RedisDAO INSTANCE = new RedisDAO();
	}
	protected Object readResolve() {
		return instance();
	}
	public static RedisDAO instance() {
	    return LazyHolder.INSTANCE;
	}
}
