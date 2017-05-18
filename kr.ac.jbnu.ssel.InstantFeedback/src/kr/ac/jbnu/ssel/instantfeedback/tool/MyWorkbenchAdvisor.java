package kr.ac.jbnu.ssel.instantfeedback.tool;

import java.util.List;

import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;

import kr.ac.jbnu.ssel.instantfeedback.domain.Readability;
import kr.ac.jbnu.ssel.instantfeedback.domain.User;
import kr.ac.jbnu.ssel.instantfeedback.tool.db.DBConnector;

public class MyWorkbenchAdvisor extends WorkbenchWindowAdvisor{

	public MyWorkbenchAdvisor(IWorkbenchWindowConfigurer configurer) {
		super(configurer);
	}
	
	@Override
	public ActionBarAdvisor createActionBarAdvisor(IActionBarConfigurer configurer) {
		return super.createActionBarAdvisor(configurer);
	}
	
	@Override
	public boolean preWindowShellClose() {
		DBConnector db = DBConnector.getInstance();
		DataSender sender = new DataSender();
		
		List<User> allUsers = db.getAllUsers();
		for (User user : allUsers) {
			sender.saveUserToServer(user);
		}
		
		List<Readability> allReadabilities = db.getAllReadabilityInfos();
		for (Readability readability : allReadabilities) {
			sender.saveReadabilityToServer(readability);
		}
		
		return super.preWindowShellClose();
	}
	
}
