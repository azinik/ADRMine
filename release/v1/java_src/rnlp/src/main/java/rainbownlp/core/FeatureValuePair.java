/**
 * @author ehsan
 */
package rainbownlp.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.Session;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.NaturalId;

import rainbownlp.util.FileUtil;
import rainbownlp.util.HibernateUtil;

/**
 * Store feature and value binded together
 */
@Entity
@Table( name = "FeatureValuePair" )
public class FeatureValuePair {
	public enum FeatureName {
		// Document Features
		JournalTitle,
		CompletedYear,
		CreatedYear,
		RevisedYear,
		MESHHeading,
		

		// Paragraph Features
		// PositionInDoc,

		// Sentence Features
		ProteinCountInSentence,
		SentenceTFIDF,

		// Words Features
		POS,
		POSNext1,
		POSNext2,
		POSPre1,
		POSPre2,
		
		PorterStem, 
		WordnetStem, 
		OriginalWord, 
		NameEntity, 
		StartWithUppercase, 
		AllUppercase, 
		AllLowercase, 
		HasSpecialChars, 
		HasDigit, 
		
		CommaLeftCount,
		CommaRightCount,
		QuoteLeftCount,
		QuoteRightCount,
		ProteinCountInWindow,
		
		SimilarityToGene_expression, 
		SimilarityToTranscription, 
		SimilarityToProtein_catabolism, 
		SimilarityToLocalization, 
		SimilarityToBinding, 
		SimilarityToPhosphorylation, 
		SimilarityToRegulation, 
		SimilarityToPositive_regulation, 
		SimilarityToNegative_regulation,
		PositionInDoc,
		
		//coreference features
		AnaphoraIsSubject,
		AntecedentInFirstSubject,
		AntecedentInHeader,
		AntecedentIsSubject,
		Appositive,
		NumberAgreement,
		SentenceDistance, TWOGram, TWOGramBackward, ThreeGram, ThreeGramBackward, NellLink, 
		ProblemCountInSentence, TestCountInSentence, TreatmentCountInSentence, TestsBeforeWord, 
		TreatmentsBeforeWord, ProblemsBeforeWord, ProblemPossibleCountInSentence, ProblemHypoCountInSentence, 
		ProblemConditionalCountInSentence, ProblemAWSECountInSentence, ProblemAbsentCountInSentence, ProblemPresentCountInSentence, EdgeType,
		WordWindowNext, WordWindowPre, EdgeParsePath, EdgeParseDistance, DependencyLinkedTokens,
		
		TimexCount, ClinicalEventsCount, LinkWordBetween, LinkArgumentType, LinkFromPhrasePolarity, LinkFromPhraseModality, LinkFromPhraseType, LinkToPhraseModality, LinkToPhraseType, 
		LinkToPhrasePolarity, LinkToPhraseTimexMod, LinkFromPhraseTimexMod, 
		InterMentionLocationType, AreDirectlyConnected, HaveCommonGovernors, AreConjunctedAnd,
		//NGrams
		NonNormalizedNGram2, NonNormalizedNGram3, NorBetweenNGram2, NorBetweenNGram3, Link2GramBetween, 
		Link2GramFrom,Link2GramTo,
		
		//Link Args basic features
		FromPhraseContent, ToPhraseContent, FromPhrasePOS, ToPhrasePOS,
		
		LinkBetweenWordCount, LinkBetweenPhraseCount, 
		
		//ParseDependency features
		FromPhraseRelPrep, ToPhraseRelPrep, FromPhraseGovVerb, ToPhraseGovVerb,  FromPhraseGovVerbTense,
		FromPhraseGovVerbAux, toPhraseGovVerbAux, areGovVerbsConnected,
		normalizedDependencies,
		//pattern statistics
		POverlapGivenPattern, PBeforeGivenPattern, PAfterGivenPattern, PNoLinkGivenPattern, hasFeasibleLink, 
		POverlapGivenPatternTTO, PBeforeGivenPatternTTO, PAfterGivenPatternTTO, PNoLinkGivenPatternTTO,
		maxProbClassByPattern,
		
		ParseTreePath, ParseTreePathSize,
		
		
		//sectime features
		relatedSectionInDoc, AdmissionOrDischarge, 
		//normalized 
		fromPrepArg, toPrepArg, isToPhDirectPrepArgOfFromPh, isEventAfterProblem, norToTypeDep,
		fromToToPathExist, toToFromPathExist, fromToToPathSize, toToFromPathSize, customGraphPath,
		//custom graph

		LabeledGraphNorDepPath,  customGraphIndividualPath, customGraphPathString,
		

