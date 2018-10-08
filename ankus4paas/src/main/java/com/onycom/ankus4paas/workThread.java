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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;

public class workThread extends Thread {
//	private static final int BUFFER_SIZE = 4096;
	public String status = "";
	public Date starttime = null;
	public Date endtime = null;
	
	public int exitcode = -9999; 
	
	public String worktype = "";
	public String cmd = null;
	public String lastlog = "";
	public String errorlog = "";
	int lastnlines  = -1;
	public workThread(String cmd, int lastnlines, String worktype) {
		super();
		this.cmd = cmd;
		this.lastnlines = lastnlines; 
		this.worktype = worktype;
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		try {
			
			StringBuffer buff = new StringBuffer();
			
			status = "run";
			starttime = new Date();
			Process p = Runtime.getRuntime().exec(cmd);
			final InputStream is = p.getInputStream();
			OutputStream os = p.getOutputStream();
			InputStream es = p.getErrorStream();
			
			final ArrayList<String> logs = new ArrayList<String>();
			
			Thread ith = new Thread() {

				@Override
				public void run() {
					
					String line;
					BufferedReader reader = new BufferedReader(new InputStreamReader(is));
					
					try{
						while ((line = reader.readLine())!= null){
							logs.add(line);
							if(lastnlines>0 && logs.size()>lastnlines) logs.remove(0);
						}
						
					}
					catch(Exception e) { System.out.println("is error...");}
					super.run();
				}
				
			};
			ith.start();

			System.out.println("WAIT-START...");
			p.waitFor();
			System.out.println("WAIT-END...");
			
			exitcode = p.exitValue();

			status = "end";
			endtime = new Date();
			
			ith.stop();
			
			for(String l:logs) buff.append(l+"\n");
			lastlog = buff.toString();
			
			if(exitcode!=0)
			{
				buff = new StringBuffer();
				
				String line;
				BufferedReader reader = new BufferedReader(new InputStreamReader(es));
				while ((line = reader.readLine())!= null) buff.append(line+"\n");
	
				reader = new BufferedReader(new InputStreamReader(es));
				while ((line = reader.readLine())!= null) buff.append(line+"\n");
	
				errorlog = buff.toString();
			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		super.run();
	}
	
/*	

	public static String exec_shell(String cmd)
	{
		try {
			
			StringBuffer buff = new StringBuffer();
			Process p = Runtime.getRuntime().exec(cmd);
			final InputStream is = p.getInputStream();
			OutputStream os = p.getOutputStream();
			InputStream es = p.getErrorStream();
			
			Thread ith = new Thread() {

				@Override
				public void run() {
					byte[] buff = new byte[8192];
					try{
					while(is.read(buff)>0){}
					}
					catch(Exception e) { System.out.println("is error...");}
					super.run();
				}
				
			};
			ith.start();

			p.waitFor();
			
			ith.stop();

			String line;
			BufferedReader reader = new BufferedReader(new InputStreamReader(is));
			while ((line = reader.readLine())!= null) buff.append(line+"\n");

			reader = new BufferedReader(new InputStreamReader(es));
			while ((line = reader.readLine())!= null) buff.append(line+"\n");
			
			return buff.toString();
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "";
	}
	
	private static String getSubstring(String v, String fromstr, String endstr)
	{
		int spos, epos;
		if(fromstr.equals(""))
		{
			spos = v.indexOf(endstr);
			if(spos<0) return "";
			else return v.substring(0, spos);
		}
		spos = v.indexOf(fromstr);
		if (spos<0) return "";
		v = v.substring(spos+fromstr.length());
		if(endstr.equals("")) return v;
		epos = v.indexOf(endstr);
		if (epos<0) return v;
		else return v.substring(0, epos);
	}
	
	public static String exec_shell_custom(String cmd)
	{
		try {
			StringBuffer buff = new StringBuffer();
			
			Process p = Runtime.getRuntime().exec(cmd);
			final InputStream is = p.getInputStream();
			final InputStream es = p.getErrorStream();
			OutputStream os = p.getOutputStream();
			
			Thread ith = new Thread() {

				@Override
				public void run() {
					byte[] buff = new byte[8192];
					try{
					while(is.read(buff)>0){}
					}
					catch(Exception e) { System.out.println("is error...");}
					super.run();
				}
				
			};
			ith.start();
			
			Thread eth = new Thread() {

				@Override
				public void run() {
					byte[] buff = new byte[8192];
					try{
					while(es.read(buff)>0){}
					}
					catch(Exception e) { System.out.println("is error...");}
					super.run();
				}
				
			};
			eth.start();
			

			p.waitFor();
			
			ith.stop();
			eth.stop();
			
			byte[] b = new byte[BUFFER_SIZE];
			while(true)
			{
			   int cnt = is.read(b);
			   if(cnt<=0) break;
			   buff.append(new String(b, 0, cnt));
			}
			return buff.toString();
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "";
	}	
	
	*/

	
}
