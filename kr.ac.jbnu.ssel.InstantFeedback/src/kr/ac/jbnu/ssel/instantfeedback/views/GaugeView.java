package kr.ac.jbnu.ssel.instantfeedback.views;

import org.apache.log4j.Logger;
import org.eclipse.draw2d.LightweightSystem;
import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.nebula.visualization.widgets.figures.MeterFigure;
import org.eclipse.nebula.visualization.xygraph.util.XYGraphMediaFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.part.ViewPart;

import kr.ac.jbnu.ssel.instantfeedback.domain.Readability;
import kr.ac.jbnu.ssel.instantfeedback.tool.db.DBConnector;

public class GaugeView extends ViewPart
{
	public static final String ID = "kr.ac.jbnu.ssel.instantfeedback.views.GaugeView";
	
	private static Logger logger = Logger.getLogger(GaugeView.class);
	
	private MeterFigure readabilityGauge;
	private Label methodLabel;
	private ArrowImageCanvas swtImgCanvas; 
	private DBConnector db;

	@Override
	public void createPartControl(Composite parent) {
		
	    FillLayout fillLayout = new FillLayout(SWT.VERTICAL);
	    fillLayout.marginHeight = 1;
	    fillLayout.marginWidth = 5;
	    fillLayout.spacing = 1;
	    parent.setLayout(fillLayout);
	    
		SashForm sashForm = new SashForm(parent, SWT.VERTICAL);

		//////////////////////////////////////////////////////////////////////////
		// put empty label for margin
//		methodLabel = new Label(sashForm, SWT.LEFT);
//		methodLabel.setText("");
		
		//////////////////////////////////////////////////////////////////////////
		methodLabel = new Label(sashForm, SWT.LEFT);
		FontDescriptor boldDescriptor = FontDescriptor.createFrom(methodLabel.getFont()).setStyle(SWT.BOLD);
		Font boldFont = boldDescriptor.createFont(methodLabel.getDisplay());
		methodLabel.setFont(boldFont);
		methodLabel.setText("Readability Gague << methodName >>");
		
		//////////////////////////////////////////////////////////////////////////
		final Canvas canvas = new Canvas(sashForm, SWT.NONE);
		LightweightSystem lws = new LightweightSystem(canvas);
		readabilityGauge = new MeterFigure();
		readabilityGauge.setBackgroundColor(XYGraphMediaFactory.getInstance().getColor(255, 255, 255));
		readabilityGauge.setValueLabelVisibility(true);
		readabilityGauge.setRange(0, 10);
		readabilityGauge.setLoLevel(3.5);
		readabilityGauge.setLoloLevel(2);
		readabilityGauge.setHiLevel(8.5);
		readabilityGauge.setHihiLevel(7);
		readabilityGauge.setMajorTickMarkStepHint(8);
		lws.setContents(readabilityGauge);
		
		//////////////////////////////////////////////////////////////////////
		swtImgCanvas= new ArrowImageCanvas(sashForm,readabilityGauge);
		swtImgCanvas.setArrowNText(0.01);
		
		sashForm.setWeights(new int[] {1, 4, 2});
	}

	@Override
	public void setFocus() {

	}
	
	double previousReadability = 0;
	
	public void invalidate(Readability readability)
	{
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				readabilityGauge.setValue(readability.getReadability());
				methodLabel.setText("Readability Gague << " + readability.getMethodName() + " >>");
			}
		});
		
		if(previousReadability == 0)
		{
			Readability prevReadability = db.getLastReadability(readability);
			if(prevReadability != null)
				previousReadability = prevReadability.getReadability(); 
		}
		
		double gap = readability.getReadability() - previousReadability;
		
		swtImgCanvas.setArrowNText(gap);
		
		logger.info("Invalidating GaugeView is completed");	

	}
	
	public void showGauge(Readability readability)
	{
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				Readability lastReadability = db.getLastReadability(readability);
				if(lastReadability == null) {
					readabilityGauge.setValue(0.0);
				}else{
					readabilityGauge.setValue(lastReadability.getReadability());
				}
				methodLabel.setText("Readability Gague << " + readability.getMethodName() + " >>");
			}
		});
		
		if(previousReadability == 0)
		{
			Readability prevReadability = db.getLastReadability(readability);
			if(prevReadability != null)
				previousReadability = prevReadability.getReadability(); 
		}
		
		double gap = 0.0;
		swtImgCanvas.setArrowNText(gap);
		previousReadability = readability.getReadability();
		logger.info("Showing GaugeView is completed");
	}

	public void setDBConnector(DBConnector db) {
		this.db = db;
	}
}
