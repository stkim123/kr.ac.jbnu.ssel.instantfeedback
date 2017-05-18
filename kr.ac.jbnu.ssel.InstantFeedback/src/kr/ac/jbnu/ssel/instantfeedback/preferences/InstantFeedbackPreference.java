package kr.ac.jbnu.ssel.instantfeedback.preferences;

import java.util.Date;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import kr.ac.jbnu.ssel.instantfeedback.domain.User;
import kr.ac.jbnu.ssel.instantfeedback.tool.DataSender;
import kr.ac.jbnu.ssel.instantfeedback.tool.db.DBConnector;

public class InstantFeedbackPreference extends PreferencePage implements IWorkbenchPreferencePage{

	private Label usernameLabel;
	private Text usernameText;
	private Label ageLabel;
	private Text ageText; 
	private Label expierenceLabel;
	private Text experienceText; 
	private Label javeExpierenceLabel;
	private Text javeExperienceText; 
	private Label workFieldLabel;
	private Text workFieldText; 
	
	private DBConnector db;
	private DataSender sender;
	
	@Override
	public void init(IWorkbench arg0) {
	}

	@Override
	protected Control createContents(Composite parent) {
		sender = new DataSender();
		noDefaultAndApplyButton();
		
		Composite composite = new Composite(parent, SWT.NULL);
		composite.setLayout(new GridLayout(1, false));
		composite.setLayoutData(new GridData(GridData.GRAB_VERTICAL));
		
		Group userInformationGroup = new Group(composite, SWT.NONE);
		userInformationGroup.setLayout(new GridLayout(2, false));
		userInformationGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		usernameLabel = new Label(userInformationGroup, SWT.NONE);
		usernameLabel.setText("Username");
		
		usernameText = new Text(userInformationGroup, SWT.FILL);
		usernameText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		ageLabel = new Label(userInformationGroup, SWT.NONE);
		ageLabel.setText("Age");
		
		ageText = new Text(userInformationGroup, SWT.FILL);
		ageText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		expierenceLabel = new Label(userInformationGroup, SWT.NONE);
		expierenceLabel.setText("Experience");
		
		experienceText = new Text(userInformationGroup, SWT.FILL);
		experienceText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		javeExpierenceLabel = new Label(userInformationGroup, SWT.NONE);
		javeExpierenceLabel.setText("Jave Experience");
		
		javeExperienceText = new Text(userInformationGroup, SWT.FILL);
		javeExperienceText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		workFieldLabel = new Label(userInformationGroup, SWT.NONE);
		workFieldLabel.setText("Working field");
		
		workFieldText = new Text(userInformationGroup, SWT.FILL);
		workFieldText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		this.contributeButtons(composite);
		
		Display.getCurrent().asyncExec(new Runnable() {
			
			@Override
			public void run() {
				db = DBConnector.getInstance();
				db.DBSetup();
				setDefault();
			}
		});
		
		return composite;
	}
	
	@Override
	protected void contributeButtons(Composite parent) {
		super.contributeButtons(parent);
		GridData gd;
		Composite buttonBar = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
        layout.numColumns = 2;
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        layout.makeColumnsEqualWidth = false;
        buttonBar.setLayout(layout);
        
        gd = new GridData(GridData.HORIZONTAL_ALIGN_END);
        
        buttonBar.setLayoutData(gd);
		
		
		Button defaultButton = new Button(buttonBar, SWT.PUSH);
		defaultButton.setText("Resotre");
		int widthHint = convertHorizontalDLUsToPixels(IDialogConstants.BUTTON_WIDTH);
		GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		Point minButtonSize = defaultButton.computeSize(SWT.DEFAULT,
				SWT.DEFAULT, true);
		data.widthHint = Math.max(widthHint, minButtonSize.x);
		defaultButton.setLayoutData(data);
		
		defaultButton.addSelectionListener(new SelectionAdapter() {
			
			@Override
			public void widgetSelected(SelectionEvent event) {
				setDefault();
			}
		});
		Button applyButton = new Button(buttonBar, SWT.PUSH);
		applyButton.setText("Apply");
		data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		minButtonSize = applyButton.computeSize(SWT.DEFAULT, SWT.DEFAULT,
				true);
		data.widthHint = Math.max(widthHint, minButtonSize.x);
		applyButton.setLayoutData(data);
		
		applyButton.addSelectionListener(new SelectionAdapter() {
			
			public void widgetSelected(SelectionEvent e) {
				User user = new User();
				String username = usernameText.getText();
				int age = Integer.valueOf(ageText.getText());
				String workField = workFieldText.getText();
				int experience = Integer.valueOf(experienceText.getText());
				int javaexperience = Integer.valueOf(javeExperienceText.getText());
				
				user.setUsername(username);
				user.setAge(age);
				user.setArea(workField);
				user.setExpierence(experience);
				user.setJavaExpierence(javaexperience);
				user.setCreatedDate(new Date());
				
//				Preferences preferences = ConfigurationScope.INSTANCE
//                        .getNode(Constants.preferencesName);
//				Preferences userPreferences = preferences.node(Constants.preferencesUserNode);
//				try {
//					userPreferences.clear();
//				} catch (BackingStoreException e1) {
//					e1.printStackTrace();
//				}
//				userPreferences.put(Constants.usernamePref, username );
//				userPreferences.putInt(Constants.agePref, age );
//				userPreferences.put(Constants.areaPref, workField);
//				userPreferences.putInt(Constants.experiencePref, experience);
//				userPreferences.putInt(Constants.javaexperiencePref, javaexperience);
//				
//				try {
//					userPreferences.flush();
//				} catch (BackingStoreException e1) {
//					e1.printStackTrace();
//				}
				
				db.saveUserData(user);
				sender.saveUserToServer(user);
			}

		});
	}
	
	private void setDefault(){
		User user = db.getCurrentUser();
		if(user != null && !user.getUsername().equals("default")){
			usernameText.setText(user.getUsername());
			ageText.setText(String.valueOf(user.getAge()));
			experienceText.setText(String.valueOf(user.getExpierence()));
			javeExperienceText.setText(String.valueOf(user.getJavaExpierence()));
			workFieldText.setText(user.getArea());
		}
	}
}