		TemporalSignal, FromPhrasePOS1By1, ToPhrasePOS1By1, 
		FromPhrasePOSWindowBefore, ToPhrasePOSWindowBefore,  
		FromPhrasePOSWindowAfter, ToPhrasePOSWindowAfter, 
		ToPhrasePOSBigramAfter, FromPhrasePOSBigramBefore,
		ToPhrasePOSBigramBefore, FromToPhrasePOSBigram, LinkFromToWordDistance, LinkPOSBetween,
		
		
		betweenChunck, 
		
		//for the deext project
		chunkContent, embeddings,
		
		//For Twitter
		content, hasTermInADRDic, 
		
		//for conceptExtraction
		TokenContent, PrevTokenContent, NextTokenContent, SecondPrevTokenContent, SecondNextTokenContent, PositiveTokensInChunk, NegativeTokensInChunk, 
		PositiveTokensInNbrChunk, NegativeTokensInNbrChunk, MaxLexiconSim, isInLexiconEntry, GovVerb, MaxPhraseLexSim, BinaryLexiconSim, DeepClassNumber, PrevDeepClassNumber, SecondPrevDeepClassNumber, NextDeepClassNumber, SecondNextDeepClassNumber, GovVerbDeepClassNumber,
		isTokenInLexicon,isTokenNegated, TokenContentProcessed, isTokenNegatedExpanded,ThirdPrevTokenContent,ThirdNextTokenContent,ThirdPrevDeepClassNumber,
		ThirdNextDeepClassNumber,isPrevTokenInLexicon,isSPrevTokenInLexicon, isNextTokenInLexicon, isSNextTokenInLexicon,
		
		//for SVM classifier
		SVMCandTokenContent, SVMCandPrevTokenContent, SVMCandSecondPrevTokenContent, SVMCandNextTokenContent, SVMCandSecondNextTokenContent, SVMCandGovVerb, Token_IDF, Token_Lexicon_IDF, 
		SVMCandParSentToken, SVMCandParSentTokenIdf, SVMCandBigrams, SVMCandParSenDeepClassNumber, SVMCandThirdNextTokenContent, SVMCandThirdPrevTokenContent, 
		SVMIsPhraseNegatedExpanded, SVMIsPhraseNegated, 
		//hierachical clustering bitstring
		HClusterBitStringCurrent, PrevHClusterBitString, SecondPrevHClusterBitString, ThirdPrevHClusterBitString, NextHClusterBitString, SecondNextHClusterBitString, ThirdNextHClusterBitString,
		HClusterBitStringCurrent8, PrevHClusterBitString8, SecondPrevHClusterBitString8, ThirdPrevHClusterBitString8, NextHClusterBitString8, SecondNextHClusterBitString8, ThirdNextHClusterBitString8,
		HClusterBitStringCurrent4, PrevHClusterBitString4, SecondPrevHClusterBitString4, ThirdPrevHClusterBitString4, NextHClusterBitString4, SecondNextHClusterBitString4, ThirdNextHClusterBitString4, 
		
		Word2vecADRSimilarity, Word2vecIndicationSimilarity,  
		

		
	}

	String featureName;
	String featureValue;
	
	//For string multi features this can be used to handle real value 
	// othewise 1 or 0 would be used for values(for tf_idfs)
	private String featureValueAuxiliary;
	
	private int featureValuePairId = -1;
	
	//reset every time used for training a model;-1 means not used in training
	private int tempFeatureIndex = -1;
	
	
	
	public int getTempFeatureIndex() {
		return tempFeatureIndex;
	}
	public void setTempFeatureIndex(int tempFeatureIndex) {
		this.tempFeatureIndex = tempFeatureIndex;
	}
	public void setFeatureName(String _featureName) {
		featureName = _featureName;
	}
	@NaturalId
	public String getFeatureName() {
		return featureName;
	}

	
	public void setFeatureValue(String _featureValue) {
		featureValue = _featureValue;
	}
	@NaturalId
	public String getFeatureValue() {
		return featureValue;
	}
	
	public FeatureValuePair()
	{
	
	}
	

	
	public void setFeatureValuePairId(int featureValuePairId) {
		this.featureValuePairId = featureValuePairId;
	}
	@Id
	@GeneratedValue(generator="increment")
	@GenericGenerator(name="increment", strategy = "increment")
	public int getFeatureValuePairId() {
		return featureValuePairId;
	}

	
	void setFeatureValueAuxiliary(String featureValueAuxiliary) {
		this.featureValueAuxiliary = featureValueAuxiliary;
	}
	@NaturalId
	public String getFeatureValueAuxiliary() {
		return featureValueAuxiliary;
	}
	
	@Override public String toString()
	{
		return featureName+" = "+ featureValue + 
			" ("+featureValueAuxiliary+")";
	}
	
