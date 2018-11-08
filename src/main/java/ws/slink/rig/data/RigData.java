package ws.slink.rig.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ws.slink.rig.data.model.BalancePool;
import ws.slink.rig.data.model.BalanceWallet;
import ws.slink.rig.data.model.RigGPU;
import ws.slink.rig.data.model.WorkerPool;
import ws.slink.rig.data.model.WorkerRig;

public class RigData {

	@SuppressWarnings("unused")
	private String valueToString(int value) {
		if (value == Integer.MIN_VALUE)
			return "NA";
		else
			try {
				return String.format("%d", value);
			} catch (Exception ex) {
				return "NA";
			}
	}
	private String valueToString(double value) {
		if (value == Double.MIN_VALUE)
			return "NA";
		else
			try {
				return (value > 10.0) ? String.format("%d", (int)value) : String.format("%.2f", value);
			} catch (Exception ex) {
				return "NA";
			}
	}
	
	private List<String> getCurrencies() {
		return BalanceWallet.instance().crypto();
	}

	// *** Workers
	private Map<String, List<String>> getActivePoolWorkersA() {
		Map<String, List<String>> result = new HashMap<>();
		for (String apw : WorkerPool.instance().activePoolWorkers()) {
			String pool = apw.split("\\.")[0];
			if (!result.containsKey(pool)) result.put(pool, new ArrayList<>());
			result.get(pool).add(apw.split("\\.")[1]);
		}
		return result;
	}
	private Map<String, String> getActivePoolWorkersB() {
		Map<String, String> result = new HashMap<>();
		for (String apw : WorkerPool.instance().activePoolWorkers())
			result.put(apw.split("\\.")[2], apw.substring(0, apw.lastIndexOf(".")));
		return result;
	}
	@SuppressWarnings("unused")
	private Map<String, List<String>> getActiveRigWorkersA() {
		Map<String, List<String>> result = new HashMap<>();
		for (String apw : WorkerRig.instance().activeRigWorkers()) {
			String rig = apw.split("\\.")[0];
			if (!result.containsKey(rig)) result.put(rig, new ArrayList<>());
			result.get(rig).add(apw.split("\\.")[1]);
		}
		return result;
	}
	private Map<String, String> getActiveRigWorkersB() {
		Map<String, String> result = new HashMap<>();
		for (String apw : WorkerRig.instance().activeRigWorkers())
			result.put(apw.split("\\.")[1], apw.split("\\.")[0]);
		return result;
	}
	public String getWorkerHashrate() {
		String result = "";
		Map<String, String> poolWorkers = getActivePoolWorkersB();
		Map<String, String> rigWorkers  = getActiveRigWorkersB();
		for (String w : rigWorkers.keySet()) {
			result += "" + w + ":\n";
			double rwhr   = WorkerRig.instance().value("hashrate",             rigWorkers.get(w), w);
			double rwhrad = WorkerRig.instance().value("hashrate_average_1d",  rigWorkers.get(w), w);
			double rwhraw = WorkerRig.instance().value("hashrate_average_7d",  rigWorkers.get(w), w);
			double rwhram = WorkerRig.instance().value("hashrate_average_30d", rigWorkers.get(w), w);

			double pwhr   = WorkerPool.instance().value("hashrate",             poolWorkers.get(w) + "." + w);
			double pwhrad = WorkerPool.instance().value("hashrate_average_1d",  poolWorkers.get(w) + "." + w);
			double pwhraw = WorkerPool.instance().value("hashrate_average_7d",  poolWorkers.get(w) + "." + w);
			double pwhram = WorkerPool.instance().value("hashrate_average_30d", poolWorkers.get(w) + "." + w);

			result += String.format("%6s %10s | %s\n", "", rigWorkers.get(w), poolWorkers.get(w).split("\\.")[0]);
			result += String.format("    current: %4s | %s\n", valueToString(rwhr), valueToString(pwhr));
			result += String.format(" average 1d: %4s | %s\n", valueToString(rwhrad), valueToString(pwhrad));
			result += String.format(" average 1w: %4s | %s\n", valueToString(rwhraw), valueToString(pwhraw));
			result += String.format(" average 1m: %4s | %s\n", valueToString(rwhram), valueToString(pwhram));
		}
		return result;
	}

