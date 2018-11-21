package rainbownlp.analyzer.evaluation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import rainbownlp.core.Setting;
import rainbownlp.machineLearning.ILearnerEngine;
import rainbownlp.machineLearning.MLExample;


public class CrossValidation {
	
	ILearnerEngine mlModel;
	public CrossValidation(ILearnerEngine learningEngine)
	{
		mlModel = learningEngine;
	}
	public EvaluationResult crossValidation(List<MLExample> examples, int folds) throws Exception
	{

		int foldCount = examples.size()/folds;
		ArrayList<EvaluationResult> results = 
			new ArrayList<EvaluationResult>();
		for(int foldIndex = 0;foldIndex<folds;foldIndex++)
		{
			Setting.crossFoldCurrent = foldIndex+1;
			int start_index = foldIndex*foldCount;
			int end_index = (foldIndex+1)*foldCount;
			if(end_index>=examples.size()) 
				end_index = examples.size();
			
//			HibernateUtil.startTransaction();
			
			List<MLExample> train_set = new ArrayList<MLExample>();
			for(int i=0;i<start_index;i++)
				train_set.add(examples.get(i).clone());
			for(int i=end_index;i<examples.size();i++)
				train_set.add(examples.get(i).clone());
				
			
			mlModel.train(train_set);
			train_set = null;
			System.gc();
			
			List<MLExample> test_set = new ArrayList<MLExample>();
			for(int i=start_index;i<end_index;i++)
				test_set.add(examples.get(i).clone());
			
			mlModel.test(test_set);
			
//			HibernateUtil.endTransaction();
			results.add(Evaluator.getEvaluationResult(test_set));

		}
		

		HashMap<String, ResultRow> 
			evaluationAverageResult = 
			new HashMap<String, 
				ResultRow>();
		for(EvaluationResult fold_result : results)
		{
			for(String evaluated_class: fold_result.evaluationResultByClass.keySet())
			{
				ResultRow row =
					fold_result.evaluationResultByClass.get(evaluated_class);
				ResultRow averageRow =
					evaluationAverageResult.get(evaluated_class);
				if(averageRow==null)
					evaluationAverageResult.put(evaluated_class,row);
				else{
					averageRow.FN += row.FN;
					averageRow.FP += row.FP;
					averageRow.TN += row.TN;
					averageRow.TP += row.TP;
				}
			}
		}
		for(String evaluated_class: evaluationAverageResult.keySet())
		{
			ResultRow averageRow =
				evaluationAverageResult.get(evaluated_class);
//			Util.log("Class: "+evaluated_class, Level.INFO);
			averageRow.print();
		}
		EvaluationResult er = new EvaluationResult();
		er.evaluationResultByClass = evaluationAverageResult;
		
		Setting.crossFoldCurrent = 0;
		
		return er;
	}

}
