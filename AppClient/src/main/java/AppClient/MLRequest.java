package AppClient;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

import com.google.gson.Gson;
public class MLRequest {
	public static void main(String[] args) {

		try{
			Gson gson = new Gson();
			MethodParm mParam = new MethodParm();
			mParam.setAppkey("123456789");
			mParam.setMethod("svm");
			
//			HashMap<String, String> method_param = new HashMap<String, String>();
//			method_param.put("var1", "1");
//			method_param.put("var2", "2");
//			mParam.setMethod_param(method_param);
			String json = gson.toJson(mParam);
			
			System.out.println(json);
			URL url = new URL("http://localhost:8090/getML/");
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setDoOutput(true);
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Accept", "application/json");

			DataOutputStream output = new DataOutputStream(conn.getOutputStream());
			output.writeUTF("param=" + gson.toJson(mParam));
			output.flush();
			// 응답
			BufferedReader br = null;
			br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
			String line = null;
			while ((line = br.readLine()) != null) {
				System.out.println(line);
			}
			output.close();
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}