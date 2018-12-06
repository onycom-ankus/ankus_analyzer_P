package AppClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import com.onycom.mesagehandler.TopicManager;

import java.util.Properties;
import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;
import kafka.producer.ProducerConfig;

public class AppRunClient {
	public static void main(String[] args) throws Exception {
		TopicManager manager = new TopicManager();
		manager.createTopic("prismdata");
		
		List<String> topics  = manager.getTopicList();
		System.out.println(topics);
		
		Properties props = new Properties();
		props.put("metadata.broker.list", "220.70.26.205:9092");
		props.put("serializer.class", "kafka.serializer.StringEncoder");
		
		ProducerConfig producerConfig = new ProducerConfig(props);
		Producer<String, String> producer = new Producer<String, String>(producerConfig);

		List<KeyedMessage<String, String>> messages = new ArrayList<KeyedMessage<String, String>>(); 
		KeyedMessage<String, String> message = new KeyedMessage<String, String>("prismdata", "ankus_library:a1");
		try {
			producer.send(message);
		}
		catch (Exception e) {
			System.out.println(e.toString());
		}
		producer.close();
	}
}
