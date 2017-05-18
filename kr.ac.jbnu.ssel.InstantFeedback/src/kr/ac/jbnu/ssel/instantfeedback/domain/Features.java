package kr.ac.jbnu.ssel.instantfeedback.domain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import kr.ac.jbnu.ssel.instantfeedback.tool.IdentifierParser;

public class Features {
	private String libraryName;
	private String packageName;
	private String className;
	private String methodName;

	private int LOC;
	private int numOfMethodInvocation;
	private int numOfBranch;
	private int numOfLoops;
	private int numOfAssignment;
	private int numOfComments;
	private int numOfArithmaticOperators;
	private int numOfBlankLines;
	private int numOfStringLiteral;
	private int numOfLogicalOperators;
	private int numOfBitOperators;
	private int numOfDuplicatedLines;
	private int numOfDuplicatedBlocks;
	private int maxNestedControl;

	private List<String> operands;
	private List<String> uniqueOperands;
	private List<String> operators;
	private List<String> uniqueOperators;
	private double programVolume;

	private List<String> allWords;
	private Map<String, Integer> termFrequencyMatrix;
	private double entropy;

	private List<String> variableNames;
	private double averageOfVariableNameLength;

	private List<String> allLines;
	private double averageLineLength;

	private List<String> seperatedMethods;
	private List<Map<String, Integer>> termFrequencyMatrics;
	private double patternRate;

	public Features() {
		super();
		libraryName = "";
		packageName = "";
		className = "";
		methodName = "";

		LOC = 0;
		numOfMethodInvocation = 0;
		numOfBranch = 0;
		numOfLoops = 0;
		numOfAssignment = 0;
		numOfComments = 0;
		numOfArithmaticOperators = 0;
		numOfBlankLines = 0;
		numOfStringLiteral = 0;
		numOfLogicalOperators = 0;
		numOfBitOperators = 0;
		maxNestedControl = 0;

		operands = new ArrayList<String>();
		operators = new ArrayList<String>();
		programVolume = 0.0;

		allWords = new ArrayList<String>();
		termFrequencyMatrix = new HashMap<String, Integer>();
		entropy = 0.0;

		variableNames = new ArrayList<String>();
		averageOfVariableNameLength = 0.0;

		allLines = new ArrayList<String>();
		averageLineLength = 0.0;

		seperatedMethods = new ArrayList<String>();
		termFrequencyMatrics = new ArrayList<Map<String, Integer>>();
		patternRate = 0.0;
	}

	public String getLibraryName() {
		return libraryName;
	}

	public void setLibraryName(String libraryName) {
		this.libraryName = libraryName;
	}

