package ws.slink.tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Map;

import org.yaml.snakeyaml.Yaml;

public class BotConfig {

	private String telegramName;
	private String telegramToken;
	private int    telegramChat;
	private String redisHost;
	private int    redisPort;
	private String proxyHost;
	private int    proxyPort;


	public BotConfig(String fileName) {
		this.load(fileName);
		if (this.telegramToken == null ||
			this.telegramChat  ==    0 ||
			this.redisHost     == null ||  
			this.redisPort     ==    0  )
			throw new IllegalArgumentException("not all parameters set");
	}

	private void load(String fileName) {
		try {
			InputStream input = new FileInputStream(new File(fileName));
		    Yaml yaml = new Yaml();
		    Object data = yaml.load(input);
		    this.redisHost     = (String) getValue("redis.host"    , data); 
		    this.redisPort     = (int)    getValue("redis.port"    , data);
		    this.proxyHost     = (String) getValue("proxy.host"    , data);
		    this.proxyPort     = (int)    getValue("proxy.port"    , data);
		    this.telegramName  = (String) getValue("telegram.name",  data);
		    this.telegramToken = (String) getValue("telegram.token", data);
		    this.telegramChat  = (int)    getValue("telegram.chat" , data);
		} catch (Exception ex) {
			System.out.println(ex);
		}
	}
	
	@SuppressWarnings("rawtypes")
	private Object getValue(String path, Object data) {
		String [] parts = path.split("\\.", 2);
		if (data != null) {
			if  (data instanceof Map) {
				if (parts.length > 1)
					return (Object)getValue(parts[1], ((Map)data).get(parts[0]));
				else
					return ((Map)data).get(parts[0]);
			}
			else {
				return data;
			}
		} else {
			return null;	
		}
		
	}
	
	
	public String getTelegramName() {
		return telegramName;
	}
	public String getTelegramToken() {
		return telegramToken;
	}
	public int getTelegramChat() {
		return telegramChat;
	}
	public String getRedisHost() {
		return redisHost;
	}
	public int getRedisPort() {
		return redisPort;
	}
	public String getProxyHost() {
		return proxyHost;
	}
	public int getProxyPort() {
		return proxyPort;
	}
	
	public String proxy() {
		if (getProxyHost() != null && getProxyPort() != 0)
			return String.format("%s:%d", getProxyHost(), getProxyPort());
		else 
			return null;
	}
	
	public String toString() {
		String s = "";
		s += String.format("redis:\n   host: %s\n   port: %d\n", this.redisHost, this.redisPort);
		s += String.format("proxy:\n   host: %s\n   port: %d\n", this.proxyHost, this.proxyPort);
		s += String.format("telegram:\n   name: %s\n   token: %s\n   chat: %d", this.telegramName, this.telegramToken, this.telegramChat);
		return s; 
	}
}
