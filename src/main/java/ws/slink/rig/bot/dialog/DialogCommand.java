package ws.slink.rig.bot.dialog;

import java.util.List;

import org.telegram.telegrambots.meta.api.methods.BotApiMethod;

public class DialogCommand implements TreeNode {

	TreeNode parent = null;
	String    title;
	String callback;
	Command command;

	public DialogCommand(String title, String callback, Command command) {
		this.title = title;
		this.callback = callback;
		this.command = command;
	}
	
	@Override
	public void add(TreeNode node) {
		// unimplemented, as DialogCommand is a leaf node 
	}
	@Override
	public void remove(TreeNode node) {
		// unimplemented, as DialogCommand is a leaf node 
	}
	@Override
	public void remove(int i) {
		// unimplemented, as DialogCommand is a leaf node 
	}
	@Override
	public TreeNode getParent() {
		return parent;
	}
	@Override
	public void setParent(TreeNode parent) {
		this.parent = parent;
	}
	@Override
	public TreeNode getChild(int i) {
		return null;
	}
	@Override
	public List<TreeNode> getChildren() {
		return null;
	}
	@Override
	public String getTitle() {
		return title;
	}
	@Override
	public String getCallback() {
		return callback;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public BotApiMethod getAction(long chatId, long messageId, String messageText) {
		String callResult = this.command.call(chatId, messageId); 
		return this.getParent().getAction(chatId, messageId, callResult);
	}
}