	public String getPackageName() {
		return packageName;
	}

	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public String getMethodName() {
		return methodName;
	}

	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}

	public int getNumOfMethodInvocation() {
		return numOfMethodInvocation;
	}

	public void setNumOfMethodInvocation(int numOfMethodInvocation) {
		this.numOfMethodInvocation = numOfMethodInvocation;
	}

	public int getLOC() {
		return LOC;
	}

	public void setLOC(int lOC) {
		LOC = lOC;
	}

	public int getNumOfBranch() {
		return numOfBranch;
	}

	public void setNumOfBranch(int numOfBranch) {
		this.numOfBranch = numOfBranch;
	}

	public int getNumOfLoops() {
		return numOfLoops;
	}

	public void setNumOfLoops(int numOfLoops) {
		this.numOfLoops = numOfLoops;
	}

	public int getNumOfAssignment() {
		return numOfAssignment;
	}

	public void setNumOfAssignment(int numOfAssignment) {
		this.numOfAssignment = numOfAssignment;
	}

	public int getNumOfComments() {
		return numOfComments;
	}

	public void setNumOfComments(int numOfComments) {
		this.numOfComments = numOfComments;
	}

	public int getNumOfArithmaticOperators() {
		return numOfArithmaticOperators;
	}

	public void setNumOfArithmaticOperators(int numOfArithmaticOperators) {
		this.numOfArithmaticOperators = numOfArithmaticOperators;
	}

	public int getNumOfBlankLines() {
		return numOfBlankLines;
	}

	public void setNumOfBlankLines(int numOfBlankLines) {
		this.numOfBlankLines = numOfBlankLines;
	}

	public int getNumOfStringLiteral() {
		return numOfStringLiteral;
	}

	public void setNumOfStringLiteral(int numOfStringLiteral) {
		this.numOfStringLiteral = numOfStringLiteral;
	}

	public int getNumOfLogicalOperators() {
		return numOfLogicalOperators;
	}

	public void setNumOfLogicalOperators(int numOfLogicalOperators) {
		this.numOfLogicalOperators = numOfLogicalOperators;
	}

	public int getNumOfBitOperators() {
		return numOfBitOperators;
	}

	public void setNumOfBitOperators(int numOfBitOperators) {
		this.numOfBitOperators = numOfBitOperators;
	}

	public int getMaxNestedControl() {
		return maxNestedControl;
	}

	public void setMaxNestedControl(int newValue) {
		if (this.maxNestedControl < newValue)
			this.maxNestedControl = newValue;
	}

	public double getAverageOfVariableNameLength() {
		return averageOfVariableNameLength;
	}

	public void setAverageOfVariableNameLength(double averageOfVariableNameLength) {
		this.averageOfVariableNameLength = averageOfVariableNameLength;
	}

	public List<String> getAllLines() {
		return allLines;
	}

	public void setAllLines(List<String> allLines) {
		this.allLines = allLines;
	}

	public double getAverageLineLength() {
		return averageLineLength;
	}

	public void setAverageLineLength(double averageLineLength) {
		this.averageLineLength = averageLineLength;
	}

	public double getProgramVolume() {
		return programVolume;
	}

	public void setProgramVolume(double programVolume) {
		this.programVolume = programVolume;
	}

	public double getEntropy() {
		return entropy;
	}

	public void setEntropy(double entropy) {
		this.entropy = entropy;
	}

	public List<String> getOperands() {
		return operands;
	}

	public void addOperands(String operand) {
		this.operands.add(operand);
	}

	public List<String> getOperators() {
		return operators;
	}

	public void addOperators(String operator) {
		this.operators.add(operator);
	}

	public List<String> getAllWords() {
		return allWords;
	}

	public void addWords(String word) {
		this.allWords.add(word);
	}

	public Map<String, Integer> getTermFrequency() {
		return termFrequencyMatrix;
	}

	public void setTermFrequency(Map<String, Integer> termFrequency) {
		this.termFrequencyMatrix = termFrequency;
	}

	public List<String> getVariableNames() {
		return variableNames;
	}

	public List<String> getSeparatedStrings() {
		return seperatedMethods;
	}

	public void setSeparatedStrings(List<String> separatedStrings) {
		this.seperatedMethods = separatedStrings;
	}

	public List<Map<String, Integer>> getTermFrequencyMatrics() {
		return termFrequencyMatrics;
	}
	
	public double getPatternRate() {
		return patternRate;
	}

	public void setPatternRate(double patternRate) {
		this.patternRate = patternRate;
	}

	public void increaseNumOfMethodInvocation() {
		numOfMethodInvocation++;
	}

	public void increaseNumOfBranch() {
		numOfBranch++;
	}

	public void increaseNumOfLoops() {
		numOfLoops++;
	}

	public void increaseNumOfAssignment() {
		numOfAssignment++;
	}

	public void increaseNumOfCommnents() {
		numOfComments++;
	}

	public void increaseNumOfBlankLines() {
		numOfBlankLines++;
	}

	public void increaseNumOfArithmaticOperators() {
		numOfArithmaticOperators++;
	}

	public void increaseNumOfStringLiteral() {
		numOfStringLiteral++;
	}

	public void increaseNumOfLogicalOperators() {
		numOfLogicalOperators++;
	}

	public void increaseNumOfBitOperators() {
		numOfBitOperators++;
	}

	public void calcAverageOfVariableNames() {
		double sum = 0.0;
		for (String variableName : allWords) {
			sum += (double) variableName.length();
		}

		averageOfVariableNameLength = sum / (double) allWords.size();
	}

	public void calcAverageLineLength() {
		double sum = 0.0;
		for (String line : allLines) {
			sum += line.length();
		}
		averageLineLength = sum / (double) LOC;
	}

	public void calculateProgramVolume() {
		int numOfTotalOperands = operands.size();
		int numOfTotalOperators = operators.size();

		if (numOfTotalOperands == 0 && numOfTotalOperators == 0) {
			programVolume = 0.0;
			return;
		}

		uniqueOperands = searchUnique(operands);
		uniqueOperators = searchUnique(operators);

		int numOfUniqueOperands = uniqueOperands.size();
		int numOfUniqueOperators = uniqueOperators.size();

		int programLength = numOfTotalOperands + numOfTotalOperators;
		int programVocabulary = numOfUniqueOperands + numOfUniqueOperators;

		double logBase10OfProgramVocabulary = Math.log10((double) programVocabulary);
		double logBase10Of2 = Math.log10(2.0);

		programVolume = programLength * (logBase10OfProgramVocabulary / logBase10Of2);
	}

	private List searchUnique(List allList) {
		List uniqueList = new ArrayList();

		Iterator allIterator = allList.iterator();
		while (allIterator.hasNext()) {
			Object element = allIterator.next();
			if (!uniqueList.contains(element))
				uniqueList.add(element);
		}

		return uniqueList;
	}

	public void calculateEntropy() {
		List<Double> probabilities = new ArrayList<Double>();

		termFrequencyMatrix = countFrequency(allWords);

		int sumOfFrequency = 0;
		Iterator termFrequencyIterator = termFrequencyMatrix.keySet().iterator();
		while (termFrequencyIterator.hasNext())
			sumOfFrequency += termFrequencyMatrix.get(termFrequencyIterator.next());

		termFrequencyIterator = termFrequencyMatrix.keySet().iterator();
		while (termFrequencyIterator.hasNext()) {
			double termFrequency = (double) termFrequencyMatrix.get(termFrequencyIterator.next());
			double probability = termFrequency / ((double) sumOfFrequency);
			probabilities.add(probability);
		}

		for (Double probability : probabilities) {
			double logBase10OfProbability = Math.log10(probability);
			double logBase10Of2 = Math.log10(2.0);

			double logBase2OfProbability = logBase10OfProbability / logBase10Of2;

			entropy += probability * logBase2OfProbability;
		}

		entropy = -entropy;
	}

	private Map countFrequency(List<String> words) {
		Iterator<String> wordsIterator = words.iterator();
		Map<String, Integer> frequencyMatrix = new HashMap<String, Integer>();

		while (wordsIterator.hasNext()) {
			String word = wordsIterator.next();
			if (!frequencyMatrix.containsKey(word))
				frequencyMatrix.put(word, 1);
			else {
				int frequency = frequencyMatrix.get(word);
				frequencyMatrix.put(word, ++frequency);
			}
		}
		return frequencyMatrix;
	}

	public void buildTermFrequencyMatrics() {
		IdentifierParser parser = new IdentifierParser();
		List<String> allIdentifiers = new ArrayList<String>();
		List<List<String>> seperatedIdentifiers = new ArrayList<List<String>>();
		List<Integer> lineLenghts = new ArrayList<Integer>();
		
		removeClassAndMethodDeclaration();

		for (String seperatedMethod : seperatedMethods) {
			String[] splitted = seperatedMethod.split("\n");
			List<String> identifiers = new ArrayList<String>();
			
			for(String splittedMethod : splitted){
				splittedMethod = removeSpace(splittedMethod);
				if(splittedMethod.equals("")){
					continue;
				}
				identifiers.addAll(parser.parseIdentifier(splittedMethod));
			}
			seperatedIdentifiers.add(identifiers);
			lineLenghts.add(splitted.length);
		}
		
		for(List<String> identifiers : seperatedIdentifiers){
			allIdentifiers.addAll(identifiers);
		}
		
		for(int i=0; i<seperatedIdentifiers.size(); i++){
			List<String> identifiers = seperatedIdentifiers.get(i);
			Map<String, Integer> frequencyMap = makeBasicMap(allIdentifiers);
			termFrequencyMatrics.add(countFrequency(identifiers, frequencyMap, lineLenghts.get(i)));
		}
	}
	
	private void removeClassAndMethodDeclaration(){
		String first = seperatedMethods.get(0);
		
		String[] splitted = first.split("\\{");
		StringBuilder result = new StringBuilder();
		
		for(int i=2; i<splitted.length; i++)
		{
			result.append(splitted[i]);
		}
		int index = seperatedMethods.indexOf(first);
		seperatedMethods.remove(index);
		seperatedMethods.add(index, result.toString());

		String last = seperatedMethods.get(seperatedMethods.size()-1);
		splitted = last.split("\\}");
		result = new StringBuilder();
		
		for(int i=0; i<splitted.length-2; i++)
		{
			result.append(splitted[i]);
		}
		index = seperatedMethods.indexOf(last);
		seperatedMethods.remove(index);
		seperatedMethods.add(index, result.toString());
	}
	
	private String removeSpace(String string){
		String result = string.replaceAll("[\\t\\r]", "");
		return result;
	}

	private Map makeBasicMap(List<String> allIdentifiers) {
		Iterator<String> wordsIterator = allIdentifiers.iterator();
		Map<String, Integer> basicMatrix = new HashMap<String, Integer>();

		while (wordsIterator.hasNext()) {
			String word = wordsIterator.next();
			if (!basicMatrix.containsKey(word))
				basicMatrix.put(word, 0);
		}
		return basicMatrix;
	}
	
	private Map countFrequency(List<String> words, Map<String, Integer> frequencyMap, int lineNum) {
		Iterator<String> wordsIterator = words.iterator();

		while (wordsIterator.hasNext()) {
			String word = wordsIterator.next();
			if (frequencyMap.containsKey(word)) {
				int frequency = frequencyMap.get(word);
				frequencyMap.put(word, ++frequency);
			}
		}
		frequencyMap.put("line", lineNum);
		return frequencyMap;
	}

	public void calculatePatternRate(int sameLineNum) {
		patternRate = ((double) sameLineNum) / ((double) LOC);
	}
}
