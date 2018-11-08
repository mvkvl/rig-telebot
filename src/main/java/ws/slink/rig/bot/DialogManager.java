package ws.slink.rig.bot;

import java.util.HashMap;
import java.util.Map;

import ws.slink.rig.bot.dialog.BackDialogCommand;
import ws.slink.rig.bot.dialog.ClearDialogCommand;
import ws.slink.rig.bot.dialog.Command;
import ws.slink.rig.bot.dialog.DialogCommand;
import ws.slink.rig.bot.dialog.DialogItem;
import ws.slink.rig.bot.dialog.TreeNode;
import ws.slink.rig.data.RigData;

public class DialogManager {
	
	private static final String STR_FOOTER = "----------------------------";
	
	/*
	 *   INSTANCE METHODS
	 */
	Map<String, TreeNode> nodes = new HashMap<>();

	String redisHost;
	int    redisPort;
	
	public TreeNode getNode(String callback) {
		if (nodes.isEmpty()) build();
		return nodes.get(callback);
	}
	public TreeNode getRoot() {
		if (nodes.isEmpty()) build();
		return nodes.get("root");
	}
	private void build() {
		// -- (+) Main menu
		TreeNode root = new DialogItem("Rig Info", "root");
		
		// -- (-) Rig
		TreeNode m1 = new DialogItem(" Rig ", "rig_menu_callback");
		root.add(m1);

			// -- (+) Hashrate
			m1.add(new DialogCommand(" Hashrate ", "rig_hashrate_command_callback", new Command() {
				@Override
				public String call(long chatId, long messageId) {
					return String.format("*** GPU hashrate ***\n<pre>%s\n%s\n%s</pre>", STR_FOOTER, RigData.instance().getGPUHashrate().trim(), STR_FOOTER);
				}
			}));

			// -- (+) Temperature
			m1.add(new DialogCommand(" Temperature ", "rig_temperature_command_callback", new Command() {
				@Override
				public String call(long chatId, long messageId) {
					return String.format("*** GPU temperature ***\n<pre>%s\n%s\n%s</pre>", STR_FOOTER, RigData.instance().getGPUTemperature().trim(), STR_FOOTER);
				}
			}));

			// -- (+) Power
			m1.add(new DialogCommand(" Power ", "rig_power_command_callback", new Command() {
				@Override
				public String call(long chatId, long messageId) {
					return String.format("*** GPU power ***\n<pre>%s\n%s\n%s</pre>", STR_FOOTER, RigData.instance().getGPUPower().trim(), STR_FOOTER);
				}
			}));
			
			m1.add(new ClearDialogCommand("rig_menu_reset"));
			m1.add(new BackDialogCommand("rig_menu_back"));
			
		// -- (+) Worker
			root.add(new DialogCommand(" Miner ", "miner_command_callback", new Command() {
				@Override
				public String call(long chatId, long messageId) {
					return String.format("*** Worker(s) hashrate ***\n<pre>%s\n%s\n%s</pre>", STR_FOOTER, RigData.instance().getWorkerHashrate().trim(), STR_FOOTER);
				}
			}));

		// -- (+) Balance
		root.add(new DialogCommand(" Balance ", "balance_command_callback", new Command() {
			@Override
			public String call(long chatId, long messageId) {
				return String.format("*** Balance ***\n<pre>%s\n%s\n%s</pre>", STR_FOOTER, RigData.instance().getBalance().trim(), STR_FOOTER);
			}
		}));
		
		root.add(new ClearDialogCommand("root_menu_reset"));

		fillNodes(root);
	}
	private void fillNodes(TreeNode node) {
		this.nodes.put(node.getCallback(), node);
		if (node.getChildren() != null)
			for (TreeNode c: node.getChildren())
				fillNodes(c);
	}
	
	/*
	 *   SINGLETON PATTERN
	 */
	private DialogManager () {}
	private static class LazyHolder {
	    private static final DialogManager INSTANCE = new DialogManager();
	}
	protected Object readResolve() {
		return instance();
	}
	public static DialogManager instance() {
	    return LazyHolder.INSTANCE;
	}
}
