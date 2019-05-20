package com.onycom.ankus4paas;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;

import DataService.MongoManager;

import javax.print.attribute.standard.Media;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;

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
@RestController
@RequestMapping(value = "/api")
public class UserManagement {
	
	@Autowired
	private MongoManager mongoManager;
	
	//ex)http://localhost:8090/signup/myid=prismdata&appname=test
	@RequestMapping(value = "signup/myid={username}&appname={appName}", method=RequestMethod.GET)
	public String signup(@PathVariable("username") String userName, @PathVariable("appName") String appName) {
		
		String fullKey = genMD5(userName, appName);
		return fullKey;
	}
	

	@PostMapping("/signup2")
	public String signup2(@RequestBody UserAppKey userAppkey) {
		
		System.out.println(userAppkey.getUser_appname());
		System.out.println(userAppkey.getUser_email());
		
		MongoCollection<Document> userAppKeyCollection = mongoManager.getUserAppCollection("UserAppKey");
		long cnt = userAppKeyCollection.countDocuments();
		
		if ( cnt == 0) {
			String md5Key = genMD5(userAppkey.getUser_email(), userAppkey.getUser_appname());
			BasicDBObject dbObject = new BasicDBObject();
			dbObject.put("e_mail",userAppkey.getUser_email());
			dbObject.put("app_name",userAppkey.getUser_appname());
			dbObject.put("app_key", md5Key);
			userAppKeyCollection.insertOne(new Document(dbObject));
			return md5Key;
		} else {
			BasicDBObject dbObject = new BasicDBObject();
			dbObject.put("e_mail",userAppkey.getUser_email());
			dbObject.put("app_name",userAppkey.getUser_appname());
			MongoCursor<Document> cursor = userAppKeyCollection.find(dbObject).iterator();
			Document doc = cursor.next();
			String md5Key = doc.get("app_key").toString();
			System.out.println(md5Key);
			return md5Key;
		}
	}
	
	@PostMapping("/getAppKey")
	public String getAppKey(@RequestBody UserAppKey userAppkey) {
		
		System.out.println(userAppkey.getUser_appname());
		System.out.println(userAppkey.getUser_email());
		
		MongoCollection<Document> userAppKeyCollection = mongoManager.getUserAppCollection("UserAppKey");
		long cnt = userAppKeyCollection.countDocuments();
		
		if ( cnt == 0) {
			return "keyNotExist";
		} else {
			BasicDBObject dbObject = new BasicDBObject();
			dbObject.put("e_mail",userAppkey.getUser_email());
			dbObject.put("app_name",userAppkey.getUser_appname());
			MongoCursor<Document> cursor = userAppKeyCollection.find(dbObject).iterator();
			Document doc = cursor.next();
			String md5Key = doc.get("app_key").toString();
			System.out.println(md5Key);
			return md5Key;
		}
	}

	private String genMD5(String un, String an) {
		String result = "";
		try {
	         // Create MD5 Hash
			String s = un + an;
			MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
			digest.update(s.getBytes());
			byte messageDigest[] = digest.digest();
			//Create Hex String
			StringBuffer hexString = new StringBuffer();
			for (int i = 0; i < messageDigest.length; i++) {
				String h = Integer.toHexString(0xFF & messageDigest[i]);
				while (h.length() < 2)
					h = "0" + h;
				hexString.append(h);
				}
			return hexString.toString();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return result;
	}
}
