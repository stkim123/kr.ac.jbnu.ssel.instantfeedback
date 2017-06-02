package kr.ac.jbnu.ssel.instantfeedback.tool.db;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.eclipse.core.runtime.preferences.ConfigurationScope;
import org.hsqldb.Server;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

import kr.ac.jbnu.ssel.instantfeedback.Constants;
import kr.ac.jbnu.ssel.instantfeedback.domain.Readability;
import kr.ac.jbnu.ssel.instantfeedback.domain.User;

public class DBConnector {
	private static DBConnector dbConnector;
	private Connection dbConnection;
	private Server dbServer;

	public static DBConnector getInstance() {
		if (dbConnector == null)
			dbConnector = new DBConnector();
		return dbConnector;
	}

	public void DBSetup() {
		try {
			if (dbServer != null && dbServer.isNoSystemExit())
				return;

			PrintWriter readability_logWriter = createLogWriter(
					Constants.readabilityDBName + "_" + Constants.DBPath + ".log", true, true);

			dbServer = HSQLDBClass.startServer(Constants.readabilityDBName, Constants.DBPath, readability_logWriter);

			setConnection("sa", "");
			dbConnection.setAutoCommit(true);

			Statement stmt = dbConnection.createStatement();

//			stmt.execute("drop table  if exists readability");
//			stmt.execute("drop table  if exists user");
			stmt.execute("CREATE TABLE IF NOT EXISTS user ( username VARCHAR(32) NOT NULL UNIQUE, age INTEGER,"
					+ "expierence INTEGER, javaExpierence INTEGER, area VARCHAR(32), createdTime datetime"
					+ ", macAddress VARCHAR(50), isSended BIT(1) default 0);");

			createDefaultUser();
			
			stmt.execute("CREATE TABLE IF NOT EXISTS readability ( id INTEGER NOT NULL IDENTITY"
					+ ",LOC INTEGER DEFAULT NULL, "
					+ "numOfComments INTEGER DEFAULT NULL"
					+ ",numOfBlankLines INTEGER DEFAULT NULL"
					+ ",numOfBitOperators INTEGER DEFAULT NULL"
					+ ",readability double DEFAULT NULL"
					+ ",username varchar(255) DEFAULT NULL"
					+ ",storedTime datetime DEFAULT NULL"
					+ ",methodname varchar(255) DEFAULT NULL"
					+ ",classname varchar(255) DEFAULT NULL"
					+ ",packagename varchar(255) DEFAULT NULL"
					+ ",methodsignature varchar(255) DEFAULT NULL"
					// + ",patternrate double DEFAULT NULL"
					+ ",maxNestedControl INTEGER DEFAULT NULL"
					+ ",programVolume double DEFAULT NULL"
					+ ",entropy double DEFAULT NULL"
					+ ", CONSTRAINT username FOREIGN KEY (username) REFERENCES "
					+ "user(username) ON DELETE NO ACTION ON UPDATE NO ACTION"
					+ ",isSended BIT(1) default 0"
					+ ");");
			
//			deleteData();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private void createDefaultUser() {
		User user = getCurrentUser();
		
		if(user != null)
			return;
		
		user = new User();
		user.setUsername("test");
		user.setAge(1);
		user.setArea("test");
		user.setJavaExpierence(1);
		user.setExpierence(1);
		user.setCreatedDate(new Date());
		user.setSended(true);
		
		InetAddress ip;
		StringBuilder macString = new StringBuilder("");
		try {
			ip = InetAddress.getLocalHost();
			System.out.println("Current IP address : " + ip.getHostAddress());
	
			NetworkInterface network = NetworkInterface.getByInetAddress(ip);
	
			byte[] mac = network.getHardwareAddress();
	
			for (int i = 0; i < mac.length; i++) {
				macString.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? "-" : ""));
			}
		}  catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (SocketException e){
			e.printStackTrace();
		}
		user.setMacAddress(macString.toString());
		
