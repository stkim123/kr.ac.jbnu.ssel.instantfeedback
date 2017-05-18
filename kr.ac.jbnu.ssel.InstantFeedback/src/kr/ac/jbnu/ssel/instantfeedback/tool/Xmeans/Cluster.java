package kr.ac.jbnu.ssel.instantfeedback.tool.Xmeans;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.naming.spi.DirStateFactory.Result;

import weka.clusterers.XMeans;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.EuclideanDistance;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;

public class Cluster {
	private Instances instances;
	private List<Map<String, Integer>> dataset;
	private Map<String, List<Instance>> clusterResult;

	public void doXmeans() {
		clusterResult = new HashMap<String, List<Instance>>();

		XMeans xmean = new XMeans();
		xmean.setSeed(10);
		EuclideanDistance dist = new EuclideanDistance();
		dist.setDontNormalize(true);
		xmean.setDistanceF(dist);
//		xmean.setDistanceF(new CosineSimilarityDistance());
		xmean.setMaxNumClusters(100);

		try {
			createDataset();
			xmean.buildClusterer(instances);

			for (int i = 0; i < instances.numInstances(); i++) {
				int index = xmean.clusterInstance(instances.instance(i));
				String key = String.valueOf(index);
				List<Instance> list;

				if (!clusterResult.containsKey(key)){
					list = new ArrayList<Instance>();
					clusterResult.put(key, list);
				}
				else
					list = clusterResult.get(key);

				list.add(instances.instance(i));
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public BufferedReader readDataFile(String filename) {
		BufferedReader inputReader = null;
		try {
			inputReader = new BufferedReader(new FileReader(filename));
		} catch (FileNotFoundException ex) {
			System.err.println("File not found: " + filename);
		}
		return inputReader;
	}

	public static void main(String[] args) {
		Cluster c = new Cluster();
		c.doXmeans();
	}

	private void createDataset(){
		if(dataset.isEmpty())
			return ;
		
		FastVector wekaAttributes = new FastVector(4);
		Map<String, Integer> data = dataset.get(0);
		Iterator<String> attributeNames = data.keySet().iterator();
		while(attributeNames.hasNext())
		{
			Attribute attr = new Attribute(attributeNames.next());
			wekaAttributes.addElement(attr);
		}
		
		instances = new Instances("Code Pattern", wekaAttributes, dataset.size());
		
		for(int i=0; i<dataset.size(); i++)
		{
			data = dataset.get(i);
			Instance instance = new DenseInstance(data.size());
			for(int j=0; j<wekaAttributes.size(); j++){
				Attribute attr = (Attribute) wekaAttributes.get(j);
				instance.setValue(attr, data.get(attr.name()));
			}
			instances.add(instance);
		}
	}
	
	public int getSamePatternLines(){
		int samePatternLines = 0;;
		Iterator<String> iter = clusterResult.keySet().iterator();
		while(iter.hasNext())
		{
			List<Instance> clusterIns = clusterResult.get(iter.next());
			if(clusterIns.size() < 2){
				continue;
			}
			for (Instance instance : clusterIns) {
				for(int i =0; i<instance.numAttributes(); i++)
				{
					Attribute attr = instance.attribute(i);
					if(attr.name().equals("line"))
					{
						samePatternLines += instance.value(attr);
					}
				}
			}
		}
		return samePatternLines;
	}

	public List<Map<String, Integer>> getDataset() {
		return dataset;
	}

	public void setDataset(List<Map<String, Integer>> dataset) {
		this.dataset = dataset;
	}

	public Map<String, List<Instance>> getClusterResult() {
		return clusterResult;
	}

	public void setClusterResult(Map<String, List<Instance>> clusterResult) {
		this.clusterResult = clusterResult;
	}
}
