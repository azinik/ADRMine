package rainbownlp.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;


public class Setting {
	public static final String RuleSureCorpus = "RuleSure";
	public static boolean SaveInGetInstance = true;
	static Properties configFile;
	public enum OperationMode
	{
		TRIGGER,
		EDGE,
		ARTIFACT
	}
	public static OperationMode Mode = OperationMode.TRIGGER;
	public static boolean TrainingMode = true;
	// This switch between using Development set or Test set for evaluation, set to true if you want to generate test submission files
	public static boolean ReleaseMode = false;
	public static int NotTriggerNumericValue = 10;
	public static int NotEdgeNumericValue = 9;
	public static int MinInstancePerLeaf;
	public static Double SVMCostParameter;
	public static Double SVMPolyCParameter;
	public static enum SVMKernels  {
		  Linear, //0: linear (default)
        Polynomial, //1: polynomial (s a*b+c)^d
        Radial, //2: radial basis function exp(-gamma ||a-b||^2)
        SigmoidTanh //3: sigmoid tanh(s a*b + c)
	};
	public static SVMKernels SVMKernel;

	public static boolean batchMode = false;
	public static int crossValidationFold;
	public static int crossFoldCurrent = 0;
	
//	public static String[] getClasses()
//	{
//		String[] classes = getValue("classes").split("|");
//		return classes;
//	}
	public static void init()
	{
		if(configFile == null){
			  configFile = new Properties();
			try {
				//File currentDirectory = new File(new File(".").getAbsolutePath());
				
//				InputStream config_file = new FileInputStream(currentDirectory.getCanonicalPath()+
//						"/examples/adrmineevaluation/src/main/resources/configuration.conf");//
//				InputStream config_file = new FileInputStream(currentDirectory.getCanonicalPath()+
//						"/configuration.conf");
				InputStream config_file =Setting.class.getClassLoader()
						.getResourceAsStream("configuration.conf");
				configFile.load(config_file);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			MinInstancePerLeaf = Integer.parseInt(configFile.getProperty("MinInstancePerLeaf"));
			SVMCostParameter = Double.parseDouble(configFile.getProperty("SVMCostParameter"));
			SVMPolyCParameter = Double.parseDouble(configFile.getProperty("SVMPolyCParameter"));
			ReleaseMode = Boolean.parseBoolean(configFile.getProperty("ReleaseMode"));
			SVMKernel = SVMKernels.values()[Integer.parseInt(configFile.getProperty("SVMKernel"))];
		}
	}
	
	public static String getValue(String key){
		init();
		return configFile.getProperty(key);
	}

	public static int getValueInteger(String key) {
		int result = Integer.parseInt(getValue(key));
		return result;
	}
	

	
	
}