		saveUserData(user);
	}

	private PrintWriter createLogWriter(String logFileName, boolean append, boolean autoFlush) throws IOException {
		File f = new File(logFileName);

		// create file if not exists
		if (!f.exists()) {
			String logFilePath = f.getAbsolutePath();

			// create parent folders
			File folder = new File(logFilePath.substring(0, logFilePath.indexOf(logFileName)));
			folder.mkdirs();

			// create file
			f.createNewFile();
		}
		FileWriter fw = new FileWriter(f, append);
		return new PrintWriter(fw, autoFlush);
	}

	private void setConnection(String userName, String password) throws SQLException, ClassNotFoundException {
		if (dbConnection == null || dbConnection.isClosed()) {
			Class.forName("org.hsqldb.jdbcDriver");
			dbConnection = DriverManager.getConnection(Constants.baseDBUrl + Constants.readabilityDBName, userName,
					password);
		}
	}

	public void storeReadability(Readability readability) {
		try {
			// String readabilityInsert = "INSERT INTO readability (LOC,
			// numOfComments, numOfBlankLines, numOfBitOperators,"
			// + " readability, username, storedTime, methodname, classname,
			// patternRate, maxNestedControl, programVolume, entropy) VALUES("
			// + "?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
			String readabilityInsert = "INSERT INTO readability (LOC, numOfComments, numOfBlankLines, numOfBitOperators,"
					+ " readability, username, storedTime, methodname, classname, packagename, methodsignature, maxNestedControl, programVolume, entropy) VALUES("
					+ "?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
			PreparedStatement stmt = dbConnection.prepareStatement(readabilityInsert);
			stmt.setInt(1, readability.getLOC());
			stmt.setInt(2, readability.getNumOfComments());
			stmt.setInt(3, readability.getNumOfBlankLines());
			stmt.setInt(4, readability.getNumOfBitOperators());
			stmt.setDouble(5, readability.getReadability());
			stmt.setString(6, readability.getUser().getUsername());
			stmt.setDate(7, new java.sql.Date(readability.getStoredTime().getTime()));
			stmt.setString(8, readability.getMethodName());
			stmt.setString(9, readability.getClassName());
			stmt.setString(10, readability.getPackageName());
			stmt.setString(11, readability.getMethodSignature());
			// stmt.setDouble(10, readability.getPatternRate());
			stmt.setInt(12, readability.getMaxNestedControl());
			stmt.setDouble(13, readability.getProgramVolume());
			stmt.setDouble(14, readability.getEntropy());

			// Statement stmt = dbConnection.createStatement();
			// stmt.executeUpdate(
			// "INSERT INTO readability (LOC, numOfComments, numOfBlankLines,
			// numOfBitOperators,"
			// + " readability, username, storedTime, methodname, classname,
			// patternRate, maxNestedControl, programVolume, entropy) VALUES("
			// + readability.getLOC() + ", " + readability.getNumOfComments() +
			// ", "
			// + readability.getNumOfBlankLines() + ", " +
			// readability.getNumOfBitOperators() + ", "
			// + readability.getReadability() + ", '" +
			// readability.getUser().getUsername() + "', "
			// + readability.getStoredTime() + ", '" +
			// readability.getMethodName() + "', '"
			// + readability.getClassName() + "', " +
			// readability.getPatternRate() + ", "
			// + readability.getMaxNestedControl() + ", " +
			// readability.getProgramVolume() + ", "
			// + readability.getEntropy() + ")",
			// Statement.RETURN_GENERATED_KEYS);
			stmt.executeUpdate();
		} catch (SQLException e) {
			System.out.println("Error : saveReadabilityData");
			e.printStackTrace();
		}
	}

	public List<Readability> getReadabilityInfos(User user) {
		List<Readability> readabilities = new ArrayList<Readability>();

		try {
			Statement stmt = dbConnection.createStatement();
			ResultSet rs = stmt.executeQuery("select * from readability where username='" + user.getUsername() + "'");

			Readability readabilityInfo = null;
			while (rs.next()) {
				readabilityInfo = new Readability();
				readabilityInfo.setLOC(rs.getInt("LOC"));
				readabilityInfo.setNumOfComments(rs.getInt("numOfComments"));
				readabilityInfo.setNumOfBlankLines(rs.getInt("numOfBlankLines"));
				readabilityInfo.setNumOfBitOperators(rs.getInt("numOfBitOperators"));
				readabilityInfo.setMaxNestedControl(rs.getInt("maxNestedControl"));
				readabilityInfo.setProgramVolume(rs.getDouble("programVolume"));
				readabilityInfo.setEntropy(rs.getDouble("entropy"));
				readabilityInfo.setReadability(rs.getDouble("readability"));
				readabilityInfo.setMethodName(rs.getString("methodname"));
				readabilityInfo.setClassName(rs.getString("classname"));
				readabilityInfo.setPackageName(rs.getString("packagename"));
				readabilityInfo.setMethodSignature(rs.getString("methodsignature"));
				readabilityInfo.setStoredTime(rs.getDate("storedTime"));
				readabilityInfo.setUser(user);
				readabilities.add(readabilityInfo);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return readabilities;
	}
	
	public Readability getLastReadability(Readability readability) {
		Readability readabilityInfo = null;
		try {
			Statement stmt = dbConnection.createStatement();
			ResultSet rs = stmt.executeQuery("select * from readability where "
					+ "username='" + readability.getUser().getUsername()
					+ "' and packagename='" + readability.getPackageName()
					+ "' and classname='" + readability.getClassName()
					+ "' and methodname='" + readability.getMethodName()
					+ "' and methodsignature='" + readability.getMethodSignature()
					+ "' ORDER BY id DESC");
			
			if(rs.next() && rs.next()){
				readabilityInfo = new Readability();
				readabilityInfo.setReadability(rs.getDouble("readability"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return readabilityInfo;
	}

	public void saveUserData(User user) {
		try {
			String userInsert = "INSERT INTO user (username, age, expierence, javaExpierence, area, createdTime) VALUES(?,?,?,?,?,?)";

			PreparedStatement insertStmt = dbConnection.prepareStatement(userInsert);
			insertStmt.setString(1, user.getUsername());
			insertStmt.setInt(2, user.getAge());
			insertStmt.setInt(3, user.getExpierence());
			insertStmt.setInt(4, user.getJavaExpierence());
			insertStmt.setString(5, user.getArea());
			insertStmt.setDate(6, new java.sql.Date(user.getCreatedDate().getTime()));
			insertStmt.executeUpdate();
		} catch (SQLException e) {
			System.out.println("Error : saveUserData");
			e.printStackTrace();
		}
	}

	public List<Readability> getGraphData(Readability source) {
		List<Readability> readabilities = new ArrayList<Readability>();
		List<Readability> result = new ArrayList<Readability>();
		User user = source.getUser();

		try {
			Statement stmt = dbConnection.createStatement();
			ResultSet rs = stmt.executeQuery("select * from readability where "
					+ "username='" + user.getUsername()
					+ "' and methodname='" + source.getMethodName() + "' and classname='" + source.getClassName()
//					+ "methodname='" + source.getMethodName() + "' and classname='" + source.getClassName()
					+ "' and packagename='" + source.getPackageName() + "' and methodsignature='"+source.getMethodSignature() 
					+ "' order by storedTime desc limit "+ Constants.maxGraphResult*3 + ";");

			Readability readabilityInfo = null;
			while (rs.next()) {
				readabilityInfo = new Readability();
				readabilityInfo.setReadability(rs.getDouble("readability"));
				readabilityInfo.setMethodName(rs.getString("methodname"));
				readabilityInfo.setClassName(rs.getString("classname"));
				readabilityInfo.setPackageName(rs.getString("packagename"));
				readabilityInfo.setMethodSignature(rs.getString("methodsignature"));
				readabilities.add(readabilityInfo);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

//		for (Readability readability : readabilities) {
////			String storedMethodName = readability.getMethodName();
////			String storedClassName = readability.getClassName();
////			if (storedClassName.equals(source.getClassName()) && storedMethodName.equals(source.getMethodName()))
//				result.add(readability);
//			if (result.size() == Constants.maxGraphResult)
//				break;
//		}

		// try {
		// String mappedData = "username=test1&methodName=" +
		// readability.getMethodName()
		// + "&className=" + readability.getClassName();
		//// String mappedData = "username=" + user.getUsername() +
		// "&methodName=" + readability.getMethodName()
		//// + "&className=" + readability.getClassName();
		//
		// ObjectMapper objectMapper = new ObjectMapper();
		//
		// URL obj = new URL(serverUrl + "/graphData");
		// HttpURLConnection con = (HttpURLConnection) obj.openConnection();
		//
		// con.setRequestMethod("POST");
		// con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
		// con.setDoOutput(true);
		// DataOutputStream wr = new DataOutputStream(con.getOutputStream());
		// wr.writeBytes(mappedData);
		// wr.flush();
		// wr.close();
		//
		// BufferedReader in = new BufferedReader(new
		// InputStreamReader(con.getInputStream()));
		// String inputLine;
		//
		// while ((inputLine = in.readLine()) != null) {
		// // Readability responseData = objectMapper.readValue(inputLine,
		// // Readability.class);
		// result = objectMapper.readValue(inputLine,
		// TypeFactory.defaultInstance().constructCollectionType(List.class,
		// Readability.class));
		// }
		// in.close();
		// con.disconnect();
		// } catch (JsonProcessingException JPE) {
		// JPE.printStackTrace();
		// } catch (MalformedURLException e1) {
		// e1.printStackTrace();
		// } catch (IOException e1) {
		// e1.printStackTrace();
		// }

		return readabilities;
	}

	public List<Readability> getListData(User user) {
		List<Readability> readabilities = new ArrayList<Readability>();
		List<Readability> result = new ArrayList<Readability>();

		try {
			Statement stmt = dbConnection.createStatement();
			ResultSet rs = stmt.executeQuery("select * from readability where username='" + user.getUsername() + "';");

			Readability readabilityInfo = null;
			while (rs.next()) {
				readabilityInfo = new Readability();
				readabilityInfo.setReadability(rs.getDouble("readability"));
				readabilityInfo.setMethodName(rs.getString("methodname"));
				readabilityInfo.setClassName(rs.getString("classname"));
				readabilityInfo.setPackageName(rs.getString("packagename"));
				readabilityInfo.setMethodSignature(rs.getString("methodsignature"));
				readabilityInfo.setStoredTime(rs.getDate("storedTime"));
				User searchedUser = new User();
				searchedUser.setUsername(rs.getString("username"));
				readabilityInfo.setUser(searchedUser);
				readabilities.add(readabilityInfo);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		for (Readability readability : readabilities) {
			if (result.isEmpty())
				result.add(readability);

			Readability duplicatedMethod = findReadabilityHadSameName(result, readability);
			if (duplicatedMethod == null)
				result.add(readability);
			else {
				Date currentTime = duplicatedMethod.getStoredTime();
				Date newTime = readability.getStoredTime();

				if (currentTime.compareTo(newTime) < 0) {
					int index = result.indexOf(duplicatedMethod);
					result.remove(duplicatedMethod);
					result.add(index, readability);
				}
			}

		}

		return result;

		// try {
		// ObjectMapper objectMapper = new ObjectMapper();
		// // String mappedData = objectMapper.writeValueAsString(user);
		// String mappedData = "username=" + user.getUsername();
		// // String mappedData = "username=test1";
		//
		// URL obj = new URL(serverUrl + "/listData");
		// HttpURLConnection con = (HttpURLConnection) obj.openConnection();
		//
		// con.setRequestMethod("POST");
		// con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
		// con.setDoOutput(true);
		// DataOutputStream wr = new DataOutputStream(con.getOutputStream());
		// wr.writeBytes(mappedData);
		// wr.flush();
		// wr.close();
		//
		// BufferedReader in = new BufferedReader(new
		// InputStreamReader(con.getInputStream()));
		// String inputLine;
		//
		// while ((inputLine = in.readLine()) != null) {
		// result = objectMapper.readValue(inputLine,
		// TypeFactory.defaultInstance().constructCollectionType(List.class,
		// Readability.class));
		// }
		// in.close();
		// con.disconnect();
		// } catch (JsonProcessingException JPE) {
		// JPE.printStackTrace();
		// } catch (MalformedURLException e1) {
		// e1.printStackTrace();
		// } catch (IOException e1) {
		// e1.printStackTrace();
		// }
		// return result;
	}

	private Readability findReadabilityHadSameName(List<Readability> list, Readability target) {
		Readability result = null;
		for (Readability readability : list) {
			String classNameOfList = readability.getClassName();
			String methodNameOfList = readability.getMethodName();
			String classNameOfTarget = target.getClassName();
			String methodNameOfTarget = target.getMethodName();

			if (classNameOfList.equals(classNameOfTarget) && methodNameOfList.equals(methodNameOfTarget)) {
				result = readability;
				break;
			}
		}
		return result;
	}

	public User getCurrentUser() {
		User result = null;

		try {
			Statement stmt = dbConnection.createStatement();
			ResultSet rs = stmt.executeQuery("select * from user ORDER BY createdTime DESC;");
			if(rs.next()){
				result = new User();
				result.setUsername(rs.getString("username"));
				result.setAge(rs.getInt("age"));
				result.setArea(rs.getString("area"));
				result.setExpierence(rs.getInt("expierence"));
				result.setJavaExpierence(rs.getInt("javaexpierence"));
				result.setMacAddress(rs.getString("macAddress"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return result;
	}
	
	public List<User> getAllUsers() {
		List<User> allUsers = new ArrayList<User>();
		
		try {
			Statement stmt = dbConnection.createStatement();
			ResultSet rs = stmt.executeQuery("select * from user where sended = 0;");
			User result = null;
			while(rs.next()){
				result = new User();
				result.setUsername(rs.getString("username"));
				result.setAge(rs.getInt("age"));
				result.setArea(rs.getString("area"));
				result.setExpierence(rs.getInt("expierence"));
				result.setJavaExpierence(rs.getInt("javaexpierence"));
				result.setMacAddress(rs.getString("macAddress"));
				allUsers.add(result);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return allUsers;
	}
	
	public List<Readability> getAllReadabilityInfos() {
		List<Readability> readabilities = new ArrayList<Readability>();
		
		try {
			Statement stmt = dbConnection.createStatement();
			ResultSet rs = stmt.executeQuery("select * from readability sended = 0;");

			Readability readabilityInfo = null;
			while (rs.next()) {
				readabilityInfo = new Readability();
				readabilityInfo.setLOC(rs.getInt("LOC"));
				readabilityInfo.setNumOfComments(rs.getInt("numOfComments"));
				readabilityInfo.setNumOfBlankLines(rs.getInt("numOfBlankLines"));
				readabilityInfo.setNumOfBitOperators(rs.getInt("numOfBitOperators"));
				readabilityInfo.setMaxNestedControl(rs.getInt("maxNestedControl"));
				readabilityInfo.setProgramVolume(rs.getDouble("programVolume"));
				readabilityInfo.setEntropy(rs.getDouble("entropy"));
				readabilityInfo.setReadability(rs.getDouble("readability"));
				readabilityInfo.setMethodName(rs.getString("methodname"));
				readabilityInfo.setClassName(rs.getString("classname"));
				readabilityInfo.setPackageName(rs.getString("packagename"));
				readabilityInfo.setMethodSignature(rs.getString("methodsignature"));
				readabilityInfo.setStoredTime(rs.getDate("storedTime"));
				User searchedUser = new User();
				searchedUser.setUsername(rs.getString("username"));
				readabilityInfo.setUser(searchedUser);
				readabilities.add(readabilityInfo);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return readabilities;
	}
	
	public void checkSendedUser(){
		try {
			String sendedSql = "update user set sended = 1 where sended=0";

			Statement updateStmt = dbConnection.createStatement();
			updateStmt.executeUpdate(sendedSql);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void checkSenedReadability(){
		try {
			String sendedSql = "update readability set sended = 1 where sended=0";

			Statement updateStmt = dbConnection.createStatement();
			updateStmt.executeUpdate(sendedSql);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void closeDBServer() {
		if (dbServer != null) {
			HSQLDBClass.stopServer(Constants.readabilityDBName);
		}
	}
	
	public void setUserDataSended() {
		
	}
}
