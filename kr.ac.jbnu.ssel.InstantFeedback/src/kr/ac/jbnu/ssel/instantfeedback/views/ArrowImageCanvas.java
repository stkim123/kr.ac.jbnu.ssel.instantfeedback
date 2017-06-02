/*******************************************************************************
* Copyright (c) 2004 Chengdong Li : cdli@ccs.uky.edu
* All rights reserved. This program and the accompanying materials 
* are made available under the terms of the Common Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/cpl-v10.html
*******************************************************************************/
package kr.ac.jbnu.ssel.instantfeedback.views;

import org.eclipse.nebula.visualization.widgets.figures.MeterFigure;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;

import kr.ac.jbnu.ssel.instantfeedback.R.R;

/**
 * A scrollable image canvas that extends org.eclipse.swt.graphics.Canvas.
 * <p/>
 * It requires Eclipse (version >= 2.1) on Win32/win32; Linux/gtk;
 * MacOSX/carbon.
 * <p/>
 * This implementation using the pure SWT, no UI AWT package is used. For
 * convenience, I put everything into one class. However, the best way to
 * implement this is to use inheritance to create multiple hierarchies.
 * 
 * @author Chengdong Li: cli4@uky.edu
 */
public class ArrowImageCanvas extends Canvas
{

	private static final String UP_ARROW="UpArrow";
	private static final String DOWN_ARROW="DownArrow";
	
	private static final String EXT=".png";
	
	private static final int MAX_IMAGES = 16;
	private static final int IMG_VIEW_DELAY = 100;
	
	public static final int UP = 100;
	public static final int DOWN = 200;
	
	private static int IMG_CENTER_LEFT_MARGIN = 0;
	
	private Image image = null;
	private Image[] upImages = new Image[MAX_IMAGES];
	private Image[] downImages = new Image[MAX_IMAGES];
	private Image[] currentImages; 
	
	private int imageIndex = 0;
	
	private int direction = UP;
	private String overlayText = "+0.0";

	private MeterFigure readabilityGauge;
	public ArrowImageCanvas(final Composite parent, MeterFigure readabilityGauge)
	{
		this(parent, SWT.NULL);
		this.readabilityGauge = readabilityGauge;
	}

	public void requestToDraw()
	{
		new Thread()
		{
			public void run()
			{
				for (int i = 0; i < MAX_IMAGES; i++)
				{
					imageIndex = i;

					try
					{
						Thread.sleep(IMG_VIEW_DELAY);
					} 
					catch (Throwable th)
					{
					}

					if (getDisplay().isDisposed())
					{
						return;
					}

					getDisplay().asyncExec(new Runnable()
					{
						public void run()
						{
							if (image.isDisposed())
								return;
							image = currentImages[imageIndex % MAX_IMAGES];
							redraw();
						}
					});
				}
			}
		}.start();
	}

	/**
	 * Constructor for ScrollableCanvas.
	 * 
	 * @param parent
	 *            the parent of this control.
	 * @param style
	 *            the style of this control.
	 */
	public ArrowImageCanvas(final Composite parent, int style)
	{
		super(parent, style);

		loadImages();
		
		addControlListener(new ControlListener()
		{

			@Override
			public void controlResized(ControlEvent event)
			{
				redraw();
			}

			@Override
			public void controlMoved(ControlEvent event)
			{
			}
		});

		addPaintListener(new PaintListener()
		{ /* paint listener. */
			public void paintControl(final PaintEvent event)
			{
				paint(event.gc);
			}
		});
	}

	private void loadImages()
	{
		for (int i = 0; i < MAX_IMAGES; i++)
		{
			upImages[i] = new Image(getDisplay(),R.class.getResourceAsStream(UP_ARROW + i + EXT));
			downImages[i] = new Image(getDisplay(),R.class.getResourceAsStream(DOWN_ARROW + i + EXT));
		}
		
		if( direction == UP)
		{
			image = upImages[0];
			currentImages = upImages;
		}
		else
		{
			image = downImages[0];
			currentImages = downImages;
		}
	}
	

	/**
	 * Dispose the garbage here
	 */
	public void dispose()
	{
		for (Image imageItem : upImages)
		{
			if (imageItem != null && !imageItem.isDisposed())
			{
				imageItem.dispose();
			}
		}
		
		for (Image imageItem : downImages)
		{
			if (imageItem != null && !imageItem.isDisposed())
			{
				imageItem.dispose();
			}
		}
	}

	/* Paint function */
	private void paint(GC gc)
	{
		org.eclipse.draw2d.geometry.Rectangle gaugeScale = readabilityGauge.getScale().getBounds();
		ImageData arrowImg = image.getImageData();

		int drawAreaWidth = gaugeScale.getCenter().x;
		int arrowImgWidth = arrowImg.width/2;
		int arrowImgHeight = arrowImg.height/2;
		
//		int startImgX = (drawAreaWidth - data.width/2)/2;
		int startImgX = drawAreaWidth - arrowImgWidth /2;
		IMG_CENTER_LEFT_MARGIN = startImgX/7; 
		
//		gc.drawImage(image, startX, startY);
		gc.drawImage(image, 0, 0, arrowImg.width, arrowImg.height, startImgX - IMG_CENTER_LEFT_MARGIN , 0, arrowImgWidth, arrowImgHeight);

		Font font = new Font(getDisplay(), "Tahoma", 15, SWT.BOLD);
		gc.setFont(font);
//		int startTextY = gaugeScale.getCenter().y + arrowImgHeight/2;
		int startTextY = arrowImgHeight/2;
		
//		System.out.println("gaugeScale.getCenter().y;"+ gaugeScale.getCenter().y + ",arrowImgHeight:"+ arrowImgHeight);
		gc.drawText(overlayText, startImgX + IMG_CENTER_LEFT_MARGIN, startTextY, SWT.DRAW_TRANSPARENT);
	}

	public void setArrowNText(int direction, String text)
	{
		this.direction = direction;
		if( direction == UP)
		{
			currentImages = upImages;
		}
		else
		{
			currentImages = downImages;
		}
		overlayText = text;
		
		requestToDraw();
	}
}
