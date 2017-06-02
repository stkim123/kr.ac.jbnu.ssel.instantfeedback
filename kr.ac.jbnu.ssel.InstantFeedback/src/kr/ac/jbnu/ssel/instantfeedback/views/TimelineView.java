package kr.ac.jbnu.ssel.instantfeedback.views;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.draw2d.LightweightSystem;
import org.eclipse.nebula.visualization.xygraph.dataprovider.CircularBufferDataProvider;
import org.eclipse.nebula.visualization.xygraph.figures.Trace;
import org.eclipse.nebula.visualization.xygraph.figures.Trace.PointStyle;
import org.eclipse.nebula.visualization.xygraph.figures.XYGraph;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.part.ViewPart;

import kr.ac.jbnu.ssel.instantfeedback.Constants;
import kr.ac.jbnu.ssel.instantfeedback.domain.Readability;
import kr.ac.jbnu.ssel.instantfeedback.tool.db.DBConnector;

public class TimelineView extends ViewPart
{
	public static final String ID = "kr.ac.jbnu.ssel.instantfeedback.views.TimelineView";

	private static Logger logger = Logger.getLogger(TimelineView.class);
	// private String serverUrl = "http://210.117.128.248:1005/readability";
	// private String serverUrl = "http://localhost:8080/readability";

	private XYGraph readabilityGraph; 
	private DBConnector db;
	private LightweightSystem lws;
	private Trace previousTrace;

	public TimelineView() {
	}
	
	@Override
	public void createPartControl(Composite parent)
	{
		FillLayout fillLayout = new FillLayout();
		fillLayout.type = SWT.HORIZONTAL;
		parent.setLayout(fillLayout);
		
		final Canvas canvas = new Canvas(parent, SWT.NONE);
        lws = new LightweightSystem(canvas);
		
		readabilityGraph = new XYGraph();
		readabilityGraph.setTitle("Readability Timeline");
		readabilityGraph.setFocusTraversable(false);
		readabilityGraph.setShowLegend(false);
		readabilityGraph.getPrimaryXAxis().setShowMajorGrid(false);
		readabilityGraph.getPrimaryYAxis().setShowMajorGrid(false);
	}

	private void setReadabilityData(Readability readability)
	{
		readabilityGraph.setTitle("Readability Timeline - " + readability.getMethodName());
		List<Readability> graphData = db.getGraphData(readability);
		graphData = groupData(graphData);
		graphData = addEmptyData(graphData);
		readabilityGraph.getPrimaryXAxis().setRange(Constants.maxGraphResult, 1.0);
		readabilityGraph.getPrimaryYAxis().setRange(1.0, 10.0);
		
		double[] xValues = new double [graphData.size()];
		double[] yValues = new double [graphData.size()];
		for(int i=0; i<graphData.size(); i++)
		{
			xValues[i] = 15.0 - (double)(i);
			yValues[i] = graphData.get(i).getReadability();
		}
		
		CircularBufferDataProvider traceDataProvider = new CircularBufferDataProvider(false);
		traceDataProvider.setBufferSize(100);
		traceDataProvider.setCurrentXDataArray(xValues);
		traceDataProvider.setCurrentYDataArray(yValues);

		Trace currentTrace = new Trace("Readability changes", readabilityGraph.getPrimaryXAxis()
				, readabilityGraph.getPrimaryYAxis(), traceDataProvider);
		
		currentTrace.getYAxis().setTitle("Readability score");
		currentTrace.getXAxis().setTitle("Measure Period(Save/Enter/Refactoring)");

		currentTrace.setPointStyle(PointStyle.XCROSS);

		if( previousTrace != null)
		{
			readabilityGraph.removeTrace(previousTrace);
		}
		
		readabilityGraph.addTrace(currentTrace);
		previousTrace = currentTrace;
		
		lws.setContents(readabilityGraph);
		readabilityGraph.repaint();
		logger.info("Invalidating TimelinView is completed");
	}

	private List<Readability> groupData(List<Readability> originalData) {
		List<Readability> result = new ArrayList<Readability>();
		int arraySize = originalData.size();
		for(int i=0; i<arraySize; i=i+3){
			double average = 0.0;
			if(i+2 >= originalData.size()){
				double sum = 0.0;
				int count = 0;
				for (int j = i; j < arraySize; j++) {
					sum = sum + originalData.get(j).getReadability();
					count++;
				}
				average = sum / (double) count;
			} else {
				average = originalData.get(i).getReadability() + originalData.get(i+1).getReadability()
						+ originalData.get(i+2).getReadability();  
			}
			Readability readabilityInfo = new Readability();
			readabilityInfo.setReadability(average);
			result.add(readabilityInfo);
		}
		return originalData;
	}
	
	private List<Readability> addEmptyData(List<Readability>graphData){
		if(graphData.size() < Constants.maxGraphResult)
		{
			int gap = Constants.maxGraphResult - graphData.size(); 
			for (int i = 0; i < gap; i++) {
				Readability readabilityInfo = new Readability();
				readabilityInfo.setReadability(0.0);
				graphData.add(0, readabilityInfo);
			}
		}
		
		return graphData;
	}

	@Override
	public void setFocus()
	{

	}

	public void invalidate(Readability readability)
	{
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				setReadabilityData(readability);
			}
		});
	}
	
	private double findMaximum(List<Readability> data){
		double max = 0.0;
		Readability readability;
		for(int i=0; i<data.size(); i++)
		{
			readability =data.get(i);
			double score = readability.getReadability();
			if(max<score)
				max = score;
		}
		return max;
	}
	
	public void setDBConnector(DBConnector db) {
		this.db = db;
	}
}
