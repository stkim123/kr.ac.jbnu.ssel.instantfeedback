package kr.ac.jbnu.ssel.instantfeedback.views;

import org.eclipse.draw2d.LightweightSystem;
import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.nebula.visualization.widgets.figures.MeterFigure;
import org.eclipse.nebula.visualization.xygraph.util.XYGraphMediaFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.part.ViewPart;

import kr.ac.jbnu.ssel.instantfeedback.domain.Readability;

public class GaugeView extends ViewPart
{
	public static final String ID = "kr.ac.jbnu.ssel.instantfeedback.views.GaugeView";
	
	// private String serverUrl = "http://210.117.128.248:1005/readability";
//	private String serverUrl = "http://175.249.158.207:8080/readability";
	// private String serverUrl = "http://localhost:8080/readability";

	private ReadabilityScore methodReadability;
	private Readability readabilityInfo;

	@Override
	public void createPartControl(Composite parent) {
		// GridLayout layout = new GridLayout(2, false);
		GridLayout layout = new GridLayout(1, true);
		parent.setLayout(layout);
		parent.setLayoutData(new GridData(GridData.FILL));

		methodReadability = new ReadabilityScore(parent, SWT.NONE);
		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.horizontalSpan = 2;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		methodReadability.setLayoutData(gridData);

	}

	@Override
	public void setFocus() {

	}
	
	public void invalidate(Readability readability)
	{
		methodReadability.setValue(readability);
	}

	public class ReadabilityScore extends Canvas {
		private MeterFigure readabilityGauge;
		private Label methodLabel;

		public ReadabilityScore(Composite parent, int style) {
			super(parent, style);

			GridLayout layout = new GridLayout(1, true);
			parent.setLayout(layout);
			parent.setLayoutData(new GridData(GridData.FILL));

			LightweightSystem lws = new LightweightSystem(this);
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

			methodLabel = new Label(parent, SWT.CENTER);
			FontDescriptor boldDescriptor = FontDescriptor.createFrom(methodLabel.getFont()).setStyle(SWT.BOLD);
			Font boldFont = boldDescriptor.createFont(methodLabel.getDisplay());
			methodLabel.setFont(boldFont);
			methodLabel.setText("methodName");

			methodLabel.setLayoutData(new GridData(SWT.CENTER, SWT.FILL, true, true));

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
