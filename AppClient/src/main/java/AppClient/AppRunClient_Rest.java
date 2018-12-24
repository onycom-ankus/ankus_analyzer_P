package AppClient;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;

import com.google.gson.Gson;

class UserAppKey {
	private String user_email;
	private String user_appname;
	
	public String getUser_email() {
		return user_email;
	}
	public void setUser_email(String user_email) {
		this.user_email = user_email;
	}
	public String getUser_appname() {
		return user_appname;
	}
	public void setUser_appname(String user_appname) {
		this.user_appname = user_appname;
	}
}

public class AppRunClient_Rest {
	
	public static void main(String[] args) throws Exception {
//		String appkey = registMyAppKey("prismdata@naver.com", "testApp1");
//		if (appkey == "" ) {
//			boolean exist = existMyAppKey("prismdata@naver.com", "testApp1");
//			if (exist == true) {
//				System.out.println("App key exist");
//			}
//		}
//	}
		String myAppKey = getMyAppKey("prismdata@naver.com", "testApp1");
		System.out.println("my App Key:" + myAppKey);
		String processResult = runMyCmd(myAppKey, "cmd");
	}
	
	private static String runMyCmd(String myAppKey, String string) {
		ProcessProperty processProperty = new ProcessProperty();
		processProperty.setAppkey(myAppKey);
		processProperty.setPackageName("keras");
		processProperty.setFunctionName("/Users/prismdata/Documents/prismdata/ankus_analyzer_P/ankus4paas/keras_ch02.py");
		
		Gson gson = new Gson();
		String json = gson.toJson(processProperty);
		System.out.println(json);
		
		try {
			URL url = new URL("http://localhost:8090/api/runcmd");
			URLConnection connection = url.openConnection();
			connection.setDoOutput(true);
			connection.setRequestProperty("Content-Type", "application/json");
			connection.setConnectTimeout(5000);
//			connection.setReadTimeout(1000000000);
			OutputStreamWriter out = new OutputStreamWriter(connection.getOutputStream());
			out.write(json);
			out.close();

			BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			String rtn = "";
			while ((rtn = in.readLine()) != null) {
				System.out.println(rtn);
				
			}
			System.out.println("\nCrunchify REST Service Invoked Successfully..");
			in.close();
		} catch (Exception e) {
			System.out.println("\nError while calling Crunchify REST Service");
			System.out.println(e);
		}
		
		return null;
	}

	private static String getMyAppKey(String string, String string2) {
		UserAppKey userAppkey = new UserAppKey();
		userAppkey.setUser_appname("testApp1");
		userAppkey.setUser_email("prismdata@naver.com");
		
		Gson gson = new Gson();
		String json = gson.toJson(userAppkey);
		System.out.println(json);
		
		try {
			URL url = new URL("http://localhost:8090/api/getAppKey");
			URLConnection connection = url.openConnection();
			connection.setDoOutput(true);
			connection.setRequestProperty("Content-Type", "application/json");
			connection.setConnectTimeout(5000);
			connection.setReadTimeout(1000000000);
			OutputStreamWriter out = new OutputStreamWriter(connection.getOutputStream());
			out.write(json);
			out.close();

			BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			String rtn = "";
			while ((rtn = in.readLine()) != null) {
				return rtn;
			}
			in.close();
		} catch (Exception e) {
			System.out.println("\nError while calling REST Service");
			System.out.println(e);
		}
		return "";
	}
	
	private static boolean existMyAppKey(String string, String string2) {
		// TODO Auto-generated method stub
		return false;
	}

	private static String registMyAppKey(String string, String string2) {
		UserAppKey userAppkey = new UserAppKey();
		userAppkey.setUser_appname("testApp1");
		userAppkey.setUser_email("prismdata@naver.com");
		
		Gson gson = new Gson();
		String json = gson.toJson(userAppkey);
		System.out.println(json);
		
		try {
			URL url = new URL("http://localhost:8090/api/signup2");
			URLConnection connection = url.openConnection();
			connection.setDoOutput(true);
			connection.setRequestProperty("Content-Type", "application/json");
			connection.setConnectTimeout(5000);
			connection.setReadTimeout(1000000000);
			OutputStreamWriter out = new OutputStreamWriter(connection.getOutputStream());
			out.write(json);
			out.close();

			BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			String rtn = "";
			while ((rtn = in.readLine()) != null) {
				System.out.println(rtn);
				return rtn;
			}
			in.close();
		} catch (Exception e) {
			System.out.println("\nError while calling REST Service");
			System.out.println(e);
		}
		return "";
	}
}
