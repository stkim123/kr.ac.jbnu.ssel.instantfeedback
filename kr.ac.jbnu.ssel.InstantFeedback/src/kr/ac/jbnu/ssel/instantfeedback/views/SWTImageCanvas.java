/*******************************************************************************
* Copyright (c) 2004 Chengdong Li : cdli@ccs.uky.edu
* All rights reserved. This program and the accompanying materials 
* are made available under the terms of the Common Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/cpl-v10.html
*******************************************************************************/
package kr.ac.jbnu.ssel.instantfeedback.views;

import java.util.Map;

import org.eclipse.jdt.core.ElementChangedEvent;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IElementChangedListener;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.internal.core.JavaElementDelta;
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
public class SWTImageCanvas extends Canvas
{

	private static final String UP_ARROW="UpArrow";
	private static final String EXT=".png";
	
	private static final int MAX_IMAGES = 16;
	private static final int IMG_VIEW_DELAY = 50;  
	private static int IMG_CENTER_LEFT_MARGIN = 0;
	
	private Image image = null;
	private Image[] images = new Image[MAX_IMAGES];
	private int imageIndex = 0;
	
	private String overlayText = "+3.5";

	public SWTImageCanvas(final Composite parent)
	{
		this(parent, SWT.NULL);
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
							image = images[imageIndex % MAX_IMAGES];
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
	public SWTImageCanvas(final Composite parent, int style)
	{
		super(parent, style);

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

		loadImages();
	}

	public void loadImages()
	{
		for (int i = 0; i < MAX_IMAGES; i++)
		{
			images[i] = new Image(getDisplay(),
					R.class.getResourceAsStream(UP_ARROW + i + EXT));
		}
		image = images[0];
	}
	

	/**
	 * Dispose the garbage here
	 */
	public void dispose()
	{
		if (image != null && !image.isDisposed())
		{
			image.dispose();
		}

		for (Image imageItem : images)
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
		Rectangle clientArea = getClientArea();
		
		ImageData data = image.getImageData();
		
		int startImgX = (clientArea.width - data.width/2)/2;
		IMG_CENTER_LEFT_MARGIN = startImgX/5; 
		
//		gc.drawImage(image, startX, startY);
		gc.drawImage(image, 0, 0, data.width, data.height, startImgX - IMG_CENTER_LEFT_MARGIN , 0, data.width / 2, data.height / 2);

		Font font = new Font(getDisplay(), "Tahoma", 15, SWT.BOLD);
		gc.setFont(font);
		int startTextY = clientArea.height/2 + clientArea.height/10;
		gc.drawText(overlayText, startImgX + IMG_CENTER_LEFT_MARGIN*2, startTextY, SWT.DRAW_TRANSPARENT);
	}

	public void setOverlayText(String text)
	{
		overlayText = text;
	}
}
