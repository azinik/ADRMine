package rainbownlp.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class SystemUtil {
	/**
	 * Run synchronous shell command and wait till it finishes 
	 * @param command
	 */
	public static void runShellCommand(String command) {
		try {
			System.out.println(command);
			Runtime rt = Runtime.getRuntime();
			Process pr = rt.exec(command);
			

			BufferedReader input = new BufferedReader(new InputStreamReader(
					pr.getInputStream()));

			String line = null;

			while ((line = input.readLine()) != null) {
				System.out.println(line);
			}

			int exitVal = pr.waitFor();
			
			System.out.println("Exited with error code " + exitVal);

		} catch (Exception e) {
			System.out.println(e.toString());
			e.printStackTrace();
		}
	}
	public static void runBashCommand(String command) {
		try {
			System.out.println(command);
			Runtime rt = Runtime.getRuntime();
			Process pr = rt.exec(new String[] { "bash", "-c", command });
			

			
			BufferedReader input = new BufferedReader(new InputStreamReader(
					pr.getInputStream()));

			String line = null;

			while ((line = input.readLine()) != null) {
				System.out.println(line);
			}

			int exitVal = pr.waitFor();
			
			System.out.println("Exited with error code " + exitVal);

		} catch (Exception e) {
			System.out.println(e.toString());
			e.printStackTrace();
		}
	}
	

	 
	public static void runShellCommandAndGenerateOutputFile(String command, String resultFile) {    	
		try {
			System.out.println(command);
			Runtime rt = Runtime.getRuntime();
			Process pr = rt.exec(command);

			BufferedReader input = new BufferedReader(new InputStreamReader(
					pr.getInputStream()));

			String line = null;
			List<String> result_lines = new ArrayList<>();
			
			while ((line = input.readLine()) != null) {
//				System.out.println("running command: "+ line);
				result_lines.add(line);
			}
			FileUtil.createFile(resultFile, result_lines);

			int exitVal = pr.waitFor();
			
			System.out.println("Exited with error code " + exitVal);

		} catch (Exception e) {
			System.out.println("An error has occured while running CRFSuite ...");
			System.out.println(e.toString());
			e.printStackTrace();
			return;
		}
    }
	
}
