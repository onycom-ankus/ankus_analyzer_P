/*
 * Copyright 2018 by ONYCOM,INC
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *     
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.onycom.ankus4paas;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.net.ssl.HttpsURLConnection;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;


/**
 * OpenAPI REST Controller.
 *
 *
 * @author onycom
 * @since 1.0
 */

/*

/process/....

/analyzer/...

/visualize/...

*/

@CrossOrigin("*")
@Controller
public class OpenApiController {

    private Logger logger = LoggerFactory.getLogger(OpenApiController.class);
	
	@Value("${platform.work_storage}")
	public String conf_platform_work_storage;
	
	@Value("${platform.engine_storage}")
	public String conf_platform_engine_storage;
	
	@Value("${artifact.cache.path}")
	public String conf_cache_path;
	
	@Value("${jdbc.driver}")
	public String conf_jdbc_driver;
	
	@Value("${jdbc.url}")
	public String conf_jdbc_url;
	
	@Value("${jdbc.username}")
	public String conf_jdbc_uid;
	
	@Value("${jdbc.password}")
	public String conf_jdbc_passwd;
	
	
	
	int chk_apikey(String apikey)
	{
//		logger.info("url={}", conf_jdbc_url);
		
		Connection conn = getConnection(conf_jdbc_url, conf_jdbc_driver, conf_jdbc_uid, conf_jdbc_passwd);

		if(conn!=null)
		{
			String sql = String.format("SELECT * FROM USER WHERE USERNAME='%s'", apikey);

//			logger.info("sql={}{}{}", sql, conf_jdbc_uid, conf_jdbc_passwd);
			ArrayList<HashMap<String, Object>> ids = select_sql(conn, sql);
			
			try {
				conn.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(ids!=null) return ids.size();			
		}
		
		return 0;
	}
	
	
/*	static ResultSet getcubridrs(String serverip, int port, String db, String uid, String pwd, String query)
	{
		String url = String.format("jdbc:cubrid:%s:%d:%s:::?charset=utf8", serverip, port, db);
	    Connection con = null;
	    Statement stmt = null;
	    ResultSet rs = null;
	    try {
	   	  	Class.forName("cubrid.jdbc.driver.CUBRIDDriver");
	    	con = DriverManager.getConnection(url, uid, pwd);
	    	stmt = con.createStatement();
	    	rs = stmt.executeQuery(query);
	    	return rs;
	    }
	    catch(Exception e)
	    {
	    	  e.printStackTrace();
	    }
	    return null;
	}

	static ResultSet getmysqlrs(String serverip, int port, String db, String uid, String pwd, String query)
	{
		String url = String.format("jdbc:mysql://%s:%d/%s?useUnicode=true&characterEncoding=utf8", serverip, port, db);
	    Connection con = null;
	    Statement stmt = null;
	    ResultSet rs = null;
	    try {
	   	  	Class.forName("com.mysql.jdbc.Driver");
	    	con = DriverManager.getConnection(url, uid, pwd);
            stmt = con.createStatement();
	    	rs = stmt.executeQuery(query);
	    	return rs;
	    }
	    catch(Exception e)
	    {
	    	e.printStackTrace();
	    }
	    return null;
	}
*/
	static ArrayList<HashMap<String,Object>> select_sql(Connection conn, String sql)
	{
		try {
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(sql);
			if(rs==null) {
				stmt.close();
				return null;
			}
		
			ArrayList<HashMap<String,Object>> rows = new ArrayList<HashMap<String,Object>>();
			ResultSetMetaData meta = null;
			while(rs.next())
			{
				if(meta==null) meta = rs.getMetaData();
				
				HashMap<String,Object> row = new HashMap<String,Object>();
				for(int i=1; i<=meta.getColumnCount(); i++) row.put(meta.getColumnLabel(i), rs.getString(i));
				rows.add(row);
			}
			rs.close();
			stmt.close();
			return rows;
		}
		catch (Exception e) {
	    	System.out.println("exec msg=["+sql+"]");
		}

		return null;
	}
	
	public static Connection getConnection(String jdbcurl, String jdbcdriver, String uid, String pwd)
	{
		try {
		    Connection con = null;
	    	Class.forName(jdbcdriver);
	    	con = DriverManager.getConnection(jdbcurl, uid, pwd);
	    	return con;
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}	
	
	public static Connection getConnection(String dbmstype, String serverip, int port, String db,  String uid, String pwd)
	{
		/*if(dbmstype.equalsIgnoreCase("oracle"))
		{
			String url = String.format("jdbc:oracle:thin:@%s:%d:%s", serverip, port, db);
			try {
			    Connection con = null;
		    	Class.forName("oracle.jdbc.driver.OracleDriver");
		    	con = DriverManager.getConnection(url, uid, pwd);
		    	return con;
			}
			catch (Exception e) {

			}			
		}
		else */if(dbmstype.equalsIgnoreCase("cubrid"))
		{
			String url = String.format("jdbc:cubrid:%s:%d:%s:::?charset=utf8", serverip, port, db);
//			System.out.println(url);
			try {
			    Connection con = null;
			    Class.forName("cubrid.jdbc.driver.CUBRIDDriver");
		    	con = DriverManager.getConnection(url, uid, pwd);
		    	return con;
			}
			catch (Exception e) {

			}			
		}
		else if(dbmstype.equalsIgnoreCase("mysql"))
		{
			String url = String.format("jdbc:mariadb://%s:%d/%s?useUnicode=true&characterEncoding=utf8", serverip, port, db);
			try {
			    Connection con = null;
			    Class.forName("org.mariadb.jdbc.Driver");
		    	con = DriverManager.getConnection(url, uid, pwd);
		    	return con;
			}
			catch (Exception e) {
				e.printStackTrace();
			}			
		}
/*		else if(dbmstype.equalsIgnoreCase("mysql"))
		{
			String url = String.format("jdbc:mysql://%s:%d/%s?useUnicode=true&characterEncoding=utf8", serverip, port, db);
			try {
			    Connection con = null;
			    Class.forName("com.mysql.jdbc.Driver");
		    	con = DriverManager.getConnection(url, uid, pwd);
		    	return con;
			}
			catch (Exception e) {
				e.printStackTrace();
			}			
		}
		else if(dbmstype.equalsIgnoreCase("mssql"))
		{
			String url = String.format("jdbc:sqlserver://%s:%d;databaseName=%s;user=%s;password=%s", serverip, port, db, uid, pwd);
			System.out.println(url);
			try {
			    Connection con = null;
			    Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
			    
		    	con = DriverManager.getConnection(url, uid, pwd);
		    	return con;
			}
			catch (Exception e) {
				e.printStackTrace();
			}			
		}*/
		return null;
	}
	
	static ResultSet getrs(Connection con, String query)
	{
	    Statement stmt = null;
	    ResultSet rs = null;
	    try {
	    	stmt = con.createStatement();
	    	rs = stmt.executeQuery(query);
	    	return rs;
	    }
	    catch(Exception e)
	    {
	    	  e.printStackTrace();
	    }
		return null;
	}	

	static ResultSet getrs2(Connection con, String query)
	{
	    Statement stmt = null;
	    ResultSet rs = null;
	    try {
	    	stmt = con.createStatement(
	    	        ResultSet.TYPE_SCROLL_INSENSITIVE,
	    	        ResultSet.CONCUR_UPDATABLE);
	    	rs = stmt.executeQuery(query);
	    	return rs;
	    }
	    catch(Exception e)
	    {
	    	  e.printStackTrace();
	    }
		
		return null;
	}	
	
	static int update(Connection con, String query)
	{
		PreparedStatement stmt = null;
	    int  result = 0;
	    try {
	    	stmt = con.prepareStatement(query);
	    	stmt.execute();
	    	result = stmt.getUpdateCount();
	    	stmt.close();
	    }
	    catch(Exception e)
	    {
			System.out.printf("exec fail query=(%s:%s)\n", query, e.getMessage());
			result = -1;
//	    	e.printStackTrace();
	    }
		return result;
	}
	
	// bulk insert 용도, 여러줄 update 시 , prefixsql 이 null 이거나 빈 문자열 이면  postssqls을 실행
	public static int bulkupdate(Connection con, String prefixsql, ArrayList<String> postssqls)
	{
		int ret = 0;
		if(prefixsql==null || prefixsql.isEmpty())
		{
			for(String s:postssqls)
			{
				int r = update(con, s);
				if(r>0) ret += r; 
			}
		}
		else
		{
			StringBuffer sb = new StringBuffer();
			for(String s:postssqls)
			{
				if(sb.length() > 0) sb.append(",");
				sb.append(s);
			}
			int r = update(con, prefixsql+sb.toString()+"");
			if(r>0) ret += r;
			else // bulk 실핼오류시 개별실행. 
			{
				if(postssqls.size()>1) 
					for(String s:postssqls)
					{
						r = update(con, prefixsql+s+"");
						if(r>0) ret += r;
					}
			}
		}
		return ret;
	}		
	
	public static String cvtname(String dbmstype, String nm)
	{
		if(dbmstype.equalsIgnoreCase("cubrid"))	return String.format("[%s]", nm);
		else return nm;
	}
	
	public static String cvtval(String v)
	{
		return v.replaceAll("'", "'||chr(39)||'");
	}
	
	public static String getsubstring(String v, String start, String end)
	{
		int sp = v.indexOf(start);
		int ep = v.indexOf(end, sp+start.length());
		if(end.isEmpty()) ep = v.length();
		
		return v.substring(sp+start.length(), ep);
	}

	public static String checkdir(String f)
	{
		int p = f.lastIndexOf("\\");
		int p2 = f.lastIndexOf("/");
		
		if(p2>p) p = p2;
		
		if(p>0) 
		{
			String path = f.substring(0, p);
			
			File fo = new File(path);
			
			if(!fo.exists())
			{
				boolean ismake = fo.mkdirs();
				System.out.printf("mkdirs.....(%s:%s)\n", path, ismake);
				return f;
			}
		}
		return f;
	}
	
/*	
	public static OutputStreamWriter writefile(String wfile, boolean beforedelete)
	{
		FileSystem rdfs = null;
		
		try {
			  Configuration conf = new Configuration();
			  
			  conf.set("fs.hdfs.impl", org.apache.hadoop.hdfs.DistributedFileSystem.class.getName());
			  conf.set("fs.file.impl", org.apache.hadoop.fs.LocalFileSystem.class.getName());
			  
			  if(rhdfsurl!=null && !rhdfsurl.isEmpty())
			  {
				  if(rhdfsaccount.isEmpty()) 
					  rdfs = FileSystem.get(new URI(rhdfsurl), conf);
				  else
					  rdfs = FileSystem.get(new URI(rhdfsurl), conf, rhdfsaccount);
			  }
			  else
			  {
				  if(rhdfsaccount.isEmpty()) 
					  rdfs = FileSystem.get(conf);
				  else
					  rdfs = FileSystem.get(FileSystem.getDefaultUri(conf), conf, rhdfsaccount);
			  }
			  System.out.printf("home path=[%s]\n", rdfs.getHomeDirectory());
			  
			  if(rdfs.getHomeDirectory().toString().startsWith("file:")) // local file 체크 ..
			  {
				  rdfs.close();
				  File f = new File(wfile);
				  if(beforedelete && f.exists()) f.delete();
				  FileOutputStream fileOutputStream = new FileOutputStream(wfile);
				  OutputStreamWriter out = new OutputStreamWriter(fileOutputStream, "UTF-8");
				  
				  return out;
			  }
			  
			
			  FSDataOutputStream fo = null;
			  
			  if(beforedelete && rdfs.exists(new Path(wfile))) rdfs.delete(new Path(wfile));
			  if(!rdfs.exists(new Path(wfile))) fo = rdfs.create(new Path(wfile));
			  else fo = rdfs.append(new Path(wfile));
	   		    
			  OutputStreamWriter out = new OutputStreamWriter(fo, "UTF-8");
			  
	          return out;
	 		
		}
		catch(Exception e) {}
			
		return null;
	}	
	
	public static InputStreamReader readfile(String infile)
	{
		FileSystem rdfs = null;
		
		try {
			  Configuration conf = new Configuration();
			  
			  conf.set("fs.hdfs.impl", org.apache.hadoop.hdfs.DistributedFileSystem.class.getName());
			  conf.set("fs.file.impl", org.apache.hadoop.fs.LocalFileSystem.class.getName());
			  
			  if(rhdfsurl!=null && !rhdfsurl.isEmpty())
			  {
				  if(rhdfsaccount.isEmpty()) 
					  rdfs = FileSystem.get(new URI(rhdfsurl), conf);
				  else
					  rdfs = FileSystem.get(new URI(rhdfsurl), conf, rhdfsaccount);
			  }
			  else
			  {
				  if(rhdfsaccount.isEmpty()) 
					  rdfs = FileSystem.get(conf);
				  else
					  rdfs = FileSystem.get(FileSystem.getDefaultUri(conf), conf, rhdfsaccount);
			  }
			  System.out.printf("home path=[%s]\n", rdfs.getHomeDirectory());
			  
			  if(rdfs.getHomeDirectory().toString().startsWith("file:")) // local file 시스템..
			  {
				  BufferedReader reader = new BufferedReader(new FileReader(infile));
				  return reader;
			  }
			
			  FileStatus fs = rdfs.getFileStatus(new Path(infile));
			
			  if(fs.isFile())
			  {
				  InputStream  rfs = rdfs.open(new Path(infile));
				  BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(rfs,"utf-8"));
				  return bufferedReader;
			  }
		}
		catch(Exception e) {e.printStackTrace();}
			
		return null;
	}
*/	

    String lastruntime = "";
    
    @Scheduled(cron="* * * * * *")
    public void main_timer() {
    	SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    	
    	String now = df.format(new Date());

    	if(!lastruntime.equals(now))
    	{
//    		logger.info("time={}",now);

    		lastruntime = now;
    	}
    }
    
	@RequestMapping("/") 
	@ResponseBody 
	String home() 
	{ 
		return "ankus4pass Controller"; 
	} 
	
//	/collect/file(apikey, file, 저장경로) => result, msg, 물리경로, size ; apikey를 검사후 file을 지정 하둡경로에 업로드한다.
    @RequestMapping(value = "/collect/file")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public Response collect_file(HttpServletRequest req, HttpServletResponse resp
    		, @RequestParam(value = "apikey", required=true) String apikey
    		, @RequestParam(value = "file", required=true) MultipartFile file
    		, @RequestParam(value = "path", required=true) String path) 
    {
    	Response response = new Response();
        int cnt = chk_apikey(apikey);
        if(cnt<=0)
        {
        	 response.setSuccess(false);
             response.getError().setMessage("apikey is not valid");
        }
        try {
    		path = checkdir(conf_platform_work_storage+"/"+path);
        	System.out.printf("save file=[%s]\n", path); 
        	file.transferTo(new File(path));
            response.setTotal(file.getSize());
            response.setSuccess(true);
        }
        catch(Exception e) 
        {
            response.setSuccess(false);
            response.getError().setMessage(e.getMessage());
        }
        return response;
    }
    
//    /collect/rdb(apikey, db종류, db서버, db포트, db명, 쿼리 or table, 저장table, 자동생성, 데이터추가) => result, msg, 저장레코드수 ; api key를 검사후 db처리를 수행한다.
    @RequestMapping(value = "/collect/rdb")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public Response collect_rdb(HttpServletRequest req, HttpServletResponse resp
    		, @RequestParam(value = "apikey", required=true) String apikey
    		, @RequestParam(value = "dbtype", required=true) String dbtype
    		, @RequestParam(value = "dbserver", required=true) String dbserver
    		, @RequestParam(value = "dbport", required=true) Integer dbport
    		, @RequestParam(value = "database", required=true) String database
    		, @RequestParam(value = "uid", required=true) String uid
    		, @RequestParam(value = "pwd", required=true) String pwd
    		, @RequestParam(value = "query", required=true) String query
    		, @RequestParam(value = "dbtype2", required=true) String dbtype2
    		, @RequestParam(value = "dbserver2", required=true) String dbserver2
    		, @RequestParam(value = "dbport2", required=true) Integer dbport2
    		, @RequestParam(value = "database2", required=true) String database2
    		, @RequestParam(value = "uid2", required=true) String uid2
    		, @RequestParam(value = "pwd2", required=true) String pwd2
    		, @RequestParam(value = "table", required=true) String table
    		, @RequestParam(value = "autocreate", required=true) String autocreate
    		, @RequestParam(value = "appendmode", required=true) String appendmode)   throws Exception 
    {
        Response response = new Response();
        int cnt = chk_apikey(apikey);
        if(cnt<=0)
        {
        	 response.setSuccess(false);
             response.getError().setMessage("apikey is not valid");
             return response;
        }
        Connection conn = getConnection(dbtype, dbserver, dbport, database, uid, pwd);
        if(conn==null)
        {
	       	 response.setSuccess(false);
	         response.getError().setMessage("db connect fail");
	         return response;
        }
    	ResultSet rs = getrs(conn, query);
		if(rs==null)
		{
			conn.close();
	       	response.setSuccess(false);
	        response.getError().setMessage("query("+query+") fail");
	        return response;
		}
        Connection conn2 = getConnection(dbtype2, dbserver2, dbport2, database2, uid2, pwd2);
        if(conn2==null)
        {
	       	 response.setSuccess(false);
	         response.getError().setMessage("db2 connect fail");
	         return response;
        }
		ResultSetMetaData rsmd = rs.getMetaData();
    	int numOfColumn = rsmd.getColumnCount();
    	ResultSet rs2 = getrs(conn, "select * from "+cvtname(dbtype2, table));
        if(rs2==null && autocreate.equalsIgnoreCase("true") || autocreate.equalsIgnoreCase("1"))
    	{
			String ddl = "";
			String fields = "";
	    	for (int i = 1; i <= numOfColumn; i++ ) {
			  String ColumnName = rsmd.getColumnLabel(i);
			  String ColumnType = "VARCHAR("+rsmd.getColumnDisplaySize(i)+")"; 
			  int type = rsmd.getColumnType(i);
			  switch(type)
			  {
			  case Types.NUMERIC:
				  ColumnType = "numeric("+rsmd.getPrecision(i)+","+rsmd.getScale(i)+")"; 
				  break;
			  case Types.CHAR:
			  case Types.NCHAR:
				  ColumnType = "CHAR("+rsmd.getColumnDisplaySize(i)+")"; 
				  break;
			  case Types.VARCHAR:
			  case Types.NVARCHAR:
				  ColumnType = "VARCHAR("+rsmd.getColumnDisplaySize(i)+")"; 
				  break;
			  case Types.DATE:
				  ColumnType = "date"; 
				  break;
			  case Types.DECIMAL:
				  ColumnType = "decimal("+rsmd.getPrecision(i)+","+rsmd.getScale(i)+")"; 
				  break;
			  case Types.TIME:
				  ColumnType = "time"; 
				  break;
			  case Types.TIMESTAMP:
				  ColumnType = "timestamp"; 
				  break;
			  case Types.FLOAT:
				  ColumnType = "float"; 
				  break;
			  case Types.DOUBLE:
				  ColumnType = "double"; 
				  break;
			  case Types.SMALLINT:
			  case Types.TINYINT:
			  case Types.INTEGER:
				  ColumnType = "int"; 
				  break;
			  case Types.BIGINT:
				  ColumnType = "bigint"; 
				  break;
			  default:
			  }
			  if(!fields.isEmpty()) fields += ",";
			  fields += String.format("%s %s",cvtname(dbtype2, ColumnName), ColumnType);
			  ddl = String.format("create table %s (%s)", cvtname(dbtype2, table), fields);
	    	}
	    	System.out.print(ddl+"\n");
	    	if(update(conn2, ddl)<0)
	    	{
				System.out.printf("to database table creation fail(%s)\n", cvtname(dbtype2, table));
	    	}
		}
		if(rs2!=null) rs2.close();
		if(!appendmode.equalsIgnoreCase("true") && !appendmode.equalsIgnoreCase("1"))
		{ // 추가 모드가 아닐경우에는 모든 데이터 삭제..
			if(update(conn2, "delete from "+cvtname(dbtype2, table))<0)
			{
				System.out.printf("to database table data delete fail(%s)\n", cvtname(dbtype2, table));
			}
		}
		String columns = "";
		for (int i = 1; i <= numOfColumn; i++ ) 
		{
			if (!columns.isEmpty()) columns += ",";
			columns += cvtname(dbtype2,rsmd.getColumnLabel(i));
		}
		String presql = String.format("insert into %s (%s) values ", cvtname(dbtype2, table), columns);
		int bulksize = 10000;
		long rcnt = 0;
		long pcnt = 0;
		ArrayList<String> vals = new ArrayList<String>();
		while(rs.next())
		{
			String values = "";
			for (int i = 1; i <= numOfColumn; i++ ) {
				  String value = rs.getString(i);
				  if(i!=1) values += ",";
				  if(rs.wasNull()) values += "NULL";
				  else values += "'"+cvtval(value)+"'";
			}
			vals.add("("+values+")");
			rcnt++;
			if(vals.size()>=bulksize)
			{
   				pcnt += bulkupdate(conn2,presql, vals);
				vals.clear();
			}
			if((rcnt%1000)==0) System.out.printf("%s %d/%d record process...\n", cvtname(dbtype2, table), pcnt, rcnt);
		}
		if(vals.size()>0)
		{
			pcnt += bulkupdate(conn2,presql, vals);
			vals.clear();
		}
		System.out.printf("%s %d/%d record process complete\n", cvtname(dbtype2, table), pcnt, rcnt);
	    if(rs!=null) rs.close();
	    if(conn!=null) conn.close();
	    if(conn2!=null) conn2.close();		
        response.setTotal(pcnt);
        response.setSuccess(true); 
    	return response;
    }

//    /collect/web(apikey, url, 저장경로) => result, msg, 물리경로, size ; api key를 검사후 url을 download 하여 저장한다.
    @RequestMapping(value = "/collect/web")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public Response collect_web(HttpServletRequest req, HttpServletResponse resp
    		, @RequestParam(value = "apikey", required=true) String apikey
    		, @RequestParam(value = "url", required=true) String url
    		, @RequestParam(value = "path", required=true) String path) 
    {
    	Response response = new Response();
        int cnt = chk_apikey(apikey);
        if(cnt<=0)
        {
        	 response.setSuccess(false);
             response.getError().setMessage("apikey is not valid");
             return response;
        }
        try {
    		path = checkdir(conf_platform_work_storage+"/"+path);
        	long rcnt = 0;
        	int responseCode = 0;
           	while(true)
           	{
            	String charset = "UTF-8";
               	URL obj = new URL(url);
    	        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
    	        con.setRequestMethod("GET");
    	        //add default request header
    	        con.setRequestProperty("User-Agent", "Chrome/version");
    	        con.setRequestProperty("Accept-Charset", "UTF-8");
    	        con.setRequestProperty("Content-Type", "text/plain; charset=utf-8");
    	        responseCode = con.getResponseCode();
    	        System.out.printf("URL : %s\n", url);
    	        System.out.printf("ContentType : %s\n", con.getContentType());
    	        
    	        if(responseCode==HttpsURLConnection.HTTP_MOVED_PERM || responseCode==HttpsURLConnection.HTTP_MOVED_TEMP)
    	        {
    	        	url = con.getHeaderField("Location");
    	        	continue;
    	        }
    	        charset = getsubstring(con.getContentType(),"charset=","");
    	        System.out.printf("Sending 'GET' request to charset : [%s]\n", charset);
    	        System.out.printf("Response Code : %d\n", responseCode);
    	        String inputLine;
    	        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream(), charset));
    	        FileWriter wfile = new FileWriter(path);
    	        rcnt = 0;
    	        while ((inputLine = in.readLine()) != null) 
    	        {
    	        	if(rcnt>0) inputLine = "\r\n"+inputLine;
    	        	wfile.append(inputLine);
    	        	rcnt += inputLine.length();
    	        }
    	        in.close();
    	        wfile.close();
    	        break;
           	}        	
	        response.setTotal(rcnt);
	        response.setSuccess(true);
		} catch (Exception e) {
            response.setSuccess(false);
            response.getError().setMessage(e.getMessage());
		}
        return response;
    }    

//    /collect/rest(apikey, url, post or get, header, parameter, return data type(xml,json, csv, raw) => result, msg, 물리경로, size ; api key를 검사후 url을 download 하여 저장한다.
    @RequestMapping(value = "/collect/rest")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public Response collect_rest(HttpServletRequest req, HttpServletResponse resp
    		, @RequestParam(value = "apikey", required=true) String apikey
    		, @RequestParam(value = "url", required=true) String url
    		, @RequestParam(value = "method", required=false, defaultValue="GET") String method
    		, @RequestParam(value = "header", required=false, defaultValue="") String header
    		, @RequestParam(value = "parameter", required=false, defaultValue="") String parameter
    		, @RequestParam(value = "path", required=true) String path) 
    {
    	Response response = new Response();
        int cnt = chk_apikey(apikey);
        if(cnt<=0)
        {
        	 response.setSuccess(false);
             response.getError().setMessage("apikey is not valid");
             return response;
        }
        try {
    		path = checkdir(conf_platform_work_storage+"/"+path);
        	
        	if(method.equalsIgnoreCase("POST"))
        	{
	            byte[] postDataBytes = parameter.toString().getBytes("UTF-8");

	            long rcnt = 0;
	        	int responseCode = 0;
	           	while(true)
	           	{
	            	String charset = "UTF-8";
	               	URL obj = new URL(url);
		            HttpURLConnection conn = (HttpURLConnection)obj.openConnection();
		            conn.setRequestMethod("POST");
		            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
		            conn.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
	    	        //add user request header
	    	        if(!header.isEmpty())
	                {
	                	for(String kv:header.split("&"))
	                	{
	                		int p = kv.indexOf("=");
	                		if(p>0)
	                		{
	                			String k  = kv.substring(0, p);
	                			String v = kv.substring(p+1);
	                			System.out.printf("[%s]=[%s]\n", k, v);
	            	            conn.setRequestProperty(k, v);
	                		}
	                	}
	                }	        
		            conn.setDoOutput(true);
		            conn.getOutputStream().write(postDataBytes);
	    	        responseCode = conn.getResponseCode();
	    	        System.out.printf("URL : %s\n", url);
	    	        System.out.printf("ContentType : %s\n", conn.getContentType());
	    	        
	    	        if(responseCode==HttpsURLConnection.HTTP_MOVED_PERM || responseCode==HttpsURLConnection.HTTP_MOVED_TEMP)
	    	        {
	    	        	url = conn.getHeaderField("Location");
	    	        	continue;
	    	        }
	    	        charset = getsubstring(conn.getContentType(),"charset=","");
	    	        System.out.printf("CharSet : %s\n", charset);
	    	        System.out.printf("Response Code : %d\n", responseCode);
	    	        String inputLine;
	    	        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), charset));
	    	        FileWriter wfile = new FileWriter(path);
	    	        rcnt = 0;
	    	        while ((inputLine = in.readLine()) != null) 
	    	        {
	    	        	if(rcnt>0) inputLine = "\r\n"+inputLine;
	    	        	wfile.append(inputLine);
	    	        	rcnt += inputLine.length();
	    	        }
	    	        in.close();
	    	        wfile.close();
	    	        break;
	           	}	            
		        response.setTotal(rcnt);
		        response.setSuccess(true);
	            
	        }
        	else if(method.equalsIgnoreCase("GET"))
        	{
        		String geturl = url;
        		if(!parameter.isEmpty())
        		{
	            	if(url.indexOf("?")<0) geturl = geturl+"?"+parameter;
	            	else geturl = geturl+"&"+parameter;
        		}
        		long rcnt = 0;
            	int responseCode = 0;
               	while(true)
               	{
                	String charset = "UTF-8";
                   	URL obj = new URL(geturl);
        	        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        	        // optional default is GET
        	        con.setRequestMethod("GET");
        	        //add request header 헤더를 만들어주는것.
        	        con.setRequestProperty("User-Agent", "Chrome/version");
        	        con.setRequestProperty("Accept-Charset", "UTF-8");
        	        con.setRequestProperty("Content-Type", "text/plain; charset=utf-8");
        	        if(!header.isEmpty())
                    {
                    	for(String kv:header.split("&"))
                    	{
                    		int p = kv.indexOf("=");
                    		if(p>0)
                    		{
                    			String k  = kv.substring(0, p);
                    			String v = kv.substring(p+1);
                    			System.out.printf("[%s]=[%s]\n", k, v);
                	            con.setRequestProperty(k, v);
                    		}
                    	}
                    }	        
        	        responseCode = con.getResponseCode();
        	        System.out.printf("URL : %s\n", url);
        	        System.out.printf("ContentType : %s\n", con.getContentType());
        	        
        	        if(responseCode==HttpsURLConnection.HTTP_MOVED_PERM || responseCode==HttpsURLConnection.HTTP_MOVED_TEMP)
        	        {
        	        	geturl = con.getHeaderField("Location");
        	        	continue;
        	        }
        	        charset = getsubstring(con.getContentType(),"charset=","");
        	        System.out.printf("CharSet : %s\n", charset);
        	        System.out.printf("Response Code : %d\n", responseCode);
        	        String inputLine;
        	        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream(), charset));
        	        FileWriter wfile = new FileWriter(path);
        	        rcnt = 0;
        	        while ((inputLine = in.readLine()) != null) 
        	        {
        	        	if(rcnt>0) inputLine = "\r\n"+inputLine;
        	        	wfile.append(inputLine);
        	        	rcnt += inputLine.length();
        	        }
        	        in.close();
        	        wfile.close();
        	        break;
               	}
    	        response.setTotal(rcnt);
    	        response.setSuccess(true);
        	}
		} catch (Exception e) {
            response.setSuccess(false);
            response.getError().setMessage(e.getMessage());
		}
        return response;
    }    
    
