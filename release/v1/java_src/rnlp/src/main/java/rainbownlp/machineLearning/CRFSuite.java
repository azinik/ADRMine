package rainbownlp.machineLearning;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.cfg.Settings;
import org.hibernate.internal.SessionImpl;
import org.hibernate.metadata.CollectionMetadata;
import rainbownlp.core.Setting;
import rainbownlp.machineLearning.convertor.CRFSuiteFormatConvertor;
import rainbownlp.util.HibernateUtil;
import rainbownlp.util.SystemUtil;

public class CRFSuite implements ILearnerEngine {
	String modelFile;
	String taskName;
	String trainFile;
	String testFile;
	public String conceptTypes;
	
	int reinforcedCount = 0;
	String[] reinforcedModels = new String[reinforcedCount];
	
	public String CRFSuiteExecutable_arg;
	private File tempCRFSuiteFile;
	private String programMode="normal";
	public String corpusName;
	private CRFSuite()
	{
		
	}

	@Override
	public void train(List<MLExample> pTrainExamples) throws IOException, SQLException {
		Setting.TrainingMode = true;
		
		//This part added since the session was so slow
		List<Integer> train_example_ids = new ArrayList<Integer>();
		for(MLExample example : pTrainExamples)
		{
			train_example_ids.add(example.getExampleId());
		}
		
		CRFSuiteFormatConvertor.writeToFile(train_example_ids, trainFile,taskName,conceptTypes);
		
//		./crfsuite learn -m CRF.model -p feature.possible_states=1 -p feature.possible_transitions=1 /tmp/CRFSuite-train-ADRConceptTokenSeq.txt
		HibernateUtil.clearLoaderSession();
		
		
		SessionImpl sessionImpl = (SessionImpl) HibernateUtil.loaderSession;
		Connection conn = sessionImpl.connection();
		
		
		String c1="0.5";
		if (conn.getMetaData().getURL().toLowerCase().matches(".*twitter.*$"))
		{
			c1="0.3";
		}
		//for Twitter c1=0.3 for DS:0.5  
		String myShellScript = 
				getCRFSuiteExecutablePath()
			//Setting.getValue("CRFSuiteLearnerPath")
				+ " learn -m " +getModelFile()
//				+ "  -p max_iterations=500 -p feature.possible_states=1 -p feature.possible_transitions=1 " +trainFile;
//		+ " -a lbfgs -p c1=0.2 -p c2=0 -p feature.possible_states=1 -p feature.possible_transitions=1 " +trainFile;
				+ " -a lbfgs -p c1="+c1+" -p c2=0  -p feature.possible_states=1 -p feature.possible_transitions=1 "
				+ "-p max_iterations=200  -p epsilon=1e-3 " +trainFile;
				
		System.out.println("CRFSuite Command: "+myShellScript);
		System.out.println("the trained model is saved in "+getModelFile());
		SystemUtil.runShellCommand(myShellScript);

	}
	
