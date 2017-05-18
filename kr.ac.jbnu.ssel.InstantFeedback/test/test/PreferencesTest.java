package test;

import org.eclipse.core.runtime.preferences.ConfigurationScope;
import org.eclipse.jface.preference.IPreferenceStore;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

import kr.ac.jbnu.ssel.instantfeedback.Constants;

public class PreferencesTest {
	public static void main(String[] args) {
		test2();
	}
	
	public static void prefsTest1(){
		Preferences preferences = ConfigurationScope.INSTANCE
                .getNode("test");
		Preferences userPreferences = preferences.node("test");
		
		int t=0, s;
		
		s = userPreferences.getInt(Constants.experiencePref, t);
		try {
			userPreferences.clear();
		} catch (BackingStoreException e1) {
			e1.printStackTrace();
		}
		userPreferences.put(Constants.usernamePref, "user" );
		userPreferences.putInt(Constants.agePref, 1 );
		userPreferences.put(Constants.areaPref, "area");
		userPreferences.putInt(Constants.experiencePref, 3);
		userPreferences.putInt(Constants.javaexperiencePref, 4);
		
		try {
            preferences.flush();
    } catch (BackingStoreException e2) {
            e2.printStackTrace();
    }
		
		Preferences prefs = new ConfigurationScope().getNode(Constants.preferencesName);
		Preferences node = prefs.node(Constants.preferencesUserNode);
		
		
		System.out.println();
	}
	
	public static void test2(){
	}
}
