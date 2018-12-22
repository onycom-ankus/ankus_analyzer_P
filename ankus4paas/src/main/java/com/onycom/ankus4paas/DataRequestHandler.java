package com.onycom.ankus4paas;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hdfs.DistributedFileSystem;
import org.bson.Document;
import org.openankus.ZooKeeperHandler.ConsummerProper;
import org.openankus.ZooKeeperHandler.TopicHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import com.google.gson.Gson;


import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import com.google.gson.Gson;
import com.onycom.dataManager.HdfsHandler;
import com.onycom.dataManager.ProcessProperty;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;


@RestController
public class DataRequestHandler  extends Configured {
	
	private HdfsHandler hdfs;
	
	@RequestMapping(value = "getdir/appkey={appkey}&path={path}", method=RequestMethod.GET)
	public String getdir(@PathVariable("appkey") String appkey, 
							@PathVariable("path") String path) {
		hdfs = new HdfsHandler();
		return hdfs.getHdfsDir(path, appkey);
	}
	
	private Properties createProducerConfig(String brokers) {
		Properties props = new Properties();
		props.put("bootstrap.servers", brokers);
		props.put("acks", "all");
		props.put("retries", 10);
		props.put("batch.size", 16384);
		props.put("linger.ms", 1);
		props.put("buffer.memory", 33554432);
		props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
		props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
		return props;
	}
	@RequestMapping(value = "runcmd/", method = RequestMethod.POST)
	public String runcmd(@RequestBody ProcessProperty requestWrapper )  throws Exception {
		String myAppKey = "g1234569";
		
		String rtn = "";
		String packageName = "sh";
		TopicHandler topicHandler = new TopicHandler();
		try {
			topicHandler.CreateTopic("ankus-analzer-p","MLREQUEST"); //Create DA Process Topic with 'MLREQUEST'
		} catch (Exception e) {
			System.out.println(e.toString());
		}
		Properties props = createProducerConfig("localhost:9092");
		KafkaProducer<String, String> producer = new KafkaProducer<String, String>(props);
		Gson gson = new Gson();
		ProcessProperty mParam = new ProcessProperty();
		mParam.setAppkey(myAppKey);
		mParam.setPackageName(packageName);

		List<Pair> functionParam = new ArrayList<>();
		functionParam.add(new MutablePair<>("/bin/echo", "ankus_echo1 test"));
		String functionParmStr =gson.toJson(functionParam);
		System.out.println(functionParmStr);
		mParam.setFunctionParam(functionParam);		
		String json = gson.toJson(mParam);		
		try {
			//json has appkey
			producer.send(new ProducerRecord<String, String>("MLREQUEST", json), new Callback() {
				public void onCompletion(RecordMetadata metadata, Exception e) {
					if (e != null) {
						e.printStackTrace();
					}
					System.out.println("Sent:" + json + ", Partition: " + metadata.partition() + ", Offset: "
							+ metadata.offset());
				}
			});
		}
		catch (Exception e) {
			System.out.println(e.toString());
		}
		producer.close();
		
		while(topicHandler.topicExist("ankus-analzer-p", "MLREQUEST_" + myAppKey) == false) {
			continue;
		}
		
		int consumerResponseWaitCnt = 0;
		boolean bResultReceive = false;
		while(consumerResponseWaitCnt < 3 && bResultReceive == false) {
			ConsummerProper consummerProper = new ConsummerProper();
			KafkaConsumer<String, String> consumer = consummerProper.getConsumer();
			consumer.subscribe(Arrays.asList("MLREQUEST_" + myAppKey)); //Get Process result using 'MLREQUEST_'+ appkey
			
			try {
				while(bResultReceive == false) {
					ConsumerRecords<String, String> records = consumer.poll(1);					
					for (ConsumerRecord<String, String> record : records) {
						if (record.topic().toString().equals("MLREQUEST_" + myAppKey)) {
							String strInjson = record.value();
							System.out.println("Result:" + strInjson);
							try {
								consumer.commitSync();
							} catch (Exception e) {
								System.out.println(e.toString());
							}
							bResultReceive = true;
							break;
						}
					}
					Thread.sleep(1000);
					consumerResponseWaitCnt++;
					if( consumerResponseWaitCnt > 3) {
						break;
					}
				}
			} finally {
				consumer.close();
			}
		}
		return rtn;
	}
}
