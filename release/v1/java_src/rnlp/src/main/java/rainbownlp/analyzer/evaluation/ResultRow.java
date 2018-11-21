package rainbownlp.analyzer.evaluation;

import java.text.DecimalFormat;

public class ResultRow
{
	public int TP = 0;
	public int FP = 0;
	public int TN = 0;
	public int FN = 0;
	public String getReport() {
		String resultStr = "==================\n";
		resultStr += "\nTP : "+TP;
		resultStr += "\nFP : "+FP;
		resultStr += "\nTN : "+TN;
		resultStr += "\nFN : "+FN;
		resultStr += "\nPrecision : "+getPrecision();
		resultStr += "\nRecall : "+getRecall();
		resultStr += "\nFValue : "+getFValue();
		resultStr += "\n==================\n";
		
		return resultStr;
	}
	public void print() {
		System.out.println(getReport());
//		Util.log(getReport(), Level.INFO);
	}
	DecimalFormat twoDForm = new DecimalFormat("###.##");
    
	public double getRecall()
	{
		if((TP+FN) == 0) return 1;
		double r = 100*(double)TP/(double)(TP+FN);
		return Double.valueOf(twoDForm.format(r));
	}
	public double getPrecision()
	{
		if((TP+FP) == 0) return 1;
		double p = 100*(double)TP/(double)(TP+FP);
		return Double.valueOf(twoDForm.format(p));
	}
	public double getFValue()
	{
		double prec = getPrecision();
		double recall = getRecall();
		if((prec+recall) == 0) return 0;
		double f = 2*(prec*recall)/(prec+recall);
		 return Double.valueOf(twoDForm.format(f));
	}
}