	// *** Balances
	private String getWalletBalanceStr() {
		return (getWalletBalanceStr(getCurrencies()));
	}
	@SuppressWarnings("unused")
	private String getWalletBalanceStr(String currencies) {
		return (getWalletBalanceStr(Arrays.asList(currencies.split(","))));
	}
	private String getWalletBalanceStr(List<String> currencies) {
		String result = "";
		for (String c: currencies) {
			double h = BalanceWallet.instance().balance(c, "holding");
			double m = BalanceWallet.instance().balance(c, "mining");
			String hv = valueToString(h);
			String mv = valueToString(m);
			result += String.format("      %s: %7s [%7s]\n", c.toUpperCase().trim(), hv, mv);
		}
		return result;		
	}
	private String getPoolBalanceStr() {
		return getPoolBalanceStr(getActivePoolWorkersA());
	}
	private String getPoolBalanceStr(Map<String, List<String>> activePools) {
		String result = "";
		for (String p : activePools.keySet()) {
			result += "   " + p.substring(0, 1).toUpperCase() + p.substring(1).toLowerCase() + ":\n"; 
			for (String c: activePools.get(p)) {
				double b = BalancePool.instance().balance(p, c, "confirmed");
				double u = BalancePool.instance().balance(p, c, "unconfirmed");
				String cb = valueToString(b);
				String ub = valueToString(u);
				result += String.format("      %s: %7s [%7s]\n", c.toUpperCase().trim(), cb, ub);
			}
		}
		return result;		
	}
	public String getBalance() {
		String result = "Wallet:\n";
		result += getWalletBalanceStr();
		result += "Pool:\n";
		result += getPoolBalanceStr();
		return result;
	}

	// *** GPU Hashrates
	private String gpuInfoStr(String field) {
		return gpuInfoStr(field, false);
	}
	private String gpuInfoStr(String field, boolean total) {
		String result = "";
		for (String rig : RigGPU.instance().rigs()) {
			List<String> rigGPUs = RigGPU.instance().rigGPUs(rig);
			if (rigGPUs.size() > 0) {
				result += rig + ":\n";
				result += String.format("%5s %4s |%4s |%4s |%4s\n",
										"", "cur", "1d", "1w", "1m");
				// for total calculation
				double val_t_cur = 0;
				double val_t_ad1 = 0;
				double val_t_aw1 = 0;
				double val_t_am1 = 0;
				
				for (String gpuStr : rigGPUs) {
					int gpuId = -1;
					String pfx = "GPU ";
					try {
						gpuId = Integer.parseInt(gpuStr);
					} catch (NumberFormatException ex) {
						pfx = "";
					}
					if (gpuId >= 0) {
						// per GPU info
						double val_cur = RigGPU.instance().value(field, rig, gpuStr);
						double val_ad1 = RigGPU.instance().value(field + "_average_1d",  rig, gpuStr);
						double val_aw1 = RigGPU.instance().value(field + "_average_7d",  rig, gpuStr);
						double val_am1 = RigGPU.instance().value(field + "_average_30d", rig, gpuStr);
						result += String.format("%2s:%4.0f |%4.0f |%4.0f |%4.0f\n",
								                pfx + gpuId, val_cur, val_ad1, val_aw1, val_am1);
						val_t_cur += val_cur;
						val_t_ad1 += val_ad1;
						val_t_aw1 += val_aw1;
						val_t_am1 += val_am1;
					}		
				}
				if (total) {
					result += String.format("\n%2s:%4.0f |%4.0f |%4.0f |%4.0f\n",
			                "total", val_t_cur, val_t_ad1, val_t_aw1, val_t_am1);
				}
			}
		}
		return result;
	}
	
	public String getGPUHashrate() {
		return gpuInfoStr("hashrate");
	}
	public String getGPUTemperature() {
		return gpuInfoStr("temperature");
	}
	public String getGPUPower() {
		return gpuInfoStr("power", true);
	}
	
	/*
	 *   SINGLETON PATTERN
	 */
	private RigData () {}
	private static class LazyHolder {
	    private static final RigData INSTANCE = new RigData();
	}
	protected Object readResolve() {
		return instance();
	}
	public static RigData instance() {
	    return LazyHolder.INSTANCE;
	}
	
	
	public static void main(String[] args) {
		RedisDAO.instance().init("127.0.0.1", 6379);
//		System.out.println(RigData.instance().getWalletBalanceStr());
//		System.out.println(RigData.instance().getBalance());
//		System.out.println(WorkerPool.instance().activePoolWorkers());
//		System.out.println(RigData.instance().getActivePoolWorkersA());
//		System.out.println(RigData.instance().getActivePoolWorkersB());
//		System.out.println(RigData.instance().getActiveRigWorkersA());
//		System.out.println(RigData.instance().getActiveRigWorkersB());
//		System.out.println(RigData.instance().getWorkerHashrate());
		System.out.println(RigData.instance().getGPUHashrate());
		System.out.println(RigData.instance().getGPUTemperature());
		System.out.println(RigData.instance().getGPUPower());
	}
}
