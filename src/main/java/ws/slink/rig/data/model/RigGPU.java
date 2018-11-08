package ws.slink.rig.data.model;

import java.util.List;

import ws.slink.rig.data.RedisDAO;

public class RigGPU extends RigDataItem {

	protected static final String REDIS_KEY_TEMPLATE = "rig.gpu";

	public RigGPU() {
		super(REDIS_KEY_TEMPLATE);
	}
	public List<String> rigs() {
		return tags(0);
	}
	public List<String> gpus() {
		return tags(1);
	}
	public List<String> rigGPUs(String rig) {
		return RedisDAO.instance().keys(REDIS_KEY_TEMPLATE + ".*:" + rig + ".*", 1, true);
	}
	
	
	/*
	 *   SINGLETON PATTERN
	 */
	private static class LazyHolder {
	    private static final RigGPU INSTANCE = new RigGPU();
	}
	protected Object readResolve() {
		return instance();
	}
	public static RigGPU instance() {
	    return LazyHolder.INSTANCE;
	}
	
	public static void main(String[] args) {
		RedisDAO.instance().init("127.0.0.1", 6379);
		RigGPU ri = new RigGPU();
		
		ri.rigs().stream().forEach(r -> ri.rigGPUs(r).forEach(g -> System.out.println(r + ": " + g)));
		
//		System.out.println(ri.fields());
//		System.out.println();
//		System.out.println(ri.gpus());
	}

}