//    /save/writefile(apikey, data, 저장경로) => result, msg, 물리경로, size ; api key를 검사후 경로에 기록한다.
    @RequestMapping(value = "/save/writefile")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public Response save_writefile(HttpServletRequest req, HttpServletResponse resp
    		, @RequestParam(value = "apikey", required=true) String apikey
    		, @RequestParam(value = "data", required=true) String data
    		, @RequestParam(value = "path", required=true) String path) 
    {
    	Response response = new Response();
        int cnt = chk_apikey(apikey);
        if(cnt<=0)
        {
        	 response.setSuccess(false);
             response.getError().setMessage("apikey is not valid");
             return response;
        }
        try {
    		path = checkdir(conf_platform_engine_storage+"/"+path);
        	File f = new File(path);
        	if(f.exists()) f.delete();
        	Writer wfile = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(path, true), "UTF-8"));
        	wfile.write(data);
        	wfile.close();
        	f = new File(path);
	        response.setTotal(f.length());
	        response.setSuccess(true);
		} catch (Exception e) {
		    response.setSuccess(false);
		    response.getError().setMessage(e.getMessage());
		}
        return response;
    }    

//    /save/appendfile(apikey, data, 저장경로) => result, msg, 물리경로, size, lastsize ; api key를 검사후 경로에 추가기록한다.
    @RequestMapping(value = "/save/appendfile")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public Response save_appendfile(HttpServletRequest req, HttpServletResponse resp
    		, @RequestParam(value = "apikey", required=true) String apikey
    		, @RequestParam(value = "data", required=true) String data
    		, @RequestParam(value = "path", required=true) String path) 
    {
    	Response response = new Response();
        int cnt = chk_apikey(apikey);
        if(cnt<=0)
        {
        	 response.setSuccess(false);
             response.getError().setMessage("apikey is not valid");
             return response;
        }
        try {
    		path = checkdir(conf_platform_engine_storage+"/"+path);
        	Writer wfile = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(path, true), "UTF-8"));
        	wfile.append(data);
        	wfile.close();
        	File f = new File(path);
	        response.setTotal(f.length());
	        response.setSuccess(true);
		} catch (Exception e) {
		    response.setSuccess(false);
		    response.getError().setMessage(e.getMessage());
		}
        return response;
    }    

    
