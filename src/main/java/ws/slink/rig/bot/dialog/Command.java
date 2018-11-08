package ws.slink.rig.bot.dialog;

public interface Command {
	public String call(long chatId, long messageId);
}
