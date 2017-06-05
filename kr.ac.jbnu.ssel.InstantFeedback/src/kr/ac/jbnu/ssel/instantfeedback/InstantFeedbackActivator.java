package kr.ac.jbnu.ssel.instantfeedback;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CaretEvent;
import org.eclipse.swt.custom.CaretListener;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
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

public class InstantFeedbackActivator extends AbstractUIPlugin {
	// The plug-in ID
	public static final String PLUGIN_ID = "kr.ac.jbnu.ssel.instantfeedback";

	private static Logger logger = Logger.getLogger(InstantFeedbackActivator.class);

	// The shared instance
	private static InstantFeedbackActivator plugin;

	private DBConnector db;
	private String previousMethodCode;
	private boolean isDBStartUp = false;
	private IMethod previouslyShowingMethod;

	/**
	 * The constructor
	 */
	public InstantFeedbackActivator() {
	}

	private boolean checkEnterKey(KeyEvent e) {
		if (e.keyCode == SWT.CR || e.keyCode == SWT.KEYPAD_CR)
			return true;
		return false;
	}

	private boolean checkSemicolone(KeyEvent e) {
		if (e.character == ';')
			return true;

		return false;
	}

	// resource change event listener 
	class MyResourceChangeReporter implements IResourceChangeListener {
		public void resourceChanged(IResourceChangeEvent event) {
			if (event.getType() == IResourceChangeEvent.POST_CHANGE) {
				System.out.println("Resources have changed.");
				
				IResource resource = event.getDelta().getResource();
				String resFullPath = resource.getFullPath().toString();
				System.out.println("resFullPath: " + resFullPath);
				if (resFullPath.endsWith(".java")) {
					try {
						event.getDelta().accept(new DeltaPrinter());
					} catch (CoreException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	// store readability information whenever saving code
	class DeltaPrinter implements IResourceDeltaVisitor {
		public boolean visit(IResourceDelta delta) {
			IResource res = delta.getResource();
			switch (delta.getKind()) {
			case IResourceDelta.CHANGED:
				if (res.getFullPath().toString().endsWith(".java")) {

					Display.getDefault().asyncExec(new Runnable() {
						public void run() {
							IWorkbench wb = PlatformUI.getWorkbench();
							IWorkbenchWindow win = wb.getActiveWorkbenchWindow();
							IWorkbenchPage page = win.getActivePage();
							ITextEditor editor = (ITextEditor) page.getActiveEditor();
							if (editor != null) {

								initializeDB();
								IJavaElement javaElement = JavaUI.getEditorInputJavaElement(editor.getEditorInput());
								if (javaElement instanceof ICompilationUnit) {
									ITextSelection textSelection = (ITextSelection) editor.getSelectionProvider()
											.getSelection();
									IJavaElement selectedJavaElement = null;
									try {
										selectedJavaElement = ((ICompilationUnit) javaElement)
												.getElementAt(textSelection.getOffset());

										// check if the selected java element is a method
										if (selectedJavaElement != null
												&& selectedJavaElement.getElementType() == IJavaElement.METHOD) {
											String className = selectedJavaElement.getParent().getElementName();
											String currentMethodCode = ((IMethod) selectedJavaElement).getSource()
													+ "}";

											logger.info("Current method code: " + currentMethodCode);
											// check if the method source is changed compared to previous revision.
											if (checkIftheMethodisChanged(currentMethodCode)) {
												FeatureExtractor extractor = new FeatureExtractor();

												logger.info("Feature extraction start");
												// extract features
												Features features = extractor.extractFeatures(currentMethodCode);
												features.setClassName(className);
												logger.info("Extracted features: " + "LOC: " + features.getLOC()
														+ ", numOfMethodInvocation: "
														+ features.getNumOfMethodInvocation() + ", numOfBranch: "
														+ features.getNumOfBranch() + ", numOfLoops: "
														+ features.getNumOfLoops() + ", numOfAssignment: "
														+ features.getNumOfAssignment() + ", numOfComments: "
														+ features.getNumOfComments() + ", ofArithmaticOperators: "
														+ features.getNumOfArithmaticOperators() + ", numOfBlankLines: "
														+ features.getNumOfBlankLines() + ", numOfStringLiteral: "
														+ features.getNumOfStringLiteral() + ", numOfLogicalOperators: "
														+ features.getNumOfLogicalOperators() + ", numOfBitOperators: "
														+ features.getNumOfBitOperators() + ", maxNestedControl: "
														+ features.getMaxNestedControl() + ", programVolume: "
														+ features.getProgramVolume() + ", entropy: "
														+ features.getEntropy() + ", averageOfVariableNameLength: "
														+ features.getAverageOfVariableNameLength()
														+ ", averageLineLength: " + features.getAverageLineLength()
														+ ", patternRate: " + features.getPatternRate());

												Readability readability = new Readability();
												readability.setFeatures(features);
												readability.setClassName(className);
												readability.calculateReadability();

												User user = db.getCurrentUser();
												readability.setUser(user);

												logger.info("Readability data is saved in db");
												db.storeReadability(readability);

												// store current method code for later comparison.
												previousMethodCode = currentMethodCode;

												// request to invalidate the Gauge view and Timeline view.
												invalidateViews(readability);
											}
										}
									} catch (JavaModelException e) {
										e.printStackTrace();
									}
								}
							}
						}

					});
				}
				break;
			}
			return true;
		}
	}

	class MyShutdownHook extends Thread {
		@Override
		public void run() {
//			DataSender sender = new DataSender();
//			
//			List<User> allUsers = db.getAllUsers();
//			for (User user : allUsers) {
//				sender.saveUserToServer(user);
//			}
//			db.checkSendedUser();
//			
//			List<Readability> allReadabilities = db.getAllReadabilityInfos();
//			for (Readability readability : allReadabilities) {
//				sender.saveReadabilityToServer(readability);
//			}
//			db.checkSenedReadability();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.
	 * BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;

		// add shutdown hook
		Runtime.getRuntime().addShutdownHook(new MyShutdownHook());

		// The following is for capturing save/refactoring events
		IResourceChangeListener listener = new MyResourceChangeReporter();
		ResourcesPlugin.getWorkspace().addResourceChangeListener(listener, IResourceChangeEvent.POST_CHANGE);

		ITextEditor editor = getCurrentEditor();
		if (editor != null) {
			((StyledText) editor.getAdapter(org.eclipse.swt.widgets.Control.class)).addKeyListener(new KeyListener() {
				@Override
				public void keyReleased(KeyEvent e) {
					if (checkEnterKey(e)
//							|| checkSemicolone(e)
							){
						logger.info("View update start");
						Display.getDefault().asyncExec(new Runnable() {
							public void run() {
								initializeDB();
								IJavaElement javaElement = JavaUI.getEditorInputJavaElement(editor.getEditorInput());
								if (javaElement instanceof ICompilationUnit) {
									ITextSelection textSelection = (ITextSelection) editor.getSelectionProvider()
											.getSelection();
									IJavaElement selectedJavaElement = null;
									try {
										selectedJavaElement = ((ICompilationUnit) javaElement)
												.getElementAt(textSelection.getOffset());

										// check if the selected java element is a method
										if (selectedJavaElement != null
												&& selectedJavaElement.getElementType() == IJavaElement.METHOD) {
											String className = selectedJavaElement.getParent().getElementName();
											String packageName = selectedJavaElement.getParent().getParent().getParent().getElementName();
											String methodFullString = selectedJavaElement.toString();
											String methodSignature = methodFullString.split(" \\[")[0];
											
											String currentMethodCode = ((IMethod) selectedJavaElement).getSource()
													+ "}";

											logger.info("Current method code: " + currentMethodCode);
											// check if the method source is changed compared to previous revision.
											if (checkIftheMethodisChanged(currentMethodCode)) {
												FeatureExtractor extractor = new FeatureExtractor();

												logger.info("Feature extraction start");
												// extract features
												Features features = extractor.extractFeatures(currentMethodCode);
												features.setClassName(className);
												logger.info("Extracted features: " + "LOC: " + features.getLOC()
														+ ", numOfMethodInvocation: "
														+ features.getNumOfMethodInvocation() + ", numOfBranch: "
														+ features.getNumOfBranch() + ", numOfLoops: "
														+ features.getNumOfLoops() + ", numOfAssignment: "
														+ features.getNumOfAssignment() + ", numOfComments: "
														+ features.getNumOfComments() + ", ofArithmaticOperators: "
														+ features.getNumOfArithmaticOperators() + ", numOfBlankLines: "
														+ features.getNumOfBlankLines() + ", numOfStringLiteral: "
														+ features.getNumOfStringLiteral() + ", numOfLogicalOperators: "
														+ features.getNumOfLogicalOperators() + ", numOfBitOperators: "
														+ features.getNumOfBitOperators() + ", maxNestedControl: "
														+ features.getMaxNestedControl() + ", programVolume: "
														+ features.getProgramVolume() + ", entropy: "
														+ features.getEntropy() + ", averageOfVariableNameLength: "
														+ features.getAverageOfVariableNameLength()
														+ ", averageLineLength: " + features.getAverageLineLength()
														+ ", patternRate: " + features.getPatternRate());

												Readability readability = new Readability();
												readability.setFeatures(features);
												readability.setClassName(className);
												readability.calculateReadability();
												readability.setMethodSignature(methodSignature);
												readability.setPackageName(packageName);

												User user = db.getCurrentUser();
												readability.setUser(user);

												logger.info("Readability data is saved in db");
												db.storeReadability(readability);

												// store current method code for later comparison.
												previousMethodCode = currentMethodCode;

												// request to invalidate the Gauge view and Timeline view.
												invalidateViews(readability);
											}
										}
									} catch (JavaModelException e) {
										e.printStackTrace();
									}
								}
							}

						});
					}
				}

				@Override
				public void keyPressed(KeyEvent arg0) {
				}
			});
		
			((StyledText)editor.getAdapter(org.eclipse.swt.widgets.Control.class)).addMouseListener(new MouseListener()
			{
				
				@Override
				public void mouseUp(MouseEvent arg0){}
				{
				}
				
				@Override
				public void mouseDown(MouseEvent arg0)
				{
					showGaugeNTimelineViewOfCurrentMethod();
				}
				
				@Override
				public void mouseDoubleClick(MouseEvent arg0){}
			});
			
			((StyledText)editor.getAdapter(org.eclipse.swt.widgets.Control.class)).addCaretListener(new CaretListener()
			{
				@Override
				public void caretMoved(CaretEvent arg0)
				{
					showGaugeNTimelineViewOfCurrentMethod();
				}
			});
		}
	}
	
	private void showGaugeNTimelineViewOfCurrentMethod()
	{
		initializeDB();
		IMethod currentMethod = null;
		try
		{
			ITextEditor editor = getCurrentEditor();
			if(editor != null) {
				
				IJavaElement elem = JavaUI.getEditorInputJavaElement(editor.getEditorInput());
				if (elem instanceof ICompilationUnit) {
					ITextSelection sel = (ITextSelection) editor.getSelectionProvider().getSelection();
					IJavaElement selected = ((ICompilationUnit) elem).getElementAt(sel.getOffset());
					if (selected != null && selected.getElementType() == IJavaElement.METHOD) {
						currentMethod = (IMethod) selected;
						
						if(previouslyShowingMethod != null)
						{
							if( currentMethod != previouslyShowingMethod)
							{
								String methodName = currentMethod.getElementName();
								String className = selected.getParent().getElementName();
								String packageName = selected.getParent().getParent().getParent().getElementName();
								String methodFullString = selected.toString();
								String methodSignature = methodFullString.split(" \\[")[0];
								
								User user = db.getCurrentUser();
								Readability readability = new Readability();
								
								readability.setMethodName(methodName);
								readability.setClassName(className);
								readability.setPackageName(packageName);
								readability.setMethodSignature(methodSignature);
								readability.setUser(user);
								showViews(readability);
							}
						}
						
						previouslyShowingMethod = currentMethod; 
					}
				}
			}
		} catch (JavaModelException e)
		{
			e.printStackTrace();
		}
	}

	private void invalidateViews(Readability readability) {
		try {
			logger.info("Invalidating GaugeView");
			GaugeView gaugeView = (GaugeView) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
					.showView(GaugeView.ID);
			gaugeView.setDBConnector(db);
			gaugeView.invalidate(readability);

			logger.info("Invalidating TimelineView");
			TimelineView timelineView = (TimelineView) PlatformUI.getWorkbench().getActiveWorkbenchWindow()
					.getActivePage().showView(TimelineView.ID);
			timelineView.setDBConnector(db);
			timelineView.invalidate(readability);

		} catch (PartInitException e) {
			e.printStackTrace();
		}
	}
	
	private void showViews(Readability readability) {
		try {
			logger.info("Showing GaugeView");
			GaugeView gaugeView = (GaugeView) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
					.showView(GaugeView.ID);
			gaugeView.setDBConnector(db);
			gaugeView.showGauge(readability);

			logger.info("Showing TimelineView");
			TimelineView timelineView = (TimelineView) PlatformUI.getWorkbench().getActiveWorkbenchWindow()
					.getActivePage().showView(TimelineView.ID);
			timelineView.setDBConnector(db);
			timelineView.invalidate(readability);

		} catch (PartInitException e) {
			e.printStackTrace();
		}
	}

	private void initializeDB() {
		if (!isDBStartUp) {
			logger.info("DB setup start");
			db = DBConnector.getInstance();
			db.DBSetup();
			isDBStartUp = true;
		}
	}

	private boolean checkIftheMethodisChanged(String newMethod) {
		if (previousMethodCode == null)
			return true;

		if (previousMethodCode.equals(newMethod))
			return false;

		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.
	 * BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static InstantFeedbackActivator getDefault() {
		return plugin;
	}

	/**
	 * Returns an image descriptor for the image file at the given plug-in
	 * relative path
	 *
	 * @param path
	 *            the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}
	
	private ITextEditor getCurrentEditor() {
		IWorkbench wb = PlatformUI.getWorkbench();
		IWorkbenchWindow win = wb.getActiveWorkbenchWindow();
		IWorkbenchPage page = win.getActivePage();
		ITextEditor editor = (ITextEditor) page.getActiveEditor();
		return editor;
	}
}