//  /save/readfile(apikey, maxsize, 저장경로) => result, msg, 물리경로, size, lastsize ; api key를 검사후 경로에 추가기록한다.
    @RequestMapping(value = "/save/readfile")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public Response save_readfile(HttpServletRequest req, HttpServletResponse resp
  		, @RequestParam(value = "apikey", required=true) String apikey
  		, @RequestParam(value = "maxsize", required=true) Long maxsize
  		, @RequestParam(value = "path", required=true) String path) 
    {
    	Response response = new Response();
    	int cnt = chk_apikey(apikey);
    	if(cnt<=0)
    	{
    		response.setSuccess(false);
    		response.getError().setMessage("apikey is not valid");
    		return response;
    	}
    	try {
    		path = checkdir(conf_platform_engine_storage+"/"+path);
    		File f = new File(path);
    		
    		if(!f.exists())
    		{
        		response.setSuccess(false);
        		response.getError().setMessage("file not found");
        		return response;
    		}
    		
    		Reader rfile = new BufferedReader(new InputStreamReader(new FileInputStream(path), "UTF-8"));
    		
    		long filesize = f.length();
    		response.setTotal(filesize);

    		char buff[] = new char[maxsize.intValue()];
    		rfile.read(buff);
    		String readstr = new String(buff); 
    		response.setLimit(readstr.length());
    		response.setObject(readstr);
    		rfile.close();
	        response.setSuccess(true);
		} catch (Exception e) {
		    response.setSuccess(false);
		    response.getError().setMessage(e.getMessage());
		}
    	return response;
    }        

