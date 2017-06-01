package kr.ac.jbnu.ssel.instantfeedback.tool;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import com.fasterxml.jackson.core.JsonProcessingException;

import kr.ac.jbnu.ssel.instantfeedback.domain.Readability;
import kr.ac.jbnu.ssel.instantfeedback.domain.User;

public class DataSender {
//	private String serverUrl = "http://210.117.128.248:1005/readability";
	private String serverUrl = "http://175.249.136.209:80/readability";
//	private String serverUrl = "http://localhost:8080/readability";
	
	public void saveReadabilityToServer(Readability readability) {
		try {
			String mappedData = "loc=" + readability.getLOC()
					+ "&comments=" + readability.getNumOfComments()
					+ "&blankLines=" + readability.getNumOfBlankLines()
					+ "&bitOperaters=" + readability.getNumOfBitOperators()
					+ "&patternRate=" + readability.getPatternRate()
					+ "&nestedControl=" + readability.getMaxNestedControl()
					+ "&programVolume=" + readability.getProgramVolume()
					+ "&entropy=" + readability.getEntropy()
					+ "&readability=" + readability.getReadability()
//					+ "&patternRate=" + readability.getPatternRate()
					+ "&methodName=" + readability.getMethodName()
					+ "&className=" + readability.getClassName()
					+ "&username=" + readability.getUser().getUsername()
					+ "&storedTime=" + readability.getStoredTime();

			URL obj = new URL(serverUrl + "/save");
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();

			con.setRequestMethod("POST");
			con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
			con.setDoOutput(true);
			DataOutputStream wr = new DataOutputStream(con.getOutputStream());
			wr.writeBytes(mappedData);
			wr.flush();
			wr.close();

			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			in.close();
			con.disconnect();
		} catch (JsonProcessingException JPE) {
			JPE.printStackTrace();
		} catch (MalformedURLException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
	
	public void saveUserToServer(User user) {
		try {
			String mappedData = "username=" + user.getUsername()
					+ "&age=" + user.getAge()
					+ "&experience=" + user.getExpierence()
					+ "&javaexperience=" + user.getJavaExpierence()
					+ "&area=" + user.getArea();

			URL obj = new URL(serverUrl + "/saveUser");
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();

			con.setRequestMethod("POST");
			con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
			con.setDoOutput(true);
			DataOutputStream wr = new DataOutputStream(con.getOutputStream());
			wr.writeBytes(mappedData);
			wr.flush();
			wr.close();

			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			in.close();
			con.disconnect();
		} catch (JsonProcessingException JPE) {
			JPE.printStackTrace();
		} catch (MalformedURLException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
}
