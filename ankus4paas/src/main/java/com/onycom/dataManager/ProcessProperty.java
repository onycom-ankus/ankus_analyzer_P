package com.onycom.dataManager;

import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

public class ProcessProperty {
	private String appkey;
	private String packageName;
	private String functionName;
	private List<Pair> functionParam;
	
	public String getFunctionName() {
		return functionName;
	}
	public void setFunctionName(String functionName) {
		this.functionName = functionName;
	}
	
	public String getAppkey() {
		return appkey;
	}
	public void setAppkey(String appkey) {
		this.appkey = appkey;
	}
	public String getPackageName() {
		return packageName;
	}
	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}
	public List<Pair> getFunctionParam() {
		return functionParam;
	}
	public void setFunctionParam(List<Pair> functionParam) {
		this.functionParam = functionParam;
	}
	@Override
	public String toString() {
		return "ProcessProperty [appkey=" + appkey + ", packageName=" + packageName + ", functionParam=" + functionParam
				+ "]";
	}
}