//  /save/readbottomfile(apikey, maxsize, 저장경로) => result, msg, 물리경로, size, lastsize ; api key를 검사후 경로에 추가기록한다.
    @RequestMapping(value = "/save/readbottomfile")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public Response save_readbottomfile(HttpServletRequest req, HttpServletResponse resp
  		, @RequestParam(value = "apikey", required=true) String apikey
  		, @RequestParam(value = "maxsize", required=true) Long maxsize
  		, @RequestParam(value = "path", required=true) String path) 
    {
    	Response response = new Response();
    	int cnt = chk_apikey(apikey);
    	if(cnt<=0)
    	{
      	 response.setSuccess(false);
           response.getError().setMessage("apikey is not valid");
           return response;
    	}
    	try {
    		path = checkdir(conf_platform_engine_storage+"/"+path);
    		File f = new File(path);
    		
    		if(!f.exists())
    		{
        		response.setSuccess(false);
        		response.getError().setMessage("file not found");
        		return response;
    		}
    		
    		Reader rfile = new BufferedReader(new InputStreamReader(new FileInputStream(path), "UTF-8"));
    		
    		long filesize = f.length();
    		response.setTotal(filesize);
    		
    		int  offset = (int)(filesize - maxsize);
    		if(offset<0) offset = 0;
    		
    		rfile.skip(offset);

    		char buff[] = new char[maxsize.intValue()];
    		rfile.read(buff);
    		String readstr = new String(buff); 
    		response.setLimit(readstr.length());
    		response.setObject(readstr);
    		rfile.close();
	        response.setSuccess(true);
		} catch (Exception e) {
		    response.setSuccess(false);
		    response.getError().setMessage(e.getMessage());
		}
    	return response;
    }        
 
//  /save/readlinefile(apikey, maxsize, 저장경로) => result, msg, 물리경로, size, lastsize ; api key를 검사후 경로에 추가기록한다.
    @RequestMapping(value = "/save/readlinefile")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public Response save_readlinefile(HttpServletRequest req, HttpServletResponse resp
  		, @RequestParam(value = "apikey", required=true) String apikey
  		, @RequestParam(value = "linecnt", required=true) Long linecnt
  		, @RequestParam(value = "path", required=true) String path) 
    {
    	Response response = new Response();
    	int cnt = chk_apikey(apikey);
    	if(cnt<=0)
    	{
    		response.setSuccess(false);
    		response.getError().setMessage("apikey is not valid");
    		return response;
    	}
    	try {
    		path = checkdir(conf_platform_engine_storage+"/"+path);
    		File f = new File(path);
    		
    		if(!f.exists())
    		{
        		response.setSuccess(false);
        		response.getError().setMessage("file not found");
        		return response;
    		}
    		
    		BufferedReader rfile = new BufferedReader(new InputStreamReader(new FileInputStream(path), "UTF-8"));
    		
    		long filesize = f.length();
    		response.setTotal(filesize);
    		
    		String line;
    		List<Object> lines = new ArrayList<Object>(); 

    		while ((line = rfile.readLine())!=null)
    		{
    			lines.add(line);
    			if(lines.size()>=linecnt) break;
    		}
    		response.setLimit(lines.size());
    		response.setList((List<Object>)lines);
    		rfile.close();
	        response.setSuccess(true);
		} catch (Exception e) {
		    response.setSuccess(false);
		    response.getError().setMessage(e.getMessage());
		}
    	return response;
    }        

