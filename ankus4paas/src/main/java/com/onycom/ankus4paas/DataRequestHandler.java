package com.onycom.ankus4paas;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hdfs.DistributedFileSystem;
import org.bson.Document;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.Gson;
import com.onycom.dataManager.HdfsHandler;
import com.onycom.dataManager.MethodParm;
import com.onycom.mesagehandler.TopicManager;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;
import kafka.producer.ProducerConfig;


@RestController
public class DataRequestHandler  extends Configured {
	
	private HdfsHandler hdfs;
	
	@RequestMapping(value = "getdir/appkey={appkey}&path={path}", method=RequestMethod.GET)
	public String getdir(@PathVariable("appkey") String appkey, 
							@PathVariable("path") String path) {
		hdfs = new HdfsHandler();
		return hdfs.getHdfsDir(path, appkey);
	}
	
	@RequestMapping(value = "getML/param={param}", method=RequestMethod.POST)
	public int getML(@PathVariable("param") String param) {
		
		int rtn = 0;
		String topicName = param;
		TopicManager manager = new TopicManager();	
		try {
			manager.createTopic("MLREQUEST");
		}
		catch (Exception e) {
			System.out.println(e.toString());
		}
		
		Gson gson = new Gson();
		MethodParm mParam = gson.fromJson(param, MethodParm.class);
				
		String topicMessage = "";
		Properties props = new Properties();
		props.put("metadata.broker.list", "220.70.26.205:9092");
		props.put("serializer.class", "kafka.serializer.StringEncoder");
		
		ProducerConfig producerConfig = new ProducerConfig(props);
		
		Producer<String, String> producer = new Producer<String, String>(producerConfig);
		KeyedMessage<String, String> message = new KeyedMessage<String, String>(topicName, mParam.toString());
		
		try {
			producer.send(message);
		}
		catch (Exception e) {
			System.out.println(e.toString());
		}
		producer.close();
		return rtn;
	}
}
