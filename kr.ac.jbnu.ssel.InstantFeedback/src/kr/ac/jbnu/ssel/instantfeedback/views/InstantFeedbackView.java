package kr.ac.jbnu.ssel.instantfeedback.views;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.LightweightSystem;
import org.eclipse.jdt.core.ElementChangedEvent;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IElementChangedListener;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.nebula.visualization.widgets.figures.MeterFigure;
import org.eclipse.nebula.visualization.xygraph.util.XYGraphMediaFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.texteditor.ITextEditor;

import kr.ac.jbnu.ssel.instantfeedback.domain.Features;
import kr.ac.jbnu.ssel.instantfeedback.domain.MatrixGraph;
import kr.ac.jbnu.ssel.instantfeedback.domain.Readability;
import kr.ac.jbnu.ssel.instantfeedback.domain.User;
import kr.ac.jbnu.ssel.instantfeedback.tool.DataSender;
import kr.ac.jbnu.ssel.instantfeedback.tool.FeatureExtractor;
import kr.ac.jbnu.ssel.instantfeedback.tool.db.DBConnector;

public class InstantFeedbackView extends ViewPart
// implements IElementChangedListener
{
	// private String serverUrl = "http://210.117.128.248:1005/readability";
	private String serverUrl = "http://175.249.158.207:8080/readability";
	// private String serverUrl = "http://localhost:8080/readability";

	private ReadabilityScore methodReadability;
	private MatrixGraph readabilityGraph;
	private MethodList methodList;
	private Label userdataLabel;
	// private ElementTable elementTable;
	private Readability readabilityInfo;
	private String previousMethod;
	private FeatureExtractor extractor;

	private DBConnector db;
	private boolean first = true;
	private DataSender sender;

	@Override
	public void createPartControl(Composite parent) {
		sender = new DataSender();
		extractor = new FeatureExtractor();
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

		userdataLabel = new Label(parent, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.horizontalSpan = 2;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		userdataLabel.setLayoutData(gridData);
		userdataLabel.setText("*****please fill in the user information in the preference page.*****");

		methodList = new MethodList(parent, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		// gridData.grabExcessVerticalSpace = true;
		gridData.verticalSpan = 2;
		methodList.setLayoutData(gridData);

		readabilityGraph = new MatrixGraph(parent, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		readabilityGraph.setLayoutData(gridData);

		methodList.setReadabilityGraph(readabilityGraph);

		addListenerToCheckChange_save();
	}

	@Override
	public void setFocus() {

	}

	private boolean checkChange(String newMethod) {
		if (previousMethod == null)
			return true;

		if (previousMethod.equals(newMethod))
			return false;

		return true;
	}

	private void addListenerToCheckChange_save() {
		JavaCore.addElementChangedListener(new IElementChangedListener() {

			@Override
			public void elementChanged(ElementChangedEvent event) {
				Display.getDefault().asyncExec(new Runnable() {

					@Override
					public void run() {
						db = DBConnector.getInstance();
						db.DBSetup();

						IWorkbench wb = PlatformUI.getWorkbench();
						IWorkbenchWindow win = wb.getActiveWorkbenchWindow();
						IWorkbenchPage page = win.getActivePage();
						ITextEditor editor = (ITextEditor) page.getActiveEditor();
						IJavaElement javaElement = JavaUI.getEditorInputJavaElement(editor.getEditorInput());
						if (javaElement instanceof ICompilationUnit) {
							ITextSelection sel = (ITextSelection) editor.getSelectionProvider().getSelection();
							IJavaElement selected = null;
							try {
								selected = ((ICompilationUnit) javaElement).getElementAt(sel.getOffset());
								if (selected != null && selected.getElementType() == IJavaElement.METHOD) {
									String methodSource = ((IMethod) selected).getSource() + "}";
									String sourceAsString = "public class AA{ "
											+ methodSource.substring(0, methodSource.length() - 1) + "}";
									ASTParser parser = ASTParser.newParser(AST.JLS8);
									parser.setSource(sourceAsString.toCharArray());
									parser.setKind(ASTParser.K_COMPILATION_UNIT);
									CompilationUnit cu = (CompilationUnit) parser.createAST(null);

									if (checkChange(methodSource)) {
										readabilityInfo = new Readability();

										Features features = extractor.extractFeatures(sourceAsString);
										features.setClassName(selected.getParent().getElementName());

										readabilityInfo.setFeatures(features);
										readabilityInfo.calculateReadability();

										methodReadability.setValue(readabilityInfo);

										User user = db.getCurrentUser();
										if (user != null && !user.getUsername().equals("default")) {
											visibleSetting(true);
											userdataLabel.setVisible(false);
											readabilityInfo.setUser(user);

											db.saveReadabilityData(readabilityInfo);
											sender.saveReadabilityToServer(readabilityInfo);

											List<Readability> listData = db.getListData(user);
											methodList.updateList(listData);
											methodList.selectCurrentOne(readabilityInfo);

											List<Readability> graphData = db.getGraphData(readabilityInfo);
											readabilityGraph.setData(graphData);
										} else {
											visibleSetting(false);
										}

										previousMethod = methodSource;
									}
								}
							} catch (JavaModelException JME) {
								JME.printStackTrace();
							}
						}
						if (first) {
							addListenerToCheckChange_key(editor);
							methodList.initialize();
							first = false;
						}
					}
				});
			}
		});

	}

	private void addListenerToCheckChange_key(ITextEditor editor) {
		((StyledText) editor.getAdapter(org.eclipse.swt.widgets.Control.class)).addKeyListener(new KeyListener() {

			@Override
			public void keyReleased(KeyEvent e) {
				Display.getDefault().asyncExec(new Runnable() {

					@Override
					public void run() {
						if (checkEnterKey(e) || checkSemicolone(e)) {
							IJavaElement javaElement = JavaUI.getEditorInputJavaElement(editor.getEditorInput());
							if (javaElement instanceof ICompilationUnit) {
								ITextSelection sel = (ITextSelection) editor.getSelectionProvider().getSelection();
								IJavaElement selected = null;
								try {
									selected = ((ICompilationUnit) javaElement).getElementAt(sel.getOffset());
									if (selected != null && selected.getElementType() == IJavaElement.METHOD) {
										String methodSource = ((IMethod) selected).getSource() + "}";
										String sourceAsString = "public class AA{ "
												+ methodSource.substring(0, methodSource.length() - 1) + "}";
										ASTParser parser = ASTParser.newParser(AST.JLS8);
										parser.setSource(sourceAsString.toCharArray());
										parser.setKind(ASTParser.K_COMPILATION_UNIT);
										CompilationUnit cu = (CompilationUnit) parser.createAST(null);

										if (checkChange(methodSource)) {
											readabilityInfo = new Readability();

											Features features = extractor.extractFeatures(sourceAsString);
											features.setClassName(selected.getParent().getElementName());

											readabilityInfo.setFeatures(features);
											readabilityInfo.calculateReadability();

											methodReadability.setValue(readabilityInfo);

											User user = db.getCurrentUser();
											if (user != null && !user.getUsername().equals("default")) {
												visibleSetting(true);
												readabilityInfo.setUser(user);

												db.saveReadabilityData(readabilityInfo);
												sender.saveReadabilityToServer(readabilityInfo);

												List<Readability> listData = db.getListData(user);
												methodList.updateList(listData);
												methodList.selectCurrentOne(readabilityInfo);

												List<Readability> graphData = db.getGraphData(readabilityInfo);
												readabilityGraph.setData(graphData);
											} else {
												visibleSetting(false);
											}

											previousMethod = methodSource;
										}
									}
								} catch (JavaModelException JME) {
									JME.printStackTrace();
								}
							}
						}

					}
				});
			}

			@Override
			public void keyPressed(KeyEvent e) {
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

		});
	}

	private void visibleSetting(boolean userinput) {
		if (userinput) {
			methodList.setVisible(true);
			readabilityGraph.setVisible(true);
			userdataLabel.setVisible(false);
		} else {
			methodList.setVisible(false);
			readabilityGraph.setVisible(false);
			userdataLabel.setVisible(true);
		}
	}

	public class ReadabilityScore extends Canvas {
		private MeterFigure readabilityScore;
		private Label methodLabel;

		public ReadabilityScore(Composite parent, int style) {
			super(parent, style);

			GridLayout layout = new GridLayout(1, true);
			parent.setLayout(layout);
			parent.setLayoutData(new GridData(GridData.FILL));

			LightweightSystem lws = new LightweightSystem(this);
			readabilityScore = new MeterFigure();
			readabilityScore.setBackgroundColor(XYGraphMediaFactory.getInstance().getColor(255, 255, 255));
			readabilityScore.setValueLabelVisibility(true);
			readabilityScore.setRange(0, 10);
			readabilityScore.setLoLevel(3.5);
			readabilityScore.setLoloLevel(2);
			readabilityScore.setHiLevel(8.5);
			readabilityScore.setHihiLevel(7);
			readabilityScore.setMajorTickMarkStepHint(8);

			lws.setContents(readabilityScore);

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
					readabilityScore.setValue(readability.getReadability());
					methodLabel.setText(readability.getMethodName());
				}
			});
		}
	}

	public class MethodList extends Canvas {
		private org.eclipse.swt.widgets.List methodList;
		private List<Readability> readabilityList;
		private MatrixGraph readabilityGraph;

		public MethodList(Composite parent, int style) {
			super(parent, style);
			// methodList = new ListViewer(parent, style);
			methodList = new org.eclipse.swt.widgets.List(parent, SWT.V_SCROLL | SWT.H_SCROLL);
			readabilityList = new ArrayList<Readability>();

			methodList.addSelectionListener(new SelectionListener() {

				@Override
				public void widgetDefaultSelected(SelectionEvent e) {

				}

				@Override
				public void widgetSelected(SelectionEvent e) {
					int selection = methodList.getSelectionIndex();
					Readability selectedReadability = readabilityList.get(selection);
					List<Readability> selectedInfos = db.getGraphData(selectedReadability);
					readabilityGraph.setData(selectedInfos);
				}

			});

			GridData gridData = new GridData();
			gridData.horizontalAlignment = SWT.FILL;
			gridData.verticalAlignment = SWT.FILL;
			// gridData.grabExcessHorizontalSpace = true;
			// gridData.grabExcessVerticalSpace = true;
			methodList.setLayoutData(gridData);

		}

		public void setReadabilityList(List readabilityList) {
			this.readabilityList = readabilityList;
		}

		public void updateList(Object input) {
			this.getDisplay().asyncExec(new Runnable() {

				@Override
				public void run() {
					methodList.removeAll();
					readabilityList = (List<Readability>) input;

					String[] readabilities = new String[readabilityList.size()];
					for (int i = 0; i < readabilityList.size(); i++) {
						readabilities[i] = readabilityList.get(i).toString();
					}

					methodList.setItems(readabilities);
				}
			});
		}

		public void setReadabilityGraph(MatrixGraph readabilityGraph) {
			this.readabilityGraph = readabilityGraph;
		}

		public void initialize() {
			this.getDisplay().asyncExec(new Runnable() {

				@Override
				public void run() {
					List<Readability> readabilities = null;

					User user = db.getCurrentUser();
					if (user != null) {
						readabilities = db.getListData(user);
						updateList(readabilities);
					} else {
						userdataLabel.setVisible(true);
					}

					// readabilities = getListData(getCurrentUser());

					// try {
					// ObjectMapper objectMapper = new ObjectMapper();
					// String mappedData = "username=" + user.getUsername();
					//
					// URL obj = new URL(serverUrl + "/listData");
					// HttpURLConnection con = (HttpURLConnection)
					// obj.openConnection();
					//
					// con.setRequestMethod("POST");
					// con.setRequestProperty("Accept-Language",
					// "en-US,en;q=0.5");
					// con.setDoOutput(true);
					// DataOutputStream wr = new
					// DataOutputStream(con.getOutputStream());
					// wr.writeBytes(mappedData);
					// wr.flush();
					// wr.close();
					//
					// BufferedReader in = new BufferedReader(new
					// InputStreamReader(con.getInputStream()));
					// String inputLine;
					//
					// while ((inputLine = in.readLine()) != null) {
					// readabilities = objectMapper.readValue(inputLine,
					// TypeFactory.defaultInstance()
					// .constructCollectionType(List.class, Readability.class));
					// }
					// in.close();
					// } catch (JsonProcessingException JPE) {
					// JPE.printStackTrace();
					// } catch (MalformedURLException e1) {
					// e1.printStackTrace();
					// } catch (IOException e1) {
					// e1.printStackTrace();
					// }
				}
			});
		}

		public void selectCurrentOne(Readability currentOne) {
			for (Readability readability : readabilityList) {
				String curClassname = readability.getClassName();
				String curMethodname = readability.getMethodName();

				if (curClassname.equals(currentOne.getClassName())
						&& curMethodname.equals(currentOne.getMethodName())) {
					int index = readabilityList.indexOf(readability);
					methodList.setSelection(index);
				}
			}
		}
	}

	// public class ElementTable extends Canvas {
	//
	// private Grid elementTable;
	// private Readability readabilityInfo;
	// private final int numOfColumns = 2;
	//
	// public ElementTable(Composite parent, int style) {
	// super(parent, style);
	//
	// GridLayout layout = new GridLayout(1, false);
	// parent.setLayout(layout);
	//
	// elementTable = new Grid(parent, style);
	// elementTable.setHeaderVisible(true);
	// elementTable.setWordWrapHeader(true);
	// elementTable.setBackground(new Color(elementTable.getDisplay()
	// ,255,255,255));
	// elementTable.setEnabled(false);
	// elementTable.setAutoHeight(true);
	// elementTable.setAutoWidth(true);
	//
	// GridData gridData = new GridData();
	// gridData.horizontalAlignment = SWT.FILL;
	// gridData.verticalAlignment = SWT.FILL;
	//// gridData.grabExcessHorizontalSpace = true;
	//// gridData.grabExcessVerticalSpace = true;
	// elementTable.setLayoutData(gridData);
	//
	// createColumns();
	// }
	//
	// private void createColumns() {
	// GridColumn elementColumn = new GridColumn(elementTable, SWT.NONE);
	// elementColumn.setText("features");
	// elementColumn.setHeaderWordWrap(true);
	// elementColumn.setWidth(100);
	// // elementColumn.setWidth(this.getDisplay().getBounds().x / 2);
	//
	// GridColumn dataColumn = new GridColumn(elementTable, SWT.NONE);
	// dataColumn.setText("value");
	// dataColumn.setHeaderWordWrap(true);
	// dataColumn.setWidth(100);
	// // dataColumn.setWidth(this.getDisplay().getBounds().x / 2);
	// // dataColumn.setWidth(this.getBorderWidth() / 2);
	// }
	//
	// public void refreshData(Readability readability) {
	// this.getDisplay().asyncExec(new Runnable() {
	// @Override
	// public void run() {
	// elementTable.clearItems();
	// GridItem locItem = new GridItem(elementTable, SWT.NONE);
	// locItem.setText(numOfColumns - 2, "loc");
	//
	// GridItem loopItem = new GridItem(elementTable, SWT.NONE);
	// loopItem.setText(numOfColumns - 2, "loop");
	//
	// GridItem assignmentItem = new GridItem(elementTable, SWT.NONE);
	// assignmentItem.setText(numOfColumns - 2, "assignment");
	//
	// GridItem branchItem = new GridItem(elementTable, SWT.NONE);
	// branchItem.setText(numOfColumns - 2, "branch");
	//
	// GridItem readabilityItem = new GridItem(elementTable, SWT.NONE);
	// readabilityItem.setText(numOfColumns - 2, "readability");
	//
	// if (readability != null) {
	// locItem.setText(numOfColumns - 1, String.valueOf(readability.getLoc()));
	//
	// loopItem.setText(numOfColumns - 1,
	// String.valueOf(readability.getLoop()));
	//
	// assignmentItem.setText(numOfColumns - 1,
	// String.valueOf(readability.getAssignment()));
	//
	// branchItem.setText(numOfColumns - 1,
	// String.valueOf(readability.getBranch()));
	//
	// readabilityItem.setText(numOfColumns - 1,
	// String.valueOf(readability.getReadability()));
	// }
	// }
	// });
	// }
	//
	// @Override
	// public void setLayout(Layout layout) {
	// super.setLayout(layout);
	//
	// elementTable.setLayout(layout);
	// }
	// }

	// public class UserInput{
	// public void showUserWindow()
	// {
	// Display display = new Display();
	// Shell shell = new Shell(display);
	// shell.setLayout(new GridLayout(2, false));
	//
	// Label idLabel = new Label(shell, SWT.NONE);
	// idLabel.setText("ID:");
	//
	// Text idInput = new Text(shell, SWT.BORDER);
	// GridData gridData = new GridData();
	// gridData.horizontalAlignment = SWT.FILL;
	// gridData.grabExcessHorizontalSpace = true;
	// idInput.setLayoutData(gridData);
	//
	// Label passwordLabel = new Label(shell, SWT.NONE);
	// passwordLabel.setText("Password:");
	// gridData = new GridData();
	// gridData.verticalAlignment = SWT.TOP;
	// passwordLabel.setLayoutData(gridData);
	//
	// Text passwordInput = new Text(shell, SWT.BORDER | SWT.WRAP | SWT.MULTI);
	// gridData = new GridData();
	// gridData.horizontalAlignment = SWT.FILL;
	// gridData.grabExcessHorizontalSpace = true;
	// gridData.verticalAlignment = SWT.FILL;
	// gridData.grabExcessVerticalSpace = true;
	// passwordInput.setLayoutData(gridData);
	//
	// Button okButton = new Button(shell, SWT.PUSH);
	// okButton.setText("OK");
	// gridData = new GridData();
	// gridData.horizontalAlignment = GridData.FILL;
	// gridData.grabExcessHorizontalSpace = true;
	// okButton.setLayoutData(gridData);
	// okButton.addListener(SWT.Selection, new Listener() {
	// public void handleEvent(Event e) {
	// switch (e.type) {
	// case SWT.Selection:
	// Constants.username = idInput.getText();
	// Constants.password = passwordInput.getText();
	//
	// display.close();
	//
	// System.out.println("id : " + Constants.username);
	// System.out.println("password : " + Constants.password);
	// break;
	// }
	// }
	// });
	//
	// Button cancleButton = new Button(shell, SWT.PUSH);
	// cancleButton.setText("Cancle");
	// gridData = new GridData();
	// gridData.horizontalAlignment = GridData.FILL;
	// gridData.grabExcessHorizontalSpace = true;
	// cancleButton.setLayoutData(gridData);
	// cancleButton.addListener(SWT.Selection, new Listener() {
	// public void handleEvent(Event e) {
	// switch (e.type) {
	// case SWT.Selection:
	// Constants.username = "test1";
	// Constants.password = "test1";
	//
	// display.close();
	//
	// System.out.println("id : " + Constants.username);
	// System.out.println("password : " + Constants.password);
	// break;
	// }
	// }
	// });
	//
	// shell.pack();
	// shell.open();
	//
	// while (!shell.isDisposed()) {
	// if (!display.readAndDispatch()) {
	// display.sleep();
	// }
	// }
	// }
	// }
}
