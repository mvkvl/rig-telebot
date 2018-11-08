package ws.slink.tools;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;

public class ParamsParser {

	@Parameter(names={"-c", "--config"}, description = "configuration file path")
	public String configFilePath;
	
	public ParamsParser(String [] args) {
		
		JCommander.newBuilder()
		  .addObject(this)
		  .build()
		  .parse(args);
		
		if (this.configFilePath == null || this.configFilePath.isEmpty()) {
			System.out.println("Usage: <program> {-c | -- config} <configuration file>");
			System.exit(1);
		}
	}
}
