package kr.ac.jbnu.ssel.instantfeedback;

import java.util.Date;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.texteditor.ITextEditor;
import org.osgi.framework.BundleContext;

import kr.ac.jbnu.ssel.instantfeedback.domain.Features;
import kr.ac.jbnu.ssel.instantfeedback.domain.Readability;
import kr.ac.jbnu.ssel.instantfeedback.domain.User;
import kr.ac.jbnu.ssel.instantfeedback.tool.FeatureExtractor;
import kr.ac.jbnu.ssel.instantfeedback.tool.db.DBConnector;
import kr.ac.jbnu.ssel.instantfeedback.views.GaugeView;
import kr.ac.jbnu.ssel.instantfeedback.views.TimelineView;

public class InstantFeedbackActivator extends AbstractUIPlugin
{
	// The plug-in ID
	public static final String PLUGIN_ID = "kr.ac.jbnu.ssel.instantfeedback";

	// The shared instance
	private static InstantFeedbackActivator plugin;

	private DBConnector db;
	private String previousMethodCode;
	private boolean isDBStartUp = false;

	/**
	 * The constructor
	 */
	public InstantFeedbackActivator()
	{
	}

	private boolean checkSaveKey(KeyEvent e)
	{
		if(((e.stateMask & SWT.CTRL) == SWT.CTRL) && (e.keyCode == 's'))
        {
            return true;
        }
		return false;
	}
	
	private boolean checkEnterKey(KeyEvent e)
	{
		if (e.keyCode == SWT.CR || e.keyCode == SWT.KEYPAD_CR)
			return true;
		return false;
	}

	private boolean checkSemicolone(KeyEvent e)
	{
		if (e.character == ';')
			return true;

		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework. BundleContext)
	 */
	public void start(BundleContext context) throws Exception
	{
		super.start(context);
		plugin = this;

		IWorkbench wb = PlatformUI.getWorkbench();
		IWorkbenchWindow win = wb.getActiveWorkbenchWindow();
		IWorkbenchPage page = win.getActivePage();
		ITextEditor editor = (ITextEditor) page.getActiveEditor();
		if (editor != null)
		{
			((StyledText) editor.getAdapter(org.eclipse.swt.widgets.Control.class)).addKeyListener(new KeyListener()
			{
				@Override
				public void keyReleased(KeyEvent e)
				{
					if (checkEnterKey(e) || checkSemicolone(e) 
//							|| checkSaveKey(e)
							)
					{
						Display.getDefault().asyncExec(new Runnable()
						{
							public void run()
							{
								initializeDB();
								IJavaElement javaElement = JavaUI.getEditorInputJavaElement(editor.getEditorInput());
								if (javaElement instanceof ICompilationUnit)
								{
									ITextSelection textSelection = (ITextSelection) editor.getSelectionProvider().getSelection();
									IJavaElement selectedJavaElement = null;
									try
									{
										selectedJavaElement = ((ICompilationUnit) javaElement)
												.getElementAt(textSelection.getOffset());
										String className = selectedJavaElement.getParent().getElementName();

										// check if the selected java element is a method
										if (selectedJavaElement != null
												&& selectedJavaElement.getElementType() == IJavaElement.METHOD)
										{
											String currentMethodCode = ((IMethod) selectedJavaElement).getSource()+ "}";

											System.out.println("currentMethodCode:" + currentMethodCode);
											///////////////////////////////////////////////////////////////////////////////////////////
											// check if the method source is changed compared to previous revision.
											if (checkIftheMethodisChanged(currentMethodCode))
											{
												FeatureExtractor extractor = new FeatureExtractor();

												// extract features
												Features features = extractor.extractFeatures(currentMethodCode);
												features.setClassName(className);

												Readability readability = new Readability();
												readability.setFeatures(features);
												readability.setClassName(className);
												readability.calculateReadability();

												// TODO: Need to remove later.
												// It is
												// just for testing.
												User user = createSampleUser();
												readability.setUser(user);

												db.storeReadability(readability);

												// store current method code for
												// later comparison.
												previousMethodCode = currentMethodCode;
												
												// request to invalidate the Gauge view and Timeline view.
												invalidateViews(readability);
											}
										}
									} catch (JavaModelException e)
									{
										e.printStackTrace();
									}
								}
							}
							
						});
					}
				}

				@Override
				public void keyPressed(KeyEvent arg0)
				{
				}
			});
		}
	}
	
	private void invalidateViews(Readability readability)
	{
		try
		{
			GaugeView gaugeView = (GaugeView) PlatformUI.getWorkbench().getActiveWorkbenchWindow()
					.getActivePage().showView(GaugeView.ID);
			gaugeView.invalidate(readability);
			
			TimelineView timelineView = (TimelineView) PlatformUI.getWorkbench().getActiveWorkbenchWindow()
					.getActivePage().showView(TimelineView.ID);
			timelineView.setDBConnector(db);
			timelineView.invalidate(readability);
			
		} catch (PartInitException e)
		{
			e.printStackTrace();
		}
		
	}

	private void initializeDB()
	{
		if (!isDBStartUp)
		{
			db = DBConnector.getInstance();
			db.DBSetup();
			isDBStartUp = true;
		}
	}

	/**
	 * Need to remove.  
	 * @return
	 */
	private User createSampleUser()
	{
		User user = new User();
		user.setUsername("test");
		user.setAge(1);
		user.setArea("test");
		user.setJavaExpierence(1);
		user.setExpierence(1);
		user.setCreatedDate(new Date());
		return user;
	}

	private boolean checkIftheMethodisChanged(String newMethod)
	{
		if (previousMethodCode == null)
			return true;

		if (previousMethodCode.equals(newMethod))
			return false;

		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework. BundleContext)
	 */
	public void stop(BundleContext context) throws Exception
	{
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static InstantFeedbackActivator getDefault()
	{
		return plugin;
	}

	/**
	 * Returns an image descriptor for the image file at the given plug-in relative path
	 *
	 * @param path
	 *            the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path)
	{
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}
}
