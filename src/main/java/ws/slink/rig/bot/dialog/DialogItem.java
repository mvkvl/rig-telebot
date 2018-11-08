package ws.slink.rig.bot.dialog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import static java.lang.Math.toIntExact;

public class DialogItem implements TreeNode {

	private static int BUTTONS_IN_ROW = 4;
	
	TreeNode parent = null;
	String title;
	String callback;

	List<TreeNode> children = new ArrayList<>();
	
	public DialogItem(String title, String callback) {
		this.callback = callback;
		this.title  = title;
	}
	
	@Override
	public void add(TreeNode node) {
		children.add(node);
		node.setParent(this);
	}

	@Override
	public void remove(TreeNode node) {
		children.remove(node);
		node.setParent(null);
	}

	@Override
	public void remove(int i) {
		TreeNode tn = children.get(i); 
		children.remove(i);
		tn.setParent(null);
	}

	@Override
	public TreeNode getParent() {
		return this.parent;
	}

	public void setParent(TreeNode parent) {
		this.parent = parent;
	}
	
	@Override
	public TreeNode getChild(int i) {
		return children.get(i);
	}

	@Override
	public List<TreeNode> getChildren() {
		return children;
	}

	public String getTitle() {
		return this.title;
	}
	
	@Override
	public String getCallback() {
		return this.callback;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public BotApiMethod getAction(long chatId, long messageId, String messageText) {

//		System.out.println(String.format("getAction enter: %s (%d : %d); children: %d", title, chatId, messageId, getChildren().size()));
		
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        
//        int children = (messageText == null) ? getChildren().size() : getChildren().size() - 1;
//        int rowSize = (children % BUTTONS_IN_ROW == 0)    ? 
//        		       children / BUTTONS_IN_ROW    : 
//        		       children / BUTTONS_IN_ROW + 1;
//        System.out.println(String.format("br: %d, c: %d, rs: %d", BUTTONS_IN_ROW, children, rowSize));
//        		             
        int idx = 0;
		List<InlineKeyboardButton> rowInline = new ArrayList<>();
		for (TreeNode c : getChildren()) {
			if (c instanceof ClearDialogCommand && messageText == null) continue;
//				System.out.println(String.format("adding button: %s (%s)", c.getTitle(), c.getCallback()));
	        rowInline.add(new InlineKeyboardButton().setText(c.getTitle()).setCallbackData(c.getCallback()));
	        idx++;
			if (idx >= BUTTONS_IN_ROW) {
				rowsInline.add(rowInline);
				rowInline = new ArrayList<>();
				idx = 0;
			}
		}
        rowsInline.add(rowInline);

		InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        markupInline.setKeyboard(rowsInline);

        if (messageId > 0) {
        	String prefix = (messageText != null && !messageText.isEmpty()) ? messageText + "\n" : "";
            EditMessageText new_message = new EditMessageText()
                    .setChatId(chatId)
                    .setMessageId(toIntExact(messageId))
                    .setReplyMarkup(markupInline)
                    .setText(String.format("%s%s", prefix, getDialogPath()));
            return new_message;
        } else {
            SendMessage message = new SendMessage() // Create a message object object
                    .setChatId(chatId)
                    .setText(getDialogPath());
            message.setReplyMarkup(markupInline);
            return message;
        }
	}
	
	private String getDialogPath() {
		List<String> titles = new ArrayList<>();
		TreeNode tn = this;
		titles.add(tn.getTitle());
		while(tn.getParent() != null) {
			tn = tn.getParent();
			titles.add(tn.getTitle());
		}
		Collections.reverse(titles);
		return titles.stream().map (i -> i.toString ()).collect(Collectors.joining (" -> "));
	}

	public String toString() {
		return String.format("%s, %s, %d", title, callback, children.size());
	}
}
