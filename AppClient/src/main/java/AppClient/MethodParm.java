package AppClient;


import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

public class MethodParm {
	private String appkey;
	public String getAppkey() {
		return appkey;
	}
	public void setAppkey(String appkey) {
		this.appkey = appkey;
	}
	private String method;
	
	public String getMethod() {
		return method;
	}
	public void setMethod(String method) {
		this.method = method;
	}
	public List<Pair<String, String>>  getMethod_param() {
		return method_param;
	}
	public void setMethod_param(List<Pair<String, String>>  method_param) {
		this.method_param = method_param;
	}
	private List<Pair<String, String>>  method_param;
}