//  /save/readbottomlinefile(apikey, maxsize, 저장경로) => result, msg, 물리경로, size, lastsize ; api key를 검사후 경로에 추가기록한다.
    @RequestMapping(value = "/save/readlastlinefile")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public Response save_readlastlinefile(HttpServletRequest req, HttpServletResponse resp
  		, @RequestParam(value = "apikey", required=true) String apikey
  		, @RequestParam(value = "linecnt", required=true) Long linecnt
  		, @RequestParam(value = "path", required=true) String path) 
    {
    	Response response = new Response();
    	int cnt = chk_apikey(apikey);
    	if(cnt<=0)
    	{
      	 response.setSuccess(false);
           response.getError().setMessage("apikey is not valid");
           return response;
    	}
    	
//		System.out.printf("save_readlastlinefile...\n");
    	
    	try {
    		path = checkdir(conf_platform_engine_storage+"/"+path);
    		File f = new File(path);
    		if(!f.exists())
    		{
        		response.setSuccess(false);
        		response.getError().setMessage("file not found");
        		return response;
    		}
    		
    		Reader rfile = new BufferedReader(new InputStreamReader(new FileInputStream(path), "UTF-8"));
    		
    		long filesize = f.length();
    		response.setTotal(filesize);
    		
    		int buffsize = (int)linecnt.intValue()*255;
    		int offset = (int)(filesize - buffsize);
    		if(offset<0) offset = 0;

    		System.out.printf("buff=%d, offset=%d\n", buffsize, offset);
    		
    		rfile.skip(offset);
    		
    		char buff[] = new char[buffsize];
    		int rcnt = rfile.read(buff);
    		String readstr = new String(buff);
    		
//    		System.out.printf("readstr=[%s], buff=%d, offset=%d, rcnt=%d\n", readstr, buffsize, offset, rcnt);
    		
    		String[] lns = readstr.split("\n");
    		List<Object> lines = new ArrayList<Object>(); 
    		
    		for(int i=lns.length-1; i>=0; i--)
    		{
    			lines.add(lns[i]);
    			if(lines.size()>=linecnt) break;
    		}
    		response.setLimit(lines.size());
    		response.setList((List<Object>)lines);
    		rfile.close();
	        response.setSuccess(true);
		} catch (Exception e) {
		    response.setSuccess(false);
		    response.getError().setMessage(e.getMessage());
		    e.printStackTrace();
		}
    	return response;
    }        
    
