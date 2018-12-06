package com.onycom.dataManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.hadoop.conf.Configuration;

import com.google.gson.Gson;
import com.onycom.mesagehandler.TopicManager;

import kafka.javaapi.producer.Producer;
import kafka.producer.ProducerConfig;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import kafka.consumer.Consumer;
import kafka.consumer.ConsumerConfig;
import kafka.consumer.KafkaStream;
import kafka.javaapi.consumer.ConsumerConnector;
import kafka.message.MessageAndMetadata;

public class ProcessManager {
	/*
	 * kafka를 사용한 Process Handling
	 * processName으로 topic 생성.
	 */
	public void Process() {
//		String topicName = "";
//		TopicManager manager = new TopicManager();
//		try {
//			manager.createTopic(processName + "_RTN");
//		}
//		catch(Exception e) {
//			System.out.println(e.toString());
//			
//		}
		
		Properties props = new Properties();
		props.put("group.id", "ankus-analyzer");
		props.put("zookeeper.connect", "220.70.26.205:9092");
		props.put("auto.commit.interval.ms", "1000");
		props.put("metadata.broker.list", "220.70.26.205:9092");
		props.put("serializer.class", "kafka.serializer.StringEncoder");
		
		ConsumerConfig consumerConfig = new ConsumerConfig(props);
		ConsumerConnector consumer = Consumer.createJavaConsumerConnector(consumerConfig);
		
		Map<String, Integer> topicCountMap = new HashMap<String, Integer>();
		topicCountMap.put("MLREQUEST", 1);
		
		Map<String, List<KafkaStream<byte[], byte[]>>> consumerMap = consumer.createMessageStreams(topicCountMap);
		List<KafkaStream<byte[], byte[]>> streams = consumerMap.get("MLREQUEST");
		ExecutorService executor = Executors.newFixedThreadPool(1);
		
		for (final KafkaStream<byte[], byte[]> stream : streams) {
			executor.execute(new Runnable() {
				@Override
				public void run() {
					for (MessageAndMetadata<byte[], byte[]> keyValue : stream) {
						System.out.println(keyValue.key().toString());
						String valueParam = keyValue.message().toString();
						System.out.println(valueParam);
						Gson gson = new Gson();
						MethodParm mParam = gson.fromJson(valueParam, MethodParm.class);
						String methodName = mParam.getMethod();
						System.out.println(methodName);
					}
				}
			});
		}
		consumer.shutdown();
		executor.shutdown();
			
//		ProducerConfig producerConfig = new ProducerConfig(props);
//		Producer<String, String> producer = new Producer<String, String>(producerConfig);
		
	}
}
