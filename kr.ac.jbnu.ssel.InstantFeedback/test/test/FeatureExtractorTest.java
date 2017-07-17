package test;

import java.io.File;
import java.io.FileReader;

import org.junit.Test;

import junit.framework.Assert;
import kr.ac.jbnu.ssel.instantfeedback.domain.Features;
import kr.ac.jbnu.ssel.instantfeedback.domain.Readability;
import kr.ac.jbnu.ssel.instantfeedback.tool.FeatureExtractor;

public class FeatureExtractorTest {
	
	@SuppressWarnings("deprecation")
	@Test
	public void featureExtractionTest1() throws Exception{
		FeatureExtractor extractor = new FeatureExtractor();
		String filePath = "testsource\\FeatureExtractorTestSource1.java";
		String testSource = loadTestSource(filePath);
		
		Features features = extractor.extractFeatures(testSource);
		Readability readability = new Readability();
		readability.setFeatures(features);
		
		System.out.println(readability.toString());
		
		double readabilityScore = readability.getReadability();
		Assert.assertEquals(234243.33, readabilityScore,0);
	}
	
	@SuppressWarnings("deprecation")
	@Test
	public void featureExtractionTest2() throws Exception{
		FeatureExtractor extractor = new FeatureExtractor();
		String filePath = "testsource\\FeatureExtractorTestSource2.java";
		String testSource = loadTestSource(filePath);
		
		Features features = extractor.extractFeatures(testSource);
		Readability readability = new Readability();
		readability.setFeatures(features);
		
		System.out.println(readability.toString());
		
		double readabilityScore = readability.getReadability();
		Assert.assertEquals(234243.33, readabilityScore,0);
	}
	
	private String loadTestSource(String path) throws Exception
	{
		File file = new File(path);
		char[] buffer = new char[(int)file.length()];
		
		FileReader reader = new FileReader(path);
		reader.read(buffer);
		return new String(buffer);
	}
	
	
//	@Test
//	public void testSomething()
//	{
//		Assert.assertEquals(true, true);
//	}
//	
//	@Test	
//	public void testPerformance()
//	{
//		long startTime = System.currentTimeMillis();
//		doSometing();
//		long endTime = System.currentTimeMillis();
//		long elapsedTime = endTime - startTime;
//		
//		System.out.println("time consumed: "+ (elapsedTime));
//		Assert.assertTrue(elapsedTime > 1000);
//	}
//
//
//	private void doSometing() throws ClassNotFoundException, InstantiationException, IllegalAccessException, NoSuchMethodException, SecurityException
//    {
//		double sum = 0;
//		for(int i = 0 ; i < 10 ; i++)
//		{
//			sum += i;
//			try
//            {
//	            Thread.sleep(100);
//            } catch (InterruptedException e)
//            {
//	            // TODO Auto-generated catch block
//	            e.printStackTrace();
//            }
//		}
//    }

}
