package com.onycom.ankus4paas;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserManagement {
	
	//ex)http://localhost:8090/signup/myid=prismdata&appname=test
	@RequestMapping(value = "signup/myid={username}&appname={appName}", method=RequestMethod.GET)
	public String signup(@PathVariable("username") String userName, 
							@PathVariable("appName") String appName) {
		
		String fullKey = genSHAKey(userName, appName);
		return fullKey;
	}
	
	public String genSHAKey(String userName, String appName) {
		String keyUserName = "";
		String keyAppName = "";
		try{
			MessageDigest sh;
			sh = MessageDigest.getInstance("SHA-256"); 
			sh.update(userName.getBytes()); 
			byte byteData[] = sh.digest();
			StringBuffer sb = new StringBuffer(); 
			for(int i = 0 ; i < byteData.length ; i++){
				sb.append(Integer.toString((byteData[i]&0xff) + 0x100, 16).substring(1));
			}
			keyUserName = sb.toString();
			
			sh.update(appName.getBytes()); 
			byteData = sh.digest();
			sb = new StringBuffer(); 
			for(int i = 0 ; i < byteData.length ; i++){
				sb.append(Integer.toString((byteData[i]&0xff) + 0x100, 16).substring(1));
			}
			keyAppName = sb.toString();
			
		}catch(NoSuchAlgorithmException e){
			e.printStackTrace(); 
			keyUserName = null; 
			keyAppName = null;
		}
		return keyUserName+keyAppName;
	}
}
