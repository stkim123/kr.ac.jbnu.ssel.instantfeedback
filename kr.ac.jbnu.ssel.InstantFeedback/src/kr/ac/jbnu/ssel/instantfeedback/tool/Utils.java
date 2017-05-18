package kr.ac.jbnu.ssel.instantfeedback.tool;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.eclipse.core.runtime.FileLocator;

public class Utils {
	
	
	public static String getEclipsePackageDirOfClass(Class<?> cls) {
		String fileURLAsString = null;

		try {
			ClassLoader loader = cls.getClassLoader();
			URL RClassDictory = loader.getResource(cls.getPackage().getName().replace('.', '/'));
			URL fileURL = FileLocator.toFileURL(RClassDictory);
			URI fileURLAsURI = fileURL.toURI();
			fileURLAsString = new File(fileURLAsURI).getAbsolutePath();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}

		return fileURLAsString;
	}
}
