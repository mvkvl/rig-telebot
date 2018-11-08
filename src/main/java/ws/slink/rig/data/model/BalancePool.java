package ws.slink.rig.data.model;

import java.util.List;

import ws.slink.rig.data.RedisDAO;


public class BalancePool extends RigDataItem {

	protected static final String REDIS_KEY_TEMPLATE = "balance.pool";

	private BalancePool() {
		super(REDIS_KEY_TEMPLATE);
	}
	public List<String> pool() {
		return tags(0);
	}
	public List<String> crypto() {
		return tags(1);
	}

	public double balance(String pool, String crypto, String field) {
		String key = REDIS_KEY_TEMPLATE + "." + field + ":" + pool + "." + crypto;
		return RedisDAO.instance().getDouble(key);
	}

	public double balance(String poolCrypto, String field) {
		String key = REDIS_KEY_TEMPLATE + "." + field + ":" + poolCrypto;
		return RedisDAO.instance().getDouble(key);
	}

	/*
	 *   SINGLETON PATTERN
	 */
	private static class LazyHolder {
	    private static final BalancePool INSTANCE = new BalancePool();
	}
	protected Object readResolve() {
		return instance();
	}
	public static BalancePool instance() {
	    return LazyHolder.INSTANCE;
	}
	
	public static void main(String[] args) {
		RedisDAO.instance().init("192.168.1.2", 6379);
		BalancePool ri = BalancePool.instance();
		System.out.println(ri.fields());
		System.out.println(ri.crypto());
		System.out.println(ri.pool());
		System.out.println(ri.balance("suprnova", "rvn"));
		System.out.println(ri.balance("suprnova", "rvn", "confirmed"));
	}

}
