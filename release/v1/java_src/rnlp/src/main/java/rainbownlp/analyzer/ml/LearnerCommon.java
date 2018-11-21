package rainbownlp.analyzer.ml;

import java.sql.SQLException;

import rainbownlp.core.Setting;
import rainbownlp.core.Setting.OperationMode;
//import rainbownlp.db.entities.ArtifactTable;
//import rainbownlp.db.entities.RelationExampleTable;
//import rainbownlp.db.entities.ArtifactExampleTable;

public class LearnerCommon {

	public static void includeExamples(String updateTo) throws SQLException {
//		if(Setting.Mode==OperationMode.EDGE)
//		{
//			RelationExampleTable.setTestAsTrain();
//			RelationExampleTable.include(updateTo);
//		}
//		if(Setting.Mode==OperationMode.TRIGGER)
//		{
//			ArtifactExampleTable.setTestAsTrain();
//			ArtifactExampleTable.include(updateTo);
//		}
	}

	public static String[] getClassTitles() {
		String[] class_titles = new String[1];
		if (Setting.getValue("RelationMode").equals("BioNLP")) {
//			if(Configuration.Mode==OperationMode.EDGE)
//			{
//				class_titles = new String[BioConceptsRelation.BioRelationTypes.values().length];
//				for(int i=0;i<BioConceptsRelation.BioRelationTypes.values().length;i++)
//					class_titles[i] = BioConceptsRelation.BioRelationTypes.values()[i].name();
//			}else
//			{
//				class_titles = new String[BioNLPLoader.TriggerTypes.values().length];
//				for(int i=0;i<BioNLPLoader.TriggerTypes.values().length;i++)
//					class_titles[i] = BioNLPLoader.TriggerTypes.values()[i].name();
//			}
		}else if (Setting.getValue("RelationMode").equals("I2B2")) {
//			if(Configuration.Mode==OperationMode.ARTIFACT)
//			{
//				class_titles = new String[]{"NoRelation","HasRelation"};
//			}else
//			{
//				Configuration.Mode=OperationMode.EDGE;
//				class_titles = new String[ClinicalRelationTypes.values().length];
//				for(int i=0;i<ClinicalRelationTypes.values().length;i++)
//					class_titles[i] = ClinicalRelationTypes.values()[i].name();
//			}
		}
		return class_titles;
	}

	public static int getTrainingExamplesCount() throws SQLException {
		int trainigExamplesCount = 0;
//		if(Setting.Mode==OperationMode.EDGE)
//			trainigExamplesCount=RelationExampleTable.getTrainingExamplesCount();
//		if(Setting.Mode==OperationMode.TRIGGER)
//			trainigExamplesCount=ArtifactExampleTable.getTrainingExamplesCount();
//		if(Setting.Mode==OperationMode.ARTIFACT)
//			trainigExamplesCount=ArtifactTable.getTrainingExamplesCount();
		return trainigExamplesCount;
	}

	public static void setPortionOfTrainAsTest(int foldIndex, int foldCount) throws SQLException {
//		if(Setting.Mode==OperationMode.EDGE)
//			RelationExampleTable.setPortionOfTrainsetAsTest(foldIndex, foldCount);
//		if(Setting.Mode==OperationMode.TRIGGER)
//			ArtifactExampleTable.setPortionOfTrainsetAsTest(foldIndex, foldCount);
	}

	public static void excludeExamples(String whatToExclude) throws SQLException {
//		if(Setting.Mode==OperationMode.EDGE)
//			RelationExampleTable.exclude(whatToExclude);
//		if(Setting.Mode==OperationMode.TRIGGER)
//			ArtifactExampleTable.exclude(whatToExclude);
	}

}
