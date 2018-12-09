package AppClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.onycom.mesagehandler.TopicManager;

import java.util.Properties;
import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;
import kafka.producer.ProducerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
public class AppRunClient {
	public static void main(String[] args) throws Exception {
		TopicManager manager = new TopicManager();
		manager.createTopic("MLREQUEST");
		
		String packageName = "sh";
		
		List<String> topics  = manager.getTopicList();
		System.out.println(topics);
		Properties props = new Properties();
		props.put("metadata.broker.list", "127.0.0.1:9092");
		props.put("serializer.class", "kafka.serializer.StringEncoder");
		ProducerConfig producerConfig = new ProducerConfig(props);
		Producer<String, String> producer = new Producer<String, String>(producerConfig);

		Gson gson = new Gson();
		ProcessProperty mParam = new ProcessProperty();
		mParam.setAppkey("123456789");
		mParam.setPackageName(packageName);
		
		List<Pair> functionParam = new ArrayList<>();
		functionParam.add(new MutablePair<>("/bin/echo", "ankus_echo test"));
		String functionParmStr =gson.toJson(functionParam);
		System.out.println(functionParmStr);
		mParam.setFunctionParam(functionParam);
		String json = gson.toJson(mParam);
		
		KeyedMessage<String, String> message = new KeyedMessage<String, String>("MLREQUEST",gson.toJson(mParam));
		try {
			producer.send(message);
		}
		catch (Exception e) {
			System.out.println(e.toString());
		}
		producer.close();
		
		Properties configs = new Properties();
		configs.put("bootstrap.servers", "localhost:9092");
		configs.put("session.timeout.ms", "10000");
		configs.put("group.id", "ankus-analzer-p");
		
		configs.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
		configs.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
		KafkaConsumer<String, String> consumer = new KafkaConsumer<>(configs);
		ConsumerRecords<String, String> records = consumer.poll(500);
	    for (ConsumerRecord<String, String> record : records) {
	      switch (record.topic()) {
	        case "MLREQUEST"+"123456789":
	        	String strInjson = record.value();
	        	System.out.println(strInjson);
	      }
	    }
	}
}
