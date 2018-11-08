package ws.slink.rig.bot;

import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.ApiContext;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import ws.slink.rig.data.RedisDAO;
import ws.slink.tools.BotConfig;
import ws.slink.tools.ParamsParser;

public class RigBot extends TelegramLongPollingBot {

	/* bot builder to create a bot (proxy-connected if configured */
	public static class BotBuilder {
		private String    _proxy_env_str;
		private String    _proxy_str;
		private BotConfig _bot_config; 

		public BotBuilder proxy_env(String proxy_env_str) {
			this._proxy_env_str = proxy_env_str;
			return this;
		}
		public BotBuilder proxy(String proxy_str) {
			this._proxy_str = proxy_str;
			return this;
		}
		public BotBuilder config(BotConfig bot_config) {
			this._bot_config = bot_config;
			return this;
		}

		private String getProxyStr() {
			if (this._proxy_env_str == null && this._proxy_str == null)
				if (this._bot_config != null)
					return this._bot_config.proxy();
				else
					return null;
			else if (this._proxy_str != null && !this._proxy_str.isEmpty())
				return this._proxy_str;
			else if (this._proxy_env_str != null && !this._proxy_env_str.isEmpty()) {
				return System.getenv(this._proxy_env_str);
			} else 
				return null;
		}
		private DefaultBotOptions configureBotOptions(String proxyString) {
			
			DefaultBotOptions botOptions = ApiContext.getInstance(DefaultBotOptions.class);
			
			String host = null, port_str = null; 
			String [] parts = proxyString.split(":");
			if (parts.length > 1) {
				host = parts[parts.length-2].replaceAll("/", "");
				port_str = parts[parts.length-1];
			}
			if (host != null && port_str != null) {
				int port = Integer.parseInt(port_str);
				HttpHost httpHost = new HttpHost(host, port);
				RequestConfig requestConfig = RequestConfig.custom().setProxy(httpHost).setAuthenticationEnabled(false).build();
				botOptions.setRequestConfig(requestConfig);
				botOptions.setProxyHost(host);
				botOptions.setProxyPort(port);
			}
			
			return botOptions;
		}
		
		public RigBot build() {
			String proxyString = getProxyStr();
			if (proxyString == null) {
				System.out.println("starting direct-connected bot...");
				return new RigBot(_bot_config.getTelegramToken(), 
						           _bot_config.getTelegramName(),
						           _bot_config.getTelegramChat());
			}
			else {
				System.out.println("starting proxy-connected bot...");
				return new RigBot(_bot_config.getTelegramToken(), 
						           _bot_config.getTelegramName(), 
						           _bot_config.getTelegramChat(), 
						           configureBotOptions(proxyString));
			}
		}
	}
	
	private String token;
	private String  name;
	private int     chat;  
	
	private RigBot(String token, String name, int ownerChatId, DefaultBotOptions opts) {
		super(opts);
		this.name  = name;
		this.token = token;
		this.chat  = ownerChatId;
	}

	private RigBot(String token, String name, int ownerChat) {
		super();
		this.name  = name;
		this.token = token;
		this.chat  = ownerChat;
	}
	
	public String getBotUsername() {
		return this.name;
	}

	@Override
	public String getBotToken() {
		return this.token;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void onUpdateReceived(Update update) {

		BotApiMethod message = null;
		long chat_id         = 0;
		
		if (update.hasMessage() && update.getMessage().hasText()) {
			String message_text = update.getMessage().getText();
	        chat_id = update.getMessage().getChatId();
	        if (chat_id == this.chat) {
		        if (message_text.equals("/start")) {
		        	SendMessage mes = (SendMessage)DialogManager.instance().getRoot().getAction(chat_id, 0, null);
		        	mes.enableHtml(true);
		        	message = mes;
		        } 
	        } else {
	        	// access denied
				System.out.println("access denied (" + chat_id + ")");
	        }
		} else if (update.hasCallbackQuery()) {
        	String call_data = update.getCallbackQuery().getData();
        	chat_id          = update.getCallbackQuery().getMessage().getChatId();
        	long message_id  = update.getCallbackQuery().getMessage().getMessageId();
        	EditMessageText emt = (EditMessageText)DialogManager.instance().getNode(call_data).getAction(chat_id, message_id, null);
        	emt.enableHtml(true);
            message = emt;
        } else {
        	System.out.println("update has no text");
		}
		
		if (chat_id == this.chat) {
			if (message != null)
		        try {
		            execute(message); // Sending our message object to user
		        } catch (TelegramApiException e) {
//		        	System.err.println(e.getLocalizedMessage());
		            e.printStackTrace();
		        }
	        else
	        	System.out.println("no any reply supposed to be, passing by...");
		} else {
			System.out.println("access denied (" + chat_id + ")");
		}
	}
		
	public static void main(String[] args) {
		
        // *** Parse command line arguments (for mandatory parameter "--config")
		ParamsParser pp = new ParamsParser(args);

        // *** Read configuration from config file
		BotConfig    bc = new BotConfig(pp.configFilePath);

        // *** configure data access object
		RedisDAO.instance().init(bc.getRedisHost(), bc.getRedisPort());
		
        // *** Initialize Api Context
        ApiContextInitializer.init();

        // *** Instantiate Telegram Bots API
        TelegramBotsApi botsApi = new TelegramBotsApi();

        // *** Create bot
        RigBot bot = new RigBot.BotBuilder().config(bc).build(); //.proxy_env("bot_proxy").proxy(proxy).build();
        
        // *** Register bot
        try {
            botsApi.registerBot(bot);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