	@Override public boolean equals(Object pFeatureValuePair)
	{
		if(!(pFeatureValuePair instanceof FeatureValuePair))
			return false;
		FeatureValuePair fvp = (FeatureValuePair)pFeatureValuePair;
		if(fvp.getFeatureValuePairId() == featureValuePairId ||
				(fvp.getFeatureName() == featureName &&
					fvp.getFeatureValue().equals(featureValue)&&
					fvp.getFeatureValueAuxiliary() == featureValueAuxiliary))
			return true;
		else 
			return false;
		
	}
	@Override public int hashCode()
	{
		return featureValuePairId;
	}
	public static FeatureValuePair getInstance(String pFeatureName, 
			String pFeatureValue,
			String pFeatureValueAuxiliary){
		return getInstance(pFeatureName, pFeatureValue, pFeatureValueAuxiliary, false);
	}
	public static FeatureValuePair getInstance(String pFeatureName, 
			String pFeatureValue,
			String pFeatureValueAuxiliary,
			boolean isDense)
	{
		FeatureValuePair feature_value;

	
		
		String hql = "from FeatureValuePair where featureName= :featureName "+
					" AND featureValue= :featureValue ";
		
		HashMap<String, Object> params = new HashMap<String, Object>();
		params.put("featureValue", pFeatureValue);
		params.put("featureName", pFeatureName);
		
		if(pFeatureValueAuxiliary!=null)
		{
			hql += " AND featureValueAuxiliary= :featureValueAuxiliary ";
			params.put("featureValueAuxiliary", pFeatureValueAuxiliary);
		}
		
		List<FeatureValuePair> featurev_list = 
			(List<FeatureValuePair>) HibernateUtil.executeReader(hql, params);
		
		if(featurev_list.size() == 0)
		{
			feature_value = new FeatureValuePair();
			feature_value.setFeatureName(pFeatureName);
			feature_value.setFeatureValue(pFeatureValue);
			if(pFeatureValueAuxiliary!=null)
				feature_value.setFeatureValueAuxiliary(pFeatureValueAuxiliary);
			HibernateUtil.save(feature_value);
		}else
			feature_value = featurev_list.get(0);
		return feature_value;
	}
	
	public static FeatureValuePair getInstance(String pFeatureName, 
			String pFeatureValue, boolean isDense)
	{
	
		return getInstance(pFeatureName, pFeatureValue, null, isDense);
	}
	public static FeatureValuePair getInstance(String pFeatureName, 
			String pFeatureValue)
	{
	
		return getInstance(pFeatureName, pFeatureValue, null, false);
	}
	
	public static List<String> multiValueFeatures = new ArrayList<String>();
	static
	{
		multiValueFeatures.add(FeatureName.MESHHeading.name());
		multiValueFeatures.add(FeatureName.SentenceTFIDF.name());
		multiValueFeatures.add(FeatureName.LinkWordBetween.name());
	}
	@Transient
	//TODO make it better
	public boolean isMultiValue()
	{
		boolean res = false;
		
		if(multiValueFeatures.contains(featureName))
			res = true;

		return res;
	}
	public static void resetIndexes() {
		String hql = "update FeatureValuePair set tempFeatureIndex = "+Integer.MAX_VALUE;
		HibernateUtil.executeNonReader(hql);
		Session temp_Session = HibernateUtil.sessionFactory.openSession();
		String selecthql = "from FeatureValuePair";


		List<FeatureValuePair> features = 
			(List<FeatureValuePair>) HibernateUtil.executeReader(selecthql,null,null, temp_Session);
		int new_feature_index = 0;
		int count =0;
		for(int i=0;i<features.size();i++) {
			count++;
			
//			FileUtil.logLine(null,"resetIndexes--------feature processed: "+count+"/"+features.size());
			temp_Session = HibernateUtil.clearSession(temp_Session);
			
			//load again fvp to get effect of bulk update
			String selectfvp = "from FeatureValuePair where featureValuePairId = "+features.get(i).getFeatureValuePairId();
			
			FeatureValuePair fvp  = 
				((List<FeatureValuePair>) HibernateUtil.executeReader(selectfvp,null,null, temp_Session)).get(0);
			
			int featureIndex = 
					fvp.getTempFeatureIndex();
			if(featureIndex==Integer.MAX_VALUE)
			{
				new_feature_index ++;
				if(fvp.getFeatureValueAuxiliary()!=null)
				{
					featureIndex = new_feature_index;
					fvp.setTempFeatureIndex(featureIndex);
					HibernateUtil.save(fvp, temp_Session);
				}else
				{
					//feature index for attribute not set before
					//find one for the attribute
					featureIndex = new_feature_index;
					
					String update_attribute_index_hql = 
						"UPDATE FeatureValuePair set tempFeatureIndex = "+ featureIndex
						+ " where featureName='"+fvp.getFeatureName()+"'";
					HibernateUtil.executeNonReader(update_attribute_index_hql);
				}
			}
		}
		temp_Session.clear();
		temp_Session.close();
	}
	public static int getMaxIndex() {
		Session tmp_session = HibernateUtil.sessionFactory.openSession();
		String hql = "select max(tempFeatureIndex) from FeatureValuePair where tempFeatureIndex<"+Integer.MAX_VALUE;
		List res = HibernateUtil.executeReader(hql, null,null,tmp_session);
		tmp_session.clear();
		tmp_session.close();
		return (res.get(0)==null)?0:Integer.valueOf(res.get(0).toString());
	}
	public static int getMinIndexForAttribute(String attributeName) {
		String hql = "select min(tempFeatureIndex) from FeatureValuePair where " +
				"featureName='" + attributeName + "'";
		List res = HibernateUtil.executeReader(hql);
		return (res.get(0)==null)?0:Integer.valueOf(res.get(0).toString());
	}
	public static FeatureValuePair getInstance(FeatureName linkType,
			String pFeatureValue) {
		
		return getInstance(linkType.name(), pFeatureValue);
	}
	
