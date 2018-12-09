package com.onycom.dataManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.hadoop.conf.Configuration;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.PeriodicTrigger;

import com.google.gson.Gson;
import com.onycom.mesagehandler.ConsummerProper;
import com.onycom.mesagehandler.TopicManager;

import kafka.javaapi.producer.Producer;
import kafka.producer.ProducerConfig;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import kafka.consumer.Consumer;
import kafka.consumer.ConsumerConfig;
import kafka.consumer.KafkaStream;
import kafka.javaapi.consumer.ConsumerConnector;
import kafka.message.MessageAndMetadata;

public class ProcessManager {
	
	private ThreadPoolTaskScheduler scheduler;
	ConsummerProper consummerProper;
	public ProcessManager() {
		consummerProper = new ConsummerProper();
		startScheduler();
	}
	public void stopScheduler() {
        scheduler.shutdown();
    } 
	
	public void startScheduler() {
        scheduler = new ThreadPoolTaskScheduler();
        scheduler.initialize();
        // 스케쥴러가 시작되는 부분 
        scheduler.schedule(getRunnable(), getTrigger());
    }
	
    private Runnable getRunnable(){
        return () -> {
        	Process();
        };
    }
    
    private Trigger getTrigger() {
        // 작업 주기 설정 
        return new PeriodicTrigger(1, TimeUnit.SECONDS);
    }
    
	@SuppressWarnings("deprecation")
	public void Process() {
		
		KafkaConsumer<String, String> consumer = consummerProper.getConsumer();
		consumer.subscribe(Arrays.asList("MLREQUEST"));
		
		while (true) {
		    ConsumerRecords<String, String> records = consumer.poll(500);
		    for (ConsumerRecord<String, String> record : records) {
		      switch (record.topic()) {
		        case "MLREQUEST":
		        	Gson gson = new Gson();
		        	
		        	System.out.println("KEY:"+record.key());
		        	System.out.println("VALUE:"+ record.value());
		        	System.out.println(record.toString());
		        	
		        	MethodParm mParam = new MethodParm();
		        	mParam = gson.fromJson(record.value(), MethodParm.class);
		        	String method = mParam.getMethod();
		        	switch(method) {
		        	case "sh":
		        		List<Pair<String, String>> methodParamList = mParam.getMethod_param();
		        		for(Pair<String, String> methodParam :methodParamList) {
			        		String mName = methodParam.getLeft();
			        		String mValue = methodParam.getLeft();
			        		
			        		List<String> commandList = new ArrayList<String>();
			        		commandList.add("sh");
			        		commandList.add(mName);
			        		commandList.add(mValue);
			        		String[] cmdListString = commandList.toArray(new String[commandList.size()]);
			        		try {
								byCommonsExec(cmdListString);
							} catch (Exception e) {
								e.toString();
							}
		        		}
		        	}
		        	
		        	break;
		        default:
		        	throw new IllegalStateException("get message on topic " + record.topic());
		      }
		    }
		}
	}
	
	public void byCommonsExec(String[] command)  
			throws IOException,InterruptedException {
		DefaultExecutor executor = new DefaultExecutor();
		CommandLine cmdLine = CommandLine.parse(command[0]);
		for (int i=1, n=command.length ; i<n ; i++ ) {
			cmdLine.addArgument(command[i]);
		}
		executor.execute(cmdLine);
	}
}
