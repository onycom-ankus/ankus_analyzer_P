package com.onycom.dataManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
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
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.onycom.mesagehandler.ConsummerProper;
import com.onycom.mesagehandler.ProducerProper;
import com.onycom.mesagehandler.TopicManager;

import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;
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
	KafkaConsumer<String, String> consumer ;
	
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
		consumer = consummerProper.getConsumer();
		
		consumer.subscribe(Arrays.asList("MLREQUEST"));
		
		while (true) {
		    ConsumerRecords<String, String> records = consumer.poll(500);
		    for (ConsumerRecord<String, String> record : records) {
		      switch (record.topic()) {
		        case "MLREQUEST":
		        	Gson gson = new Gson();
		        	String strInjson = record.value();
		        	JsonParser parser = new JsonParser();
		    		JsonElement element = parser.parse(strInjson);
		    		
		    		String appkey = element.getAsJsonObject().get("appkey").getAsString();
		    		String newTopicName = "MLREQUEST_"+appkey;
		    		
		    		
		    		String packageName = element.getAsJsonObject().get("packageName").getAsString();
		    		switch(packageName) {
			    		case "sh":
			    			JsonArray jsonFunParamList = element.getAsJsonObject().get("functionParam").getAsJsonArray();
				    		for(JsonElement jsunFunParam: jsonFunParamList) {
				    			JsonObject argument = jsunFunParam.getAsJsonObject();
				    			String key = argument.get("left").toString().replaceAll("\"", "");
				    			String value = argument.get("right").toString().replaceAll("\"", "");
				    			System.out.println(key);
				    			System.out.println(value);
				    			List<String> listCommand = new ArrayList<String>();
				    			listCommand.add(key);
				    			listCommand.add(value);
				    			String[] command = listCommand.toArray(new String[0]);
				    			try {
				    				System.out.println("cmd:" + listCommand.toString());
				    				byCommonsExec(newTopicName, command);
				    			} catch(Exception e) {
				    				System.out.println(e.toString());
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
	
	@SuppressWarnings("deprecation")
	public void byCommonsExec(String topic, String[] command)  
			throws IOException,InterruptedException {
		ProcessBuilder pb = new ProcessBuilder(command);
		try
		{
			try {
	    		TopicManager manager = new TopicManager();
	    		manager.createTopic(topic);
    		} catch(Exception e) {
    			System.out.println(e.toString());
    		}
			Properties props = new Properties();
			props.put("metadata.broker.list", "127.0.0.1:9092");
			props.put("serializer.class", "kafka.serializer.StringEncoder");
			ProducerConfig producerConfig = new ProducerConfig(props);
			Producer<String, String> producer = new Producer<String, String>(producerConfig);
			KeyedMessage<String, String> message = new KeyedMessage<String, String>(topic, topic+"_rtn");
			producer.send(message);
			
			Process process = pb.start();//실행
			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			StringBuilder builder = new StringBuilder();
			String line = null;
			while ( (line = reader.readLine()) != null) {
				builder.append(line);
				builder.append(System.getProperty("line.separator"));
			}
			String result = builder.toString();
			System.out.println(result);
			
			producer.close();
		}
		catch(Exception e) {
			System.out.println(e.toString());
		}
	}
}
