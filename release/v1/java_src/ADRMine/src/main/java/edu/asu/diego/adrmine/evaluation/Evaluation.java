package edu.asu.diego.adrmine.evaluation;

import java.util.ArrayList;
import java.util.List;


public class Evaluation {
	int tp=0;
	int fp=0;
	int fn=0;
	private int tn=0;
	double precision= 0.0;
	double recall = 0.0;
	public List<String> truePositives= new ArrayList<String>();
	
	public double getFMeasure()
	{
		double f=0.0;
		double p= getPrecision();
		double r=getRecall();
		if (p!=0)
		{
			f= (double)2* p*r/(p+r);
		}
		
		return f;
	}
	public double getPrecision()
	{
		double p=0.0;
		if (tp!=0)
		{
			p=(double)tp/(tp+fp);
		}
		
		return p;
	}
	public double getRecall()
	{
		double r=0.0;
		if (tp!=0)
		{
			r=(double)tp/(tp+fn);
		}
		
		return r;
	}
	public void addTP()
	{
		tp++;
	}
	public void addFP()
	{
		fp++;
	}
	public void addFN()
	{
		fn++;
	}
	public void addTN()
	{
		tn++;
	}
	public void getEvaluation()
	{
		System.out.println("TP FP FN TN:    "+tp +"**"+fp +"**"+fn +"**"+tn);
		System.out.println("Precision:    "+getPrecision());
		System.out.println("Recall:    "+getRecall());
		System.out.println("FMeasure:    "+getFMeasure());
		
	}
	public int getTn() {
		return tn;
	}
	public void setTn(int tn) {
		this.tn = tn;
	}

}
