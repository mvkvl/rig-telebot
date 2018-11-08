package ws.slink.rig.bot.dialog;

import java.util.List;

import org.telegram.telegrambots.meta.api.methods.BotApiMethod;

@SuppressWarnings("rawtypes")
public interface TreeNode{
	public void add(TreeNode node);
	public void remove(TreeNode node);
	public void remove(int i);
	public TreeNode getParent();
	public void setParent(TreeNode parent);
	public TreeNode getChild(int i);
	public List<TreeNode> getChildren();
	public String getTitle();
	public String getCallback();
	public BotApiMethod getAction(long chatId, long messageId, String messageText);
}
