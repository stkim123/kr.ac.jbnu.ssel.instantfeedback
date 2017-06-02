package kr.ac.jbnu.ssel.instantfeedback.domain;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Readability {
	private int id;
	private int LOC;
	private int numOfComments;
	private int numOfBlankLines;
	private int numOfBitOperators;
	private int maxNestedControl;
	private double patternRate;
	private double programVolume;
	private double entropy;
	private double readability;
	
	private Date storedTime;
	private String methodName;
	private String className;
	private String packageName;
	private String methodSignature;
	private User user;
	
	public Readability() {
		readability = 0.0;
		storedTime = null;
	}
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public double getReadability() {
		return readability;
	}
	
	public Date getStoredTime()
	{
		return this.storedTime;
	}
	
	public User getUser()
	{
		return user;
	}
	
	public void setStoredTime(Date storedTime) {
		this.storedTime = storedTime;
	}

	public void setReadability(double readability) {
		this.readability = readability;
	}
	
	public void setUser(User user)
	{
		this.user = user;
	}

	public String getMethodName() {
		return methodName;
	}

	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}
	
	public String getPackageName() {
		return packageName;
	}

	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}

	public String getMethodSignature() {
		return methodSignature;
	}

	public void setMethodSignature(String signature) {
		this.methodSignature = signature;
	}

	public int getLOC() {
		return LOC;
	}

	public void setLOC(int lOC) {
		LOC = lOC;
	}

	public int getNumOfComments() {
		return numOfComments;
	}

	public void setNumOfComments(int numOfComments) {
		this.numOfComments = numOfComments;
	}

	public int getNumOfBlankLines() {
		return numOfBlankLines;
	}

	public void setNumOfBlankLines(int numOfBlankLines) {
		this.numOfBlankLines = numOfBlankLines;
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

	public void setMaxNestedControl(int maxNestedControl) {
		this.maxNestedControl = maxNestedControl;
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

	public double getPatternRate() {
		return patternRate;
	}

	public void setPatternRate(double patternRate) {
		this.patternRate = patternRate;
	}

	@Override
	public String toString()
	{
		return className + "." + methodName; 
	}
	
	public void setFeatures(Features features){
		setLOC(features.getLOC());
		setNumOfComments(features.getNumOfComments());
		setNumOfBlankLines(features.getNumOfBlankLines());
		setNumOfBitOperators(features.getNumOfBitOperators());
		setMaxNestedControl(features.getMaxNestedControl());
		setProgramVolume(features.getProgramVolume());
		setEntropy(features.getEntropy());
		setPackageName(features.getPackageName());
		setClassName(features.getClassName());
		setMethodName(features.getMethodName());
		setPatternRate(features.getPatternRate());
		
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date currentDate = new Date();
		setStoredTime(currentDate);
	}
	
	public void calculateReadability(){
		readability = 7.422 + (-0.020)*LOC + 0.040*numOfComments + 0.037*numOfBlankLines
				+ (-0.755)*numOfBitOperators + (-0.153)*maxNestedControl + (-0.001)*programVolume
				+ (-0.610)*entropy;
	}
}