package kr.ac.jbnu.ssel.instantfeedback.tool.Xmeans;

import java.util.Enumeration;

import weka.core.DistanceFunction;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.neighboursearch.PerformanceStats;

public class CosineSimilarityDistance implements DistanceFunction {
	private Instances instances;
	
	public CosineSimilarityDistance() {
		
	}

	@Override
	public String[] getOptions() {
		return null;
	}

	@Override
	public Enumeration<Option> listOptions() {
		return null;
	}

	@Override
	public void setOptions(String[] arg0) throws Exception {
		
	}

	@Override
	public void clean() {
		instances = null;
	}

	@Override
	public double distance(Instance ins1, Instance ins2) {
		return distance(ins1, ins2, 0, null);
	}

	@Override
	public double distance(Instance ins1, Instance ins2, PerformanceStats arg2) throws Exception {
		return distance(ins1, ins2, 0, null);
	}

	@Override
	public double distance(Instance ins1, Instance ins2, double arg2) {
		return distance(ins1, ins2, 0, null);
	}

	@Override
	public double distance(Instance ins1, Instance ins2, double arg2, PerformanceStats arg3) {
		int numIns1Values = ins1.numValues();
		int numIns2Values = ins2.numValues();
		double innerProduct = 0;
		double squaredD1 = 0;
		double squaredD2 = 0;
		
		if(numIns1Values != numIns2Values)
			return 0;
		
		for(int i=0; i<numIns1Values; i++)
		{
			double valIns1 = ins1.value(i);
			double valIns2 = ins2.value(i);
			
			innerProduct += valIns1*valIns2;
			squaredD1 += Math.pow(valIns1, 2);
			squaredD2 += Math.pow(valIns2, 2);
		}
		
		double distance = innerProduct/(Math.sqrt(squaredD1)*Math.sqrt(squaredD2));
	
		return distance;
	}

	@Override
	public String getAttributeIndices() {
		return null;
	}

	@Override
	public Instances getInstances() {
		return instances;
	}

	@Override
	public boolean getInvertSelection() {
		return false;
	}

	@Override
	public void postProcessDistances(double[] arg0) {
		
	}

	@Override
	public void setAttributeIndices(String arg0) {
		
	}

	@Override
	public void setInstances(Instances instances) {
		this.instances = instances;
	}

	@Override
	public void setInvertSelection(boolean arg0) {
		
	}

	@Override
	public void update(Instance arg0) {
		
	}
}
