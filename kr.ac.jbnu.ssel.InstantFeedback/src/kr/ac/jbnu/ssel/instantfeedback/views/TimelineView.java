package kr.ac.jbnu.ssel.instantfeedback.views;

import java.util.List;

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

import kr.ac.jbnu.ssel.instantfeedback.domain.Readability;
import kr.ac.jbnu.ssel.instantfeedback.tool.db.DBConnector;

public class TimelineView extends ViewPart
{
	public static final String ID = "kr.ac.jbnu.ssel.instantfeedback.views.TimelineView";

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
		readabilityGraph.setTitle("Readability");
		readabilityGraph.setFocusTraversable(false);
		readabilityGraph.setShowLegend(false);
		readabilityGraph.getPrimaryXAxis().setShowMajorGrid(false);
		readabilityGraph.getPrimaryYAxis().setShowMajorGrid(false);
	}

	private void setReadabilityData(Readability readability)
	{
		List<Readability> graphData = db.getGraphData(readability);
		readabilityGraph.getPrimaryXAxis().setRange((double) graphData.size(), 1.0);
		readabilityGraph.getPrimaryYAxis().setRange(1.0, findMaximum(graphData));
		
		double[] xValues = new double [graphData.size()];
		double[] yValues = new double [graphData.size()];
		for(int i=0; i<graphData.size(); i++)
		{
			xValues[i] = (double)(i+1);
			yValues[i] = graphData.get(i).getReadability();
		}
		
		CircularBufferDataProvider traceDataProvider = new CircularBufferDataProvider(false);
		traceDataProvider.setBufferSize(100);
		traceDataProvider.setCurrentXDataArray(xValues);
		traceDataProvider.setCurrentYDataArray(yValues);

		Trace currentTrace = new Trace("Readability changes", readabilityGraph.getPrimaryXAxis()
				, readabilityGraph.getPrimaryYAxis(), traceDataProvider);
		
		currentTrace.getYAxis().setVisible(false);
		currentTrace.getXAxis().setTitle("");

		currentTrace.setPointStyle(PointStyle.XCROSS);

		if( previousTrace != null)
		{
			readabilityGraph.removeTrace(previousTrace);
		}
		
		readabilityGraph.addTrace(currentTrace);
		previousTrace = currentTrace;
		
		lws.setContents(readabilityGraph);
		readabilityGraph.repaint();
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
