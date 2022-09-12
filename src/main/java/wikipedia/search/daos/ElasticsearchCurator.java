package wikipedia.search.daos;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public class ElasticsearchCurator {
	
	private final String curatorCmd = "curator --config D:\\config.yml D:\\action.yml";
	private final String curatorLocation = "C:\\Users\\User\\\\AppData\\Local\\Programs\\elasticsearch-curator\\";
	
	public String getCuratorCmd() {
		return curatorCmd;
	}
	
	public String getCuratorLocation() {
		return curatorLocation;
	}
	
	
	
	public void performRollover() throws IOException {
		
		Process process = Runtime.getRuntime().exec("cmd /c " + curatorCmd, null, new File(curatorLocation));
		printResults(process);
		
	}
	private void printResults(Process process) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
	    String line = "";
	    System.out.println("*******\nRollover results");
	    while ((line = reader.readLine()) != null) {
	        System.out.println(line);
	    }
	    System.out.println("*******");
		
	}

}
