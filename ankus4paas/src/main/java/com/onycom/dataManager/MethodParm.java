package com.onycom.dataManager;

import java.util.HashMap;

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
	public HashMap<String, String> getMethod_param() {
		return method_param;
	}
	public void setMethod_param(HashMap<String, String> method_param) {
		this.method_param = method_param;
	}
	private HashMap<String, String>  method_param;
}
