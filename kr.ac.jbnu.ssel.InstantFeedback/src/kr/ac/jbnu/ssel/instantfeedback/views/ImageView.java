package kr.ac.jbnu.ssel.instantfeedback.views;

import org.eclipse.draw2d.LightweightSystem;
import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.nebula.visualization.widgets.figures.MeterFigure;
import org.eclipse.nebula.visualization.xygraph.util.XYGraphMediaFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.part.ViewPart;

import kr.ac.jbnu.ssel.instantfeedback.domain.Readability;

public class ImageView extends ViewPart
{
	public static final String ID = "kr.ac.jbnu.ssel.instantfeedback.views.ImageView";
	
	private SWTImageCanvas swtImgCanvas;

	@Override
	public void createPartControl(Composite parent) {
		// GridLayout layout = new GridLayout(2, false);
		FillLayout fillLayout = new FillLayout();
		fillLayout.type = SWT.HORIZONTAL;
		parent.setLayout(fillLayout);
		
		swtImgCanvas= new SWTImageCanvas(parent);
		swtImgCanvas.requestToDraw();
	}

	@Override
	public void setFocus() {

	}
	
}
