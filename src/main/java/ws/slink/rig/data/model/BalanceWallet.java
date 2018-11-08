package ws.slink.rig.data.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ws.slink.rig.data.RedisDAO;

public class BalanceWallet extends RigDataItem {

	protected static final String REDIS_KEY_TEMPLATE = "balance.wallet";

	public BalanceWallet() {
		super(REDIS_KEY_TEMPLATE);
	}
	public List<String> crypto() {
		return tags(0);
	}
	
	public Map<String, Double> balance(String crypto) {
		Map<String, Double> result = new HashMap<>();
		for(String f : fields()) {
			String key = REDIS_KEY_TEMPLATE + "." + f + ":" + crypto;
			result.put(f, RedisDAO.instance().getDouble(key));
		}
		return result;
	}

	public double balance(String crypto, String wallet) {
		String key = REDIS_KEY_TEMPLATE + "." + wallet + ":" + crypto;
		return RedisDAO.instance().getDouble(key);
	}

	/*
	 *   SINGLETON PATTERN
	 */
	private static class LazyHolder {
	    private static final BalanceWallet INSTANCE = new BalanceWallet();
	}
	protected Object readResolve() {
		return instance();
	}
	public static BalanceWallet instance() {
	    return LazyHolder.INSTANCE;
	}
	
	public static void main(String[] args) {
		RedisDAO.instance().init("192.168.1.2", 6379);
		BalanceWallet ri = BalanceWallet.instance();
		System.out.println(ri.fields());
		System.out.println(ri.crypto());
		System.out.println(ri.balance("rvn"));
		System.out.println(ri.balance("rvn", "holding"));
	}
}
