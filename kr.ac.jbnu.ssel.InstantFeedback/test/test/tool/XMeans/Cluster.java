package test.tool.XMeans;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;

import kr.ac.jbnu.ssel.instantfeedback.tool.Xmeans.CosineSimilarityDistance;
import weka.clusterers.XMeans;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;

public class Cluster {
	public static BufferedReader readDataFile(String filename) {
		BufferedReader inputReader = null;
		try {
			inputReader = new BufferedReader(new FileReader(filename));
		} catch (FileNotFoundException ex) {
			System.err.println("File not found: " + filename);
		}
		return inputReader;
	}

	public static void main(String[] args) throws Exception {
		XMeans xmean = new XMeans();
		xmean.setSeed(10);
		xmean.setDistanceF(new CosineSimilarityDistance());
		xmean.setMaxNumClusters(100);

		BufferedReader datafile = readDataFile("Resources/cpu.arff");
		Instances data = new Instances(datafile);

		xmean.buildClusterer(data);

		for (int i = 0; i < data.numInstances(); i++) {
			Instance instance = data.instance(i);
			Attribute t = instance.attribute(0);
			System.out.println(t.name());
			System.out.println(instance.value(t));
			System.out.printf("Instance %d -> Cluster %d \n", i, xmean.clusterInstance(data.instance(i)));
			System.out.println();
		}
	}
}
