package kr.ac.jbnu.ssel.instantfeedback;

import kr.ac.jbnu.ssel.instantfeedback.R.R;
import kr.ac.jbnu.ssel.instantfeedback.tool.Utils;

public class Constants {
	public static String preferencesName = "kr.ac.jbnu.ssel.instantfeedback";
	public static String preferencesUserNode = "readabilityUser";
	public static String usernamePref = "username";
	public static String agePref = "age";
	public static String experiencePref = "experience";
	public static String javaexperiencePref = "javaexperience";
	public static String areaPref = "area";
	
	public static String currentUsername = "test1";
	public static String baseDBUrl = "jdbc:hsqldb:hsql://localhost/";
	public static String userDBName = "user";
	public static String readabilityDBName = "readability";
	public static String DBPath = "readability";
	
	public static final String WEKA_CENTROID_FILE_POSTFIX = ".centroid";
	public static final String CSV_DELIM = ",";
	public static final String LINE_SEPARATER = "\r\n";
	
	public static final int maxGraphResult = 15;
	
//	public static final String wekaCSVPath = Utils.getEclipsePackageDirOfClass(R.class)+ "\\DTM.csv"; 
//	public static final String simianPath = Utils.getEclipsePackageDirOfClass(R.class)+ "\\simianTempClass.java";
}