	public static FeatureValuePair getInstance(FeatureName linkType,
			String pFeatureValue, String auxValue) {
		
		return getInstance(linkType.name(), pFeatureValue, auxValue);
	}
	public static Integer getRelatedFromEventTypeFValuePairIds(
			String pFeatureValue)
	{
		Integer feature_value_id =null;
		
//		String featureValueString = "";
//		for(String val: pFeatureValues)
//		{
//			featureValueString = featureValueString.concat(", '"+val+"'");
//		}
//		featureValueString = featureValueString.replaceFirst(",", "");
		
		String hql = "from FeatureValuePair where featureName in ('LinkFromPhraseType') "+
					" AND featureValue = '"+pFeatureValue+"' ";
		
		
		List<FeatureValuePair> featurev_list = 
			(List<FeatureValuePair>) HibernateUtil.executeReader(hql);
		if (featurev_list.size() !=0)
		{
			feature_value_id = featurev_list.get(0).getFeatureValuePairId();
		}
		
		return feature_value_id;
	}
	public static Integer getRelatedToEventTypeFValuePairIds(
		String pFeatureValue)
	{
		Integer feature_value_id =null;
		
//		String featureValueString = "";
//		for(String val: pFeatureValues)
//		{
//			featureValueString = featureValueString.concat(", '"+val+"'");
//		}
//		featureValueString = featureValueString.replaceFirst(",", "");
		
		String hql = "from FeatureValuePair where featureName in ('LinkToPhraseType') "+
					" AND featureValue ='"+pFeatureValue+"' ";
		
		
		List<FeatureValuePair> featurev_list = 
			(List<FeatureValuePair>) HibernateUtil.executeReader(hql);
		if (featurev_list.size() !=0)
		{
			feature_value_id = featurev_list.get(0).getFeatureValuePairId();
		}
		
		return feature_value_id;
	}
	public static List<FeatureValuePair> getInstancesBulk(
			List<String> vector_values, String featurePrefix) {
		
		String valuesOfInterest = "";
		for (Integer d=0;d<vector_values.size();d++)
		{
			String curValue = vector_values.get(d);
			valuesOfInterest+="'"+curValue+"',";
		}
		valuesOfInterest+="''";
		String hql = "from FeatureValuePair where featureName like :featurePrefix and featureValue in ("+valuesOfInterest+")";
		
		HashMap<String, Object> params = new HashMap<String, Object>();
		params.put("featurePrefix", featurePrefix+"%");
		
		List<FeatureValuePair> featurev_list = 
			(List<FeatureValuePair>) HibernateUtil.executeReader(hql, params);
		HibernateUtil.startTransaction();;
		Session session = HibernateUtil.saverSession;
		List<FeatureValuePair> fvpsResult = new ArrayList<FeatureValuePair>();
		
		for (Integer d=0;d<vector_values.size();d++)
		{
			FeatureValuePair feature_value = null;
			String curValue = vector_values.get(d);
			boolean found = false;
			for(FeatureValuePair fvp : featurev_list){
				if(fvp.getFeatureName().equals(featurePrefix+d) && 
						fvp.getFeatureValue().equals(curValue)){
					feature_value = fvp;
					found = true;
					break;
				}
			}
			
			if(!found){
				feature_value = new FeatureValuePair();
				feature_value.setFeatureName(featurePrefix+d);
				feature_value.setFeatureValue(curValue);
				session.save(feature_value);
			}
			
			fvpsResult.add(feature_value);
		}
		
		HibernateUtil.endTransaction(); 
		
		
		return fvpsResult;
	}
	
	
}


