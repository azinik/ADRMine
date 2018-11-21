package rainbownlp.machineLearning;

import java.util.List;


public interface ILearnerEngine {
	public void train(List<MLExample> pTrainExamples) throws Exception ;
	public void test(List<MLExample> pTestExamples) throws Exception ;
	
}