	protected String getCRFSuiteExecutablePath() {
		if(this.CRFSuiteExecutable_arg != null)
			return CRFSuiteExecutable_arg;
		try {
			tempCRFSuiteFile = File.createTempFile("crfsuite", Long.toString(System.currentTimeMillis()));
//			tempCRFSuiteFile .deleteOnExit();
			tempCRFSuiteFile.setExecutable(true);
			
			
			InputStream inputStream = CRFSuite.class.getClassLoader().getResourceAsStream("crfsuite");
//			InputStream inputStream  =  new FileInputStream(CRFSuiteExecutable_arg);
			FileOutputStream outputStream =  new FileOutputStream(tempCRFSuiteFile);

			int read = 0;
			byte[] bytes = new byte[1024];

			while ((read = inputStream.read(bytes)) != -1) {
				outputStream.write(bytes, 0, read);
			}
			outputStream.flush();
			outputStream.close();
			inputStream.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Temp crfsuite file: " + tempCRFSuiteFile .getAbsolutePath());
		return tempCRFSuiteFile .getAbsolutePath();
	}

	//model file
	
	@Override
	public void test(List<MLExample> pTestExamples) throws Exception {
		
//		File model = new File(modelFile);
//		if(!model.exists()) return;
		
		Setting.TrainingMode = false;
		List<Integer> test_example_ids = new ArrayList<Integer>();
		String exampleids = "";
		for(MLExample example : pTestExamples)
		{
			exampleids = exampleids.concat(","+example.getExampleId());
			test_example_ids.add(example.getExampleId());
		}
			
		exampleids = exampleids.replaceFirst(",", "");
		String resetQuery = "update MLExample set predictedClass = -1 where exampleId in ("+ exampleids +")";
		HibernateUtil.executeNonReader(resetQuery);
		
		
		File resultFile =  new File(System.getProperty("user.dir")+"/predictions_"+Long.toString(System.currentTimeMillis())+".txt");
		
//		resultFile .deleteOnExit();
		String resultFilePath =resultFile.getPath();
		
		CRFSuiteFormatConvertor.writeToFile(test_example_ids, testFile,taskName,conceptTypes);
		
//		./crfsuite tag -m CRF.model /home/azadeh/projects/java/deext/data/forTrain=false.crfsuite.txt> predictions.txt
		String myShellScript = 
				getCRFSuiteExecutablePath() + " tag -m "
					+ getModelFile() +" "+ testFile;
		
		try {
			SystemUtil.runShellCommandAndGenerateOutputFile(myShellScript,resultFile.getPath());
		} catch (Exception e) {
			throw new Exception(e.getMessage());
		}
		
		
		
		
//		File f=new File(resultFilePath);
		if (!resultFile.exists()) {
			throw(new Exception("CRF result not generated!"));
		}
	
		// 2. read classification output and update database
		FileReader fileR = new FileReader(resultFile);
		BufferedReader reader = new BufferedReader(fileR);
		
		int counter = 0;
		
		while (counter<pTestExamples.size() && reader.ready()) {
			String line = reader.readLine();
			
			int classNum = Integer.parseInt(line.split(" ")[0]);
			System.out.println(classNum-1);
			MLExample test = pTestExamples.get(counter);
			test.setPredictedClass(classNum-1);//convert to index (e.g. 1 -> 0)
     		String savePredictedQuery = "update MLExample set predictedClass ="+test.getPredictedClass()+" where exampleId="+test.getExampleId();
			
			HibernateUtil.executeNonReader(savePredictedQuery);
			System.out.println(counter+"/"+pTestExamples.size());

			counter++;
		}

		assert !reader.ready() : "Something wrong file remained, updated rows:"+counter;
		assert counter==pTestExamples.size() : "Something wrong resultset remained, updated rows:"+counter;
		
		reader.close();
		
	}

	public static ILearnerEngine getLearnerEngine(String pTaskName) throws IOException {
		CRFSuite learnerEngine = new CRFSuite();
		learnerEngine.setTaskName(pTaskName);
		
		learnerEngine.setPaths();
		return learnerEngine;
	}

	private void setPaths() throws IOException {
		String fold = (Setting.crossFoldCurrent>0)?("Fold"+Setting.crossFoldCurrent):"";
		
		
//		File modelFileTemp =  File.createTempFile("modelFile_", Long.toString(System.currentTimeMillis()));
//		modelFileTemp .deleteOnExit();
//		String modelFilePath =modelFileTemp.getPath();
//		setModelFilePath(Paths.get(System.getProperty("java.io.tmpdir"), "crfsuite.model").toString());
		
		if(Setting.getValue("TrainsetFilePath") != null) {
			setTrainFilePath(Setting.getValue("TrainsetFilePath"));
		} else {
			setTrainFilePath(Paths.get(System.getProperty("java.io.tmpdir"),
					fold + "CRFSuite-train-" +
							taskName+".txt").toString());
		}


		if(Setting.getValue("TestsetFilePath") != null) {
			setTestFilePath(Setting.getValue("TestsetFilePath"));
			System.out.println(testFile);
//			TODO: remove this
		} else {
//			e.g. test file path /tmp/CRFSuite-test-ADRConceptTokenSeq.txt
			setTestFilePath(Paths.get(System.getProperty("java.io.tmpdir"),
					fold+"CRFSuite-test-"+
							taskName+".txt").toString());

		}
	}
	protected String getModelFile() throws IOException {
		if(programMode.matches("just-test"))
		{
			if (modelFile==null)
			{
				String provided_model = "crfsuiteModels/generalHealthRelated/crfsuite.model";
				
				if (corpusName.matches(".*twitter.*"))
					provided_model = "crfsuiteModels/twitter/crfsuiteTwitter.model";
//				    modelFile = 
//				    Thread.currentThread().getContextClassLoader().getResource("crfsuiteModels/twitter/crfsuiteTwitter.model").getPath();
//				else
//					modelFile = 
//				    Thread.currentThread().getContextClassLoader().getResource("crfsuiteModels/generalHealthRelated/crfsuite.model").getPath();
				try {
						File modelFileTemp =  File.createTempFile("crfsuite", ".model");
//						modelFileTemp .deleteOnExit();
						String modelFilePath =modelFileTemp.getPath();
					
					
					   setModelFilePath(modelFilePath);
					    
						
						InputStream inputStream = CRFSuite.class.getClassLoader().getResourceAsStream(provided_model);
//						InputStream inputStream  =  new FileInputStream(CRFSuiteExecutable_arg);
						FileOutputStream outputStream =  new FileOutputStream(modelFileTemp);

						int read = 0;
						byte[] bytes = new byte[1024];

						while ((read = inputStream.read(bytes)) != -1) {
							outputStream.write(bytes, 0, read);
						}
						outputStream.flush();
						outputStream.close();
						inputStream.close();

					  } catch (IOException e) {
						  //TODO
					  }
			}
			
		}
		else
		{
			if (modelFile==null)
			{
				File modelFileTemp =  File.createTempFile("crfsuite", ".model");
//				modelFileTemp .deleteOnExit();
				String modelFilePath =modelFileTemp.getPath();
				
				
				setModelFilePath(Paths.get(System.getProperty("java.io.tmpdir"), "crfsuite.model").toString());

			}
			
		}
		return modelFile;
	}
	public void setTestFilePath(String pTestFile) {
		testFile = pTestFile;
	}

	public void setTrainFilePath(String pTrainFile) {
		trainFile = pTrainFile;		
	}

	public void setModelFilePath(String pModelFile) {
		modelFile = pModelFile;
	}

	private void setTaskName(String pTaskName) {
		taskName = pTaskName;
	}

	public String getProgramMode() {
		return programMode;
	}

	public void setProgramMode(String programMode) {
		this.programMode = programMode;
	}


}
