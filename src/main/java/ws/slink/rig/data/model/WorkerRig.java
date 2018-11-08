package ws.slink.rig.data.model;

import java.util.ArrayList;
import java.util.List;

import ws.slink.rig.data.RedisDAO;

public class WorkerRig extends RigDataItem {

	protected static final String REDIS_KEY_TEMPLATE = "rig.worker"; //	"rig.worker.power:rig001.sn_rvn_zenemy"

	public WorkerRig() {
		super(REDIS_KEY_TEMPLATE);
	}
	public List<String> rigs() {
		return tags(0);
	}
	public List<String> workers() {
		return tags(1);
	}
	public List<String> activeRigWorkers() {
		List<String> result = new ArrayList<>();
		for (String r : rigs())
			for (String w : workers()) {
				if (value("hashrate", r, w) > 0.01 && value("power", r, w) > 0.01)
					result.add(r + "." + w);
			}
		return result;
	}
//	public Double value(String field, String rig, String worker) {
//		String key = REDIS_KEY_TEMPLATE + "." + field + ":" +  rig + "." + worker;
//		return RedisDAO.instance().getDouble(key);
//	}
//	public Double value(String field, String rigWorker) {
//		String key = REDIS_KEY_TEMPLATE + "." + field + ":" +  rigWorker;
//		return RedisDAO.instance().getDouble(key);
//	}

	/*
	 *   SINGLETON PATTERN
	 */
	private static class LazyHolder {
	    private static final WorkerRig INSTANCE = new WorkerRig();
	}
	protected Object readResolve() {
		return instance();
	}
	public static WorkerRig instance() {
	    return LazyHolder.INSTANCE;
	}
	
	public static void main(String[] args) {
		RedisDAO.instance().init("127.0.0.1", 6379);
		WorkerRig ri = WorkerRig.instance();
//		System.out.println(ri.fields());
//		System.out.println(ri.rigs());
//		System.out.println(ri.workers());
		System.out.println(ri.activeRigWorkers());
//		for (String aw : ri.activeRigWorkers()) {
//			System.out.println("HR: " + ri.value("hashrate", aw));
//			System.out.println("PW: " + ri.value("power", aw));
//		}
	}
}
