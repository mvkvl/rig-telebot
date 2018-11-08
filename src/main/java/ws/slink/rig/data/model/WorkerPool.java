package ws.slink.rig.data.model;

import java.util.ArrayList;
import java.util.List;

import ws.slink.rig.data.RedisDAO;

public class WorkerPool extends RigDataItem {

	protected static final String REDIS_KEY_TEMPLATE = "pool.worker";

	public WorkerPool() {
		super(REDIS_KEY_TEMPLATE);
	}
	public List<String> pools() {
		return tags(0);
	}
	public List<String> crypto() {
		return tags(1);
	}
	public List<String> workers() {
		return tags(2);
	}
	
	public List<String> activePoolWorkers() {
		List<String> result = new ArrayList<>();
		for (String p : pools())
			for (String c : crypto()) 
				for (String w : workers()) {
					if (value("hashrate", p, c, w) > 0.01)
						result.add(p + "." + c + "." + w);
				}
		return result;
	}
//	public Double value(String field, String pool, String crypto, String worker) {
//		String key = REDIS_KEY_TEMPLATE + "." + field + ":" +  pool + "." + crypto + "." + worker;
//		return RedisDAO.instance().getDouble(key);
//	}
//	public Double value(String field, String rigCryptoWorker) {
//		String key = REDIS_KEY_TEMPLATE + "." + field + ":" +  rigCryptoWorker;
//		return RedisDAO.instance().getDouble(key);
//	}
	
	/*
	 *   SINGLETON PATTERN
	 */
	private static class LazyHolder {
	    private static final WorkerPool INSTANCE = new WorkerPool();
	}
	protected Object readResolve() {
		return instance();
	}
	public static WorkerPool instance() {
	    return LazyHolder.INSTANCE;
	}
	
	public static void main(String[] args) {
		RedisDAO.instance().init("127.0.0.1", 6379);
		WorkerPool ri = WorkerPool.instance();
//		System.out.println(ri.fields());
//		System.out.println(ri.pools());
//		System.out.println(ri.crypto());
		System.out.println(ri.activePoolWorkers());
//		for (String aw : ri.activePoolWorkers()) {
//			System.out.println(aw + " HR (cur): " + ri.value("hashrate", aw));
//			System.out.println(aw + " HR ( 1d): " + ri.value("hashrate_average_1d", aw));
//			System.out.println(aw + " HR ( 7d): " + ri.value("hashrate_average_7d", aw));
//			System.out.println(aw + " HR (30d): " + ri.value("hashrate_average_30d", aw));
//		}
	}
}
