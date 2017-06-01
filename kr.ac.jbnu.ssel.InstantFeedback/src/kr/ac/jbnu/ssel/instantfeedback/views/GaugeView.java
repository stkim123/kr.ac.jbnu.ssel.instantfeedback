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

public class GaugeView extends ViewPart
{
	public static final String ID = "kr.ac.jbnu.ssel.instantfeedback.views.GaugeView";
	
	private static Logger logger = Logger.getLogger(GaugeView.class);
	
	// private String serverUrl = "http://210.117.128.248:1005/readability";
//	private String serverUrl = "http://175.249.158.207:8080/readability";
	// private String serverUrl = "http://localhost:8080/readability";

	private MeterFigure readabilityGauge;
	private Label methodLabel;
	private ArrowImageCanvas swtImgCanvas; 

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
		methodLabel.setText("<< methodName >>");
		
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
		swtImgCanvas= new ArrowImageCanvas(sashForm);
		swtImgCanvas.setArrowNText(ArrowImageCanvas.UP, "+0.3");
		
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
				methodLabel.setText("<< " + readability.getMethodName() + " >>");
			}
		});
		
		double gap = readability.getReadability() - previousReadability;
		int direction = ArrowImageCanvas.UP;
		String sign = "";
		
		if( gap >= 0)
		{
			direction = ArrowImageCanvas.UP;
			sign = "+";
		}
		else
		{
			direction = ArrowImageCanvas.DOWN;
		}
		
		swtImgCanvas.setArrowNText(direction, sign + " " +  String.format("%.2f", gap));
		previousReadability = readability.getReadability();
		logger.info("Invalidating GaugeView is completed");
	}

	public class ReadabilityScore extends Canvas {
		private MeterFigure readabilityGauge;
		private Label methodLabel;

		public ReadabilityScore(Composite parent, int style) {
			super(parent, style);

			FillLayout layout = new FillLayout();
			layout.type = SWT.VERTICAL;
			parent.setLayout(layout);
			
			final Canvas canvas = new Canvas(parent, SWT.NONE);
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
			
			GridData gridData = new GridData();
			gridData.horizontalAlignment = GridData.FILL;
			gridData.grabExcessHorizontalSpace = true;
			setLayoutData(gridData);

			methodLabel = new Label(parent, SWT.CENTER);
			FontDescriptor boldDescriptor = FontDescriptor.createFrom(methodLabel.getFont()).setStyle(SWT.BOLD);
			Font boldFont = boldDescriptor.createFont(methodLabel.getDisplay());
			methodLabel.setFont(boldFont);
			methodLabel.setText("methodName");

			methodLabel.setLayoutData(new GridData(SWT.CENTER, SWT.FILL, true, true));
			//////////////////////////////////////////////////////////////////////////////////
			
//			SWTImageCanvas swtImgCanvas= new SWTImageCanvas(parent);
//			swtImgCanvas.startThreadForTest();
//			swtImgCanvas.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		}

		public void setValue(Readability readability) {
			this.getDisplay().asyncExec(new Runnable() {
				@Override
				public void run() {
					readabilityGauge.setValue(readability.getReadability());
					methodLabel.setText(readability.getMethodName());
				}
			});
		}
	}
}
