package rainbownlp.analyzer.evaluation;

import java.util.HashMap;



public class EvaluationResult {
	
	public HashMap<String, ResultRow> 
		evaluationResultByClass = 
			new HashMap<String, 
				ResultRow>();

	public String getReport()
	{
		String print_res = "";
		
		for(String evaluated_class: evaluationResultByClass.keySet())
		{
			ResultRow averageRow =
				evaluationResultByClass.get(evaluated_class);
			print_res += "Class: "+evaluated_class+"\n"; 
			print_res += averageRow.getReport();
		}
		
		print_res += "Micro-average :\n";
		print_res += getMicroAverage().getReport();
		
		return print_res;
	}
	public void printResult() {
		System.out.print(getReport());
//		Util.log(getReport(), Level.INFO);
	}


	public ResultRow getMicroAverage() {
		ResultRow micro_average = new ResultRow();
		for(String evaluated_class: evaluationResultByClass.keySet())
		{
			if(evaluated_class.equals("NotEdge") ||
					evaluated_class.equals("NotTrigger")) continue;
			ResultRow averageRow =
				evaluationResultByClass.get(evaluated_class);
//			Util.log("Class: "+evaluated_class, Level.INFO);
			micro_average.TP += averageRow.TP;
			micro_average.FP += averageRow.FP;
			micro_average.FN += averageRow.FN;
			micro_average.TN += averageRow.TN;
		}
		return micro_average;
	}
	
	
}