//  /save/deletefile(apikey, 저장경로) => result, msg, 물리경로, size ; api key를 검사후 경로에 기록한다.
    @RequestMapping(value = "/save/deletefile")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public Response save_deletefile(HttpServletRequest req, HttpServletResponse resp
  		, @RequestParam(value = "apikey", required=true) String apikey
  		, @RequestParam(value = "path", required=true) String path) 
    {
    	Response response = new Response();
    	int cnt = chk_apikey(apikey);
    	if(cnt<=0)
    	{
    		response.setSuccess(false);
    		response.getError().setMessage("apikey is not valid");
    		return response;
    	}
    	try {
    		path = checkdir(conf_platform_engine_storage+"/"+path);
    		File f = new File(path);
      	  	if(f.exists())
      	  	{
      	  		response.setTotal(f.length());
      	  		f.delete(); 
      	  		response.setSuccess(true);
      	  	}
      	  	else
      	  	{
      	  		response.setTotal(-1);
      	  		response.setSuccess(false);
      	  		response.getError().setMessage("file not found");
      	  	}
    	} catch (Exception e) {
    		response.setSuccess(false);
    		response.getError().setMessage(e.getMessage());
    	}
    	return response;
    }    
    
	///save/insertrdb(apikey, dbtype, server, port, uid, pwd, database, 테이블, data) => result, msg, 물리경로, size ; api key를 검사후 경로에 기록한다.
	@RequestMapping(value = "/save/appendrdb")
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	public Response save_appendrdb(HttpServletRequest req, HttpServletResponse resp
			, @RequestParam(value = "apikey", required=true) String apikey
			, @RequestParam(value = "dbtype", required=true) String dbtype
			, @RequestParam(value = "dbserver", required=true) String dbserver
			, @RequestParam(value = "dbport", required=true) Integer dbport
			, @RequestParam(value = "database", required=true) String database
			, @RequestParam(value = "uid", required=true) String uid
			, @RequestParam(value = "pwd", required=true) String pwd
			, @RequestParam(value = "table", required=true) String table
			, @RequestParam(value = "data", required=true) String data) 
	{
		Response response = new Response();
	    int cnt = chk_apikey(apikey);
	    if(cnt<=0)
	    {
	    	response.setSuccess(false);
	    	response.getError().setMessage("apikey is not valid");
	    	return response;
	    }
	    try {
	    	cnt = 0;
		    Connection conn = getConnection(dbtype, dbserver, dbport, database, uid, pwd);
		    ObjectMapper mapper = new ObjectMapper();
		    ArrayList<HashMap<String, Object>> datas = mapper.readValue(data, new ArrayList<HashMap<String, Object>>().getClass());
		    for(HashMap<String, Object> d:datas)
		    {
		    	StringBuffer cols = new StringBuffer();
		    	StringBuffer vals = new StringBuffer(); 
		    	for (Map.Entry<String, Object> entry : d.entrySet()) 
		    	{
		    		String key = entry.getKey();
		    		Object val = entry.getValue();
		    		if(cols.length()>0) cols.append(",");
		    		cols.append(cvtname(dbtype,key));
		    		if(vals.length()>0) vals.append(",");
		    		vals.append("'"+cvtval((String)val)+"'"); 
		    	}
		    	String sql = String.format("INSERT %s (%s) VALUES (%s)", cvtname(dbtype,table), cols.toString(), vals.toString());
		    	int ucnt = update(conn, sql);
		    	if(ucnt>0) cnt+=ucnt;
		    }
		    conn.close();
			response.setTotal(cnt);
			response.setSuccess(true);
	    } catch (Exception e) {
	  	  	response.setSuccess(false);
	  	  	response.getError().setMessage(e.getMessage());
	    }
	    return response;
	}    

	///save/insertrdb(apikey, dbtype, server, port, uid, pwd, database, 테이블, data) => result, msg, 물리경로, size ; api key를 검사후 경로에 기록한다.
	@RequestMapping(value = "/save/readrdb")
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	public Response save_readrdb(HttpServletRequest req, HttpServletResponse resp
			, @RequestParam(value = "apikey", required=true) String apikey
			, @RequestParam(value = "dbtype", required=true) String dbtype
			, @RequestParam(value = "dbserver", required=true) String dbserver
			, @RequestParam(value = "dbport", required=true) Integer dbport
			, @RequestParam(value = "database", required=true) String database
			, @RequestParam(value = "uid", required=true) String uid
			, @RequestParam(value = "pwd", required=true) String pwd
			, @RequestParam(value = "query", required=true) String query) 
	{
	    resp.addHeader("Access-Control-Allow-Origin", "*");  
	    resp.setHeader("Access-Control-Allow-Headers", "origin, x-requested-with, content-type, accept");
		
		Response response = new Response();
	    int cnt = chk_apikey(apikey);
	    if(cnt<=0)
	    {
	    	response.setSuccess(false);
	    	response.getError().setMessage("apikey is not valid");
	    	return response;
	    }
	    try {
	    	cnt = 0;
		    Connection conn = getConnection(dbtype, dbserver, dbport, database, uid, pwd);
		    
		    List<Object> records = new ArrayList<Object>(); 
		    ArrayList<HashMap<String,Object>> datas = select_sql(conn, query);
		    cnt = datas.size();
		    
		    records.addAll(datas);
		    
		    conn.close();
			response.setTotal(cnt);
			response.setList(records);
			response.setSuccess(true);
	    } catch (Exception e) {
	  	  	response.setSuccess(false);
	  	  	response.getError().setMessage(e.getMessage());
	    }
	    return response;
	}  	
	
	///save/updaterdb(apikey, dbtype, server, port, uid, pwd, database, 테이블, data) => result, msg, 물리경로, size ; api key를 검사후 경로에 기록한다.
	@RequestMapping(value = "/save/updaterdb")
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	public Response save_updaterdb(HttpServletRequest req, HttpServletResponse resp
			, @RequestParam(value = "apikey", required=true) String apikey
			, @RequestParam(value = "dbtype", required=true) String dbtype
			, @RequestParam(value = "dbserver", required=true) String dbserver
			, @RequestParam(value = "dbport", required=true) Integer dbport
			, @RequestParam(value = "database", required=true) String database
			, @RequestParam(value = "uid", required=true) String uid
			, @RequestParam(value = "pwd", required=true) String pwd
			, @RequestParam(value = "table", required=true) String table
			, @RequestParam(value = "data", required=true) String data) 
	{
		Response response = new Response();
	    int cnt = chk_apikey(apikey);
	    if(cnt<=0)
	    {
	    	response.setSuccess(false);
	    	response.getError().setMessage("apikey is not valid");
	    	return response;
	    }
	    try {
	    	cnt = 0;
		    Connection conn = getConnection(dbtype, dbserver, dbport, database, uid, pwd);
		    ObjectMapper mapper = new ObjectMapper();
		    ArrayList<HashMap<String, Object>> datas = mapper.readValue(data, new ArrayList<HashMap<String, Object>>().getClass());
		    for(HashMap<String, Object> d:datas)
		    {
		    	StringBuffer wheres = new StringBuffer();
		    	StringBuffer sets = new StringBuffer(); 
		    	for (Map.Entry<String, Object> entry : d.entrySet())
		    	{
		    		String key = entry.getKey();
		    		Object val = entry.getValue();
		    		if(key.startsWith("key_")) // where item
		    		{
		    			String cond = String.format("%s='%s'", cvtname(dbtype, key.substring(4)), val);
			    		if(wheres.length()>0) wheres.append(" AND ");
		    			wheres.append(cond);
		    		}
		    		else // set item
		    		{
		    			String set = String.format("%s='%s'", cvtname(dbtype, key), val);
			    		if(sets.length()>0) sets.append(" , ");
		    			sets.append(set);
		    		}
		    	}
		    	String sql = String.format("UPDATE %s SET %s WHERE %s", cvtname(dbtype, table), sets.toString(), wheres.toString());
		    	int ucnt = update(conn, sql);
		    	if(ucnt>0) cnt+=ucnt;
		    }
		    conn.close();
			response.setTotal(cnt);
			response.setSuccess(true);
	    } catch (Exception e) {
	  	  	response.setSuccess(false);
	  	  	response.getError().setMessage(e.getMessage());
	    }
	    return response;
	}    

	///save/deleterdb(apikey, dbtype, server, port, uid, pwd, database, 테이블, data) => result, msg, 물리경로, size ; api key를 검사후 경로에 기록한다.
	@RequestMapping(value = "/save/deleterdb")
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	public Response save_deleterdb(HttpServletRequest req, HttpServletResponse resp
			, @RequestParam(value = "apikey", required=true) String apikey
			, @RequestParam(value = "dbtype", required=true) String dbtype
			, @RequestParam(value = "dbserver", required=true) String dbserver
			, @RequestParam(value = "dbport", required=true) Integer dbport
			, @RequestParam(value = "database", required=true) String database
			, @RequestParam(value = "uid", required=true) String uid
			, @RequestParam(value = "pwd", required=true) String pwd
			, @RequestParam(value = "table", required=true) String table
			, @RequestParam(value = "data", required=true) String data) 
	{
		Response response = new Response();
	    int cnt = chk_apikey(apikey);
	    if(cnt<=0)
	    {
	    	response.setSuccess(false);
	    	response.getError().setMessage("apikey is not valid");
	    	return response;
	    }
	    try {
	    	cnt = 0;
		    Connection conn = getConnection(dbtype, dbserver, dbport, database, uid, pwd);
		    ObjectMapper mapper = new ObjectMapper();
		    ArrayList<HashMap<String, Object>> datas = mapper.readValue(data, new ArrayList<HashMap<String, Object>>().getClass());
		    for(HashMap<String, Object> d:datas)
		    {
		    	StringBuffer wheres = new StringBuffer();
		    	for (Map.Entry<String, Object> entry : d.entrySet()) 
		    	{
		    		String key = entry.getKey();
		    		Object val = entry.getValue();
		    		if(key.startsWith("key_"))
		    		{
		    			String cond = String.format("%s='%s'", cvtname(dbtype, key.substring(4)), val);
			    		if(wheres.length()>0) wheres.append(" AND ");
		    			wheres.append(cond);
		    		}
		    	}
		    	String sql = String.format("DELETE FROM %s WHERE %s", cvtname(dbtype, table), wheres.toString());
		    	int ucnt = update(conn, sql);
		    	if(ucnt>0) cnt+=ucnt;
		    }
		    conn.close();
			response.setTotal(cnt);
			response.setSuccess(true);
	    } catch (Exception e) {
	  	  	response.setSuccess(false);
	  	  	response.getError().setMessage(e.getMessage());
	    }
	    return response;
	}   
	
	///processing/standardization(apikey, inputfile, method(csv, json, tsv, xml, tokenizer), rule, outputcsv) => 정형화
	@RequestMapping(value = "/processing/list")
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	public Response processing_list(HttpServletRequest req, HttpServletResponse resp
			, @RequestParam(value = "apikey", required=true) String apikey) 
	{
		Response response = new Response();
	    int cnt = chk_apikey(apikey);
	    if(cnt<=0)
	    {
	    	response.setSuccess(false);
	    	response.getError().setMessage("apikey is not valid");
	    	return response;
	    }
	    
	    chk_algorithm();
		
		if(m_metainfos!=null)
		{
	    	response.setSuccess(true);
			List<Object> lst = response.getList();
			// lst.addAll(m_metainfos);
			for(HashMap<String, Object> i: m_metainfos)
			{
				if("processing".equalsIgnoreCase((String)i.get("apptype"))) lst.add(i);
			}
			response.setLimit(lst.size());
			response.setTotal(lst.size());
/*			
			try {
				ObjectMapper mapper = new ObjectMapper();
				mapper.writeValue(resp.getOutputStream(), response);
				
				return null;
				
			} catch (JsonGenerationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JsonMappingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
*/				
		}
		else
		{
	    	response.setSuccess(false);
	    	response.getError().setMessage("algorithm is nothing");
		}
			
	    return response;
	}   

	///processing/standardization(apikey, inputfile, method(csv, json, tsv, xml, tokenizer), rule, outputcsv) => 정형화
	@RequestMapping(value = "/processing/{algorithm}")
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	public Response processing_common(HttpServletRequest req, HttpServletResponse resp
			, @RequestParam(value = "apikey", required=true) String apikey
			, @PathVariable String algorithm
			, @RequestParam Map<String,Object> parameters
			) 
	{
		Response response = new Response();
	    int cnt = chk_apikey(apikey);
	    if(cnt<=0)
	    {
	    	response.setSuccess(false);
	    	response.getError().setMessage("apikey is not valid");
	    	return response;
	    }
	    
	    chk_algorithm();

	    HashMap<String, Object> find_item = null;  
	    if(m_metainfos!=null) for(HashMap<String, Object> item:m_metainfos)
	    {
	    	if(algorithm.equals(item.get("classname")))
	    	{
	    		find_item = item;
	    		break;
	    	}
	    }
	    
	    if(find_item==null)
	    {
	    	response.setSuccess(false);
	    	response.getError().setMessage(algorithm+" algorithm is not installed");
		    System.out.printf("algorithm %s not found\n", algorithm);
	    	return response;
	    }
	    
	    String path = (String)find_item.get("path");

	    ArrayList<HashMap<String, Object>>params = (ArrayList<HashMap<String, Object>>)find_item.get("params");
	    
	    if(params==null)
	    {
	    	response.setSuccess(false);
	    	response.getError().setMessage(algorithm+" algorithm parameter info not found");
		    System.out.printf("algorithm %s parameter info not found\n", algorithm);
	    	return response;
	    }
	    
	    StringBuffer paramstr = new StringBuffer(); 
	    
	    for(HashMap<String, Object>param:params)
	    {
	    	String parname = (String)param.get("name");
	    	String required = (String)param.get("required");
	    	String parval = (String)parameters.get(parname);
	    	
	    	if("Y".equals(required) && parval==null) // 필수 파라미터 인데 값이 없으면 ...
	    	{
		    	response.setSuccess(false);
		    	response.getError().setMessage(algorithm+" parameter missing");
			    System.out.printf("algorithm=%s parameter %s missing\n", algorithm, parname);
		    	return response;
	    	}
	    	
	    	if(parval!=null && parname!=null)
	    	{
	    		paramstr.append(" -"+parname);
	    		paramstr.append(" \""+parval+"\"");
	    	}
	    }
	    
	    String cmd = String.format("java -jar %s %s", path, paramstr.toString());
	    if("mapreduce".equalsIgnoreCase((String)find_item.get("runtype")))
	    {
		    cmd = String.format("hadoop jar %s %s", path, paramstr.toString());
	    }
	    
	    if(m_worklist==null) m_worklist = new ArrayList<workThread>();
	    
	    workThread work = new workThread(cmd, 100, "processing");
	    
	    m_worklist.add(work);
	    
	    work.start();
	    
	    System.out.printf("algorithm=%s, cmd=[%s], cmdcnt=%d\n", algorithm, cmd, m_worklist.size());

    	response.setSuccess(true);
    	response.setObject(cmd);
	    
	    return response;
	}   
	
	///processing/discretization(apikey, inputcsvfile, targetindex, classindex, outputcsv) => 이산화
	@RequestMapping(value = "/processing/discretization")
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	public Response processing_discretization(HttpServletRequest req, HttpServletResponse resp
			, @RequestParam(value = "apikey", required=true) String apikey
			, @RequestParam(value = "inputcsvfile", required=true) String inputcsvfile
			, @RequestParam(value = "targetindex", required=true) String targetindex
			, @RequestParam(value = "classindex", required=true) String classindex
			, @RequestParam(value = "outputcsv", required=true) String outputcsv) 
	{
		Response response = new Response();
	    int cnt = chk_apikey(apikey);
	    if(cnt<=0)
	    {
	    	response.setSuccess(false);
	    	response.getError().setMessage("apikey is not valid");
	    	return response;
	    }

	    return response;
	}   

	///processing/normalization(apikey, inputcsvfile, targetindex, exceptionindex, printall, outputcsv) => 정규화
	@RequestMapping(value = "/processing/normalization")
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	public Response processing_normalization(HttpServletRequest req, HttpServletResponse resp
			, @RequestParam(value = "apikey", required=true) String apikey
			, @RequestParam(value = "inputcsvfile", required=true) String inputcsvfile
			, @RequestParam(value = "targetindex", required=true) String targetindex
			, @RequestParam(value = "exceptionindex", required=true) String exceptionindex
			, @RequestParam(value = "printall", required=true) String printall
			, @RequestParam(value = "outputcsv", required=true) String outputcsv) 
	{
		Response response = new Response();
	    int cnt = chk_apikey(apikey);
	    if(cnt<=0)
	    {
	    	response.setSuccess(false);
	    	response.getError().setMessage("apikey is not valid");
	    	return response;
	    }

	    return response;
	}   

	///processing/etl(apikey, inputcsvfile, 	method(Column Extractor/Filter Include/Filter Exclude/Replace/Categorization/Rows sort), ruletext, sort, sortindex, outputcsvfile) => ETL
	@RequestMapping(value = "/processing/etl")
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	public Response processing_etl(HttpServletRequest req, HttpServletResponse resp
			, @RequestParam(value = "apikey", required=true) String apikey
			, @RequestParam(value = "inputcsvfile", required=true) String inputcsvfile
			, @RequestParam(value = "method", required=true) String method
			, @RequestParam(value = "ruletext", required=true) String ruletext
			, @RequestParam(value = "sort", required=true) String sort
			, @RequestParam(value = "sortindex", required=true) String sortindex
			, @RequestParam(value = "outputcsv", required=true) String outputcsv) 
	{
		Response response = new Response();
	    int cnt = chk_apikey(apikey);
	    if(cnt<=0)
	    {
	    	response.setSuccess(false);
	    	response.getError().setMessage("apikey is not valid");
	    	return response;
	    }
	    return response;
	}   
	
	///processing/standardization(apikey, inputfile, method(csv, json, tsv, xml, tokenizer), rule, outputcsv) => 정형화
	@RequestMapping(value = "/processing/standardization")
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	public Response processing_standardization(HttpServletRequest req, HttpServletResponse resp
			, @RequestParam(value = "apikey", required=true) String apikey
			, @RequestParam(value = "inputfile", required=true) String inputfile
			, @RequestParam(value = "method", required=true) String method
			, @RequestParam(value = "rule", required=true) String rule
			, @RequestParam(value = "outputcsv", required=true) String outputcsv) 
	{
		Response response = new Response();
	    int cnt = chk_apikey(apikey);
	    if(cnt<=0)
	    {
	    	response.setSuccess(false);
	    	response.getError().setMessage("apikey is not valid");
	    	return response;
	    }
	    return response;
	}   

	///processing/standardization(apikey, inputfile, method(csv, json, tsv, xml, tokenizer), rule, outputcsv) => 정형화
	@RequestMapping(value = "/analysis/list")
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	public Response analysis_list(HttpServletRequest req, HttpServletResponse resp
			, @RequestParam(value = "apikey", required=true) String apikey) 
	{
		Response response = new Response();
	    int cnt = chk_apikey(apikey);
	    if(cnt<=0)
	    {
	    	response.setSuccess(false);
	    	response.getError().setMessage("apikey is not valid");
	    	return response;
	    }
	    
	    chk_algorithm();
		
		if(m_metainfos!=null)
		{
	    	response.setSuccess(true);
			List<Object> lst = response.getList();
			// lst.addAll(m_metainfos);
			for(HashMap<String, Object> i: m_metainfos)
			{
				if("analysis".equalsIgnoreCase((String)i.get("apptype"))) {
					lst.add(i);
				}
			}
			response.setLimit(lst.size());
			response.setTotal(lst.size());
			
			
/*			
			try {
				ObjectMapper mapper = new ObjectMapper();
				mapper.writeValue(resp.getOutputStream(), response);
				
				return null;
				
			} catch (JsonGenerationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JsonMappingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
*/				
		}
		else
		{
	    	response.setSuccess(false);
	    	response.getError().setMessage("algorithm is nothing");
		}
			
	    return response;
	}    

	///processing/standardization(apikey, inputfile, method(csv, json, tsv, xml, tokenizer), rule, outputcsv) => 정형화
	@RequestMapping(value = "/analysis/{algorithm}")
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	public Response analysis_common(HttpServletRequest req, HttpServletResponse resp
			, @RequestParam(value = "apikey", required=true) String apikey
			, @PathVariable String algorithm
			, @RequestParam Map<String,Object> parameters
			) 
	{
		Response response = new Response();
	    int cnt = chk_apikey(apikey);
	    if(cnt<=0)
	    {
	    	response.setSuccess(false);
	    	response.getError().setMessage("apikey is not valid");
	    	return response;
	    }
	    
	    chk_algorithm();

	    HashMap<String, Object> find_item = null;  
	    if(m_metainfos!=null) for(HashMap<String, Object> item:m_metainfos)
	    {
	    	if(algorithm.equals(item.get("classname")))
	    	{
	    		find_item = item;
	    		break;
	    	}
	    }
	    
	    if(find_item==null)
	    {
	    	response.setSuccess(false);
	    	response.getError().setMessage(algorithm+" algorithm is not installed");
		    System.out.printf("algorithm %s not found\n", algorithm);
	    	return response;
	    }
	    
	    String path = (String)find_item.get("path");

	    ArrayList<HashMap<String, Object>>params = (ArrayList<HashMap<String, Object>>)find_item.get("params");
	    
	    if(params==null)
	    {
	    	response.setSuccess(false);
	    	response.getError().setMessage(algorithm+" algorithm parameter info not found");
		    System.out.printf("algorithm %s parameter info not found\n", algorithm);
	    	return response;
	    }
	    
	    StringBuffer paramstr = new StringBuffer(); 
	    
	    for(HashMap<String, Object>param:params)
	    {
	    	String parname = (String)param.get("name");
	    	String required = (String)param.get("required");
	    	String parval = (String)parameters.get(parname);
	    	
	    	if("Y".equals(required) && parval==null) // 필수 파라미터 인데 값이 없으면 ...
	    	{
		    	response.setSuccess(false);
		    	response.getError().setMessage(algorithm+" parameter missing");
			    System.out.printf("algorithm=%s parameter %s missing\n", algorithm, parname);
		    	return response;
	    	}
	    	
	    	if(parval!=null && parname!=null)
	    	{
	    		paramstr.append(" -"+parname);
	    		paramstr.append(" \""+parval+"\"");
	    	}
	    }
	    
	    String cmd = String.format("java -jar %s %s", path, paramstr.toString());
	    if("mapreduce".equalsIgnoreCase((String)find_item.get("runtype")))
	    {
		    cmd = String.format("hadoop jar %s %s", path, paramstr.toString());
	    }
	    
	    if(m_worklist==null) m_worklist = new ArrayList<workThread>();
	    
	    workThread work = new workThread(cmd, 100, "analysis");
	    
	    m_worklist.add(work);
	    
	    work.start();
	    
	    System.out.printf("algorithm=%s, cmd=[%s], cmdcnt=%d\n", algorithm, cmd, m_worklist.size());

    	response.setSuccess(true);
    	response.setObject(cmd);
	    
	    return response;
	}   

	///processing/standardization(apikey, inputfile, method(csv, json, tsv, xml, tokenizer), rule, outputcsv) => 정형화
	@RequestMapping(value = "/visualization/list")
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	public Response visualization_list(HttpServletRequest req, HttpServletResponse resp
			, @RequestParam(value = "apikey", required=true) String apikey) 
	{
		Response response = new Response();
	    int cnt = chk_apikey(apikey);
	    if(cnt<=0)
	    {
	    	response.setSuccess(false);
	    	response.getError().setMessage("apikey is not valid");
	    	return response;
	    }
	    
	    chk_algorithm();
		
		if(m_metainfos!=null)
		{
	    	response.setSuccess(true);
			List<Object> lst = response.getList();
			// lst.addAll(m_metainfos);
			for(HashMap<String, Object> i: m_metainfos)
			{
				if("visualization".equalsIgnoreCase((String)i.get("apptype"))) lst.add(i);
			}
			response.setLimit(lst.size());
			response.setTotal(lst.size());
/*			
			try {
				ObjectMapper mapper = new ObjectMapper();
				mapper.writeValue(resp.getOutputStream(), response);
				
				return null;
				
			} catch (JsonGenerationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JsonMappingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
*/				
		}
		else
		{
	    	response.setSuccess(false);
	    	response.getError().setMessage("algorithm is nothing");
		}
			
	    return response;
	}
	
	
	
/*	///processing/standardization(apikey, inputfile, method(csv, json, tsv, xml, tokenizer), rule, outputcsv) => 정형화
	@RequestMapping(value = "/visualization/pie")
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	public Response visualization_pie(HttpServletRequest req, HttpServletResponse resp
			, @RequestParam(value = "apikey", required=true) String apikey
			, @RequestParam(value = "data", required=true) String data
			, @RequestParam(value = "div", required=true) String div
			, @RequestParam(value = "caption", required=true) String caption
			, @RequestParam(value = "parameters", required=false) Map<String, Object> parameters) 
	{
		Response response = new Response();
	    int cnt = memberService.existUsername(apikey);
	    if(cnt<=0)
	    {
	    	response.setSuccess(false);
	    	response.getError().setMessage("apikey is not valid");
	    	return response; 
	    }
	    
	    resp.addHeader("Access-Control-Allow-Origin", "*");  
	    resp.setHeader("Access-Control-Allow-Headers", "origin, x-requested-with, content-type, accept");
	    
	    
	    // server url...
	    String svrurl = "http://"+req.getServerName()+":"+req.getLocalPort();

	    // data is json object or csv
	    
	    StringBuffer sb = new StringBuffer();
	    
	    sb.append("<link type=text/css rel=stylesheet href="+svrurl+"/resources/lib/main/nvd3/css/nv.d3.min.css/>\n"); 
	    sb.append("<script src="+svrurl+"/resources/lib/main/d3/js/d3.v3.min.js></script>");
	    sb.append("<script src="+svrurl+"/resources/lib/main/nvd3/js/nv.d3.min.js></script>");
	    sb.append("<script>");
	    sb.append("function pieview(obj, data, caption)");
	    sb.append("{");
	    sb.append("var svg = '" + svgid + "';");
	    sb.append("$(obj).html('<h4>'+caption+'</h4><svg id='+svg+' style=\"height:100%;width:100%\"> </svg>');");
	    sb.append("    	nv.addGraph(function() {");
	    sb.append("    	  var chart = nv.models.pieChart()");
	    sb.append("    	      .x(function(d) { return d.label })");
	    sb.append("    	      .y(function(d) { return d.value })");
	    sb.append("    	      .showLabels(true);");
	    sb.append("    	    d3.select('#'+svg)");
	    sb.append("    	        .datum(data)");
	    sb.append("    	        .transition().duration(350)");
	    sb.append("    	        .call(chart);");
	    sb.append("    	  return chart;");
	    sb.append("    	});");
	    sb.append("}");
	    sb.append("pieview('#"+div+"', "+data+", '"+caption+"');");
	    sb.append("</script>");
	    response.setSuccess(true);
	    response.setObject(sb.toString());
	    
	    System.out.printf("script=[%s]\n", sb.toString());
	    

	    return response;
	}	*/
	
	
	///processing/standardization(apikey, inputfile, method(csv, json, tsv, xml, tokenizer), rule, outputcsv) => 정형화
	@RequestMapping(value = "/visualization/pie")
//	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	public String visualization_pie(HttpServletRequest req, HttpServletResponse resp
			, @RequestParam(value = "apikey", required=true) String apikey
			, @RequestParam(value = "data", required=true) String data
			, @RequestParam(value = "div", required=true) String div
			, @RequestParam(value = "caption", required=true) String caption
//			, @RequestParam(value = "parameters", required=false) Map<String, Object> parameters
			) 
	{
	    int cnt = chk_apikey(apikey);
	    if(cnt<=0)
	    {
	    	resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
	    	return ""; 
	    }
	    
	    resp.addHeader("Access-Control-Allow-Origin", "*");  
	    resp.setHeader("Access-Control-Allow-Headers", "origin, x-requested-with, content-type, accept");
	    
	    // server url...
	    String svrurl = "http://"+req.getServerName()+":"+req.getLocalPort();

	    // data is json object or csv
	    
	    StringBuffer sb = new StringBuffer();
	    
	    String svgid = String.format("svg%f",Math.random()).replace(".", "");
	    
	    sb.append("<h4>"+caption+"</h4><svg id='"+svgid+"' style='height:100%;width:100%'> </svg>\n");
//	    sb.append("<link type=text/css rel=stylesheet href='"+svrurl+"/resources/lib/main/nvd3/css/nv.d3.min.css'/>\n"); 
//	    sb.append("<script src='"+svrurl+"/resources/lib/main/d3/js/d3.v3.min.js'></script>\n");
//	    sb.append("<script src='"+svrurl+"/resources/lib/main/nvd3/js/nv.d3.min.js'></script>\n");
	    
	    sb.append("<script>\n");
	    sb.append("var svg = '" + svgid + "';");
	    sb.append("    	nv.addGraph(function() {");
	    sb.append("    	  var chart = nv.models.pieChart()");
	    sb.append("    	      .x(function(d) { return d.label })");
	    sb.append("    	      .y(function(d) { return d.value })");
	    sb.append("    	      .showLabels(true);");
	    sb.append("    	    d3.select('#'+svg)");
	    sb.append("    	        .datum("+data+")");
	    sb.append("    	        .transition().duration(350)");
	    sb.append("    	        .call(chart);");
	    sb.append("    	  return chart;");
	    sb.append("    	});");
	    sb.append("\n</script>");
	    
//	    System.out.printf("script=[%s]\n", sb.toString());

    	resp.setStatus(HttpServletResponse.SC_OK);
	    return sb.toString();
	}		
	
	
	
	
	///processing/standardization(apikey, inputfile, method(csv, json, tsv, xml, tokenizer), rule, outputcsv) => 정형화
	@RequestMapping(value = "/visualization/{visualization}")
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	public Response visualization_common(HttpServletRequest req, HttpServletResponse resp
			, @RequestParam(value = "apikey", required=true) String apikey
			, @PathVariable String visualization
			, @RequestParam(value = "parameters", required=true) Map<String, Object> parameters) 
	{
		Response response = new Response();
	    int cnt = chk_apikey(apikey);
	    if(cnt<=0)
	    {
	    	response.setSuccess(false);
	    	response.getError().setMessage("apikey is not valid");
	    	return response;
	    }

	    return response;
	}
	
	
	///processing/standardization(apikey, inputfile, method(csv, json, tsv, xml, tokenizer), rule, outputcsv) => 정형화
	@RequestMapping(value = "/work/list")
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	public Response work_list(HttpServletRequest req, HttpServletResponse resp
			, @RequestParam(value = "apikey", required=true) String apikey) 
	{
		Response response = new Response();
	    int cnt = chk_apikey(apikey);
	    if(cnt<=0)
	    {
	    	response.setSuccess(false);
	    	response.getError().setMessage("apikey is not valid");
	    	return response;
	    }
	    
		if(m_worklist!=null)
		{
	    	response.setSuccess(true);
			List<Object> lst = response.getList();
			// lst.addAll(m_metainfos);
			for(workThread i: m_worklist)
			{
				lst.add(i);
			}
			response.setLimit(lst.size());
			response.setTotal(lst.size());
		}
		else
		{
	    	response.setSuccess(false);
	    	response.getError().setMessage("workthread is nothing");
		}
			
	    return response;
	}    	
	
	// work list...
	ArrayList<workThread> m_worklist  = null;
	
	// meta loading...
	ArrayList<HashMap<String, Object>> m_metainfos = null; // 2018-06-01 : preloading...

	void chk_algorithm() // 알고리즘 로딩
	{
		String path = conf_cache_path;
		m_metainfos = readmetainfos(path);
	}
	
	private ArrayList<HashMap<String, Object>> readmetainfos(String folder)
    {
 		File path  = new File(folder);
        ObjectMapper mapper = new ObjectMapper();
		ArrayList<HashMap<String, Object>> algorithms = new ArrayList<HashMap<String, Object>>(); 
		
		File[] files = path.isDirectory() ? path.listFiles(): new File[]{path};
		for(File f:files)
		{
			if(f.isFile())
			{
				String fname = f.getAbsolutePath();
				String ext = "";
				int p = fname.lastIndexOf(".");
				if(p>=0) ext = fname.substring(p+1);

				if(ext.equalsIgnoreCase("jar"))
				{
					long flength = f.length();
					long lastmodified = f.lastModified();
					
					// jar 파일이 이미 로딩 되어 있고 파일크기와 수정일자가 동일하면  재로딩 하지않고 이미 로딩된 정보 사용
					boolean loaded = false;
					if(m_metainfos!=null)
					{
						for(HashMap<String, Object> info:m_metainfos)
						{
							if(fname.equals(info.get("path")) && ((Long)info.get("flength"))==flength && ((Long)info.get("lastmodified"))==lastmodified) 
							{
								loaded = true;
								break;
							}
						}
					}

					if(loaded)
					{	// 기존 로딩 정보 사용
						for(HashMap<String, Object> info:m_metainfos)
						{
							if(fname.equals(info.get("path"))) algorithms.add(info);
						}
						continue;
					}
					
					try {
						byte[] meta = readfile(fname, "res/appinfo.json");
						if(meta==null || meta.length<1) continue;
	                    HashMap<String, Object> metainfo = mapper.readValue(new String(meta), new HashMap<String, Object>().getClass());
	                    
	                    for(HashMap<String, Object> ainfo:(ArrayList<HashMap<String, Object>>)metainfo.get("applist"))
	                    {
	                    	ainfo.put("path", fname);
	                    	ainfo.put("author", metainfo.get("author"));
	                    	ainfo.put("create", metainfo.get("create"));
	                    	ainfo.put("packagename", metainfo.get("packagename"));
	                    	ainfo.put("flength", flength);
	                    	ainfo.put("lastmodified", lastmodified);
	                    	
	                    	algorithms.add(ainfo);
	                    }

					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}				
			}
		}

		return algorithms;
    }    
    
    private byte[] readfile(String zipFilePath, String fname) throws IOException {
        ZipInputStream zipIn = new ZipInputStream(new FileInputStream(zipFilePath));
        
        ZipEntry entry = zipIn.getNextEntry();
        // iterates over entries in the zip file
        
        byte[] read = null;
        
        if(fname.startsWith("/")) fname = fname.substring(1);
        
        while (entry != null) {
            if (!entry.isDirectory()) {
                // if the entry is a file, extracts it
            	if(entry.getName().equals(fname))
            	{
            		read = extractFileRead(zipIn);
            		break;
            	}
            }
            zipIn.closeEntry();
            entry = zipIn.getNextEntry();
        }
        zipIn.close();
        return read;
    	
    }
    
    private byte[] extractFileRead(ZipInputStream zipIn) throws IOException {
    	ByteArrayOutputStream bs = new ByteArrayOutputStream();
        
        byte[] bytesIn = new byte[8192];
        int read = 0;
        while ((read = zipIn.read(bytesIn)) != -1) {
        	bs.write(bytesIn, 0, read);
        }
        return bs.toString("UTF-8").getBytes();
    }	
 	
	
}
