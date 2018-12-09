package AppClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;

import com.google.gson.Gson;
import com.onycom.mesagehandler.TopicManager;

import java.util.Properties;
import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;
import kafka.producer.ProducerConfig;

public class AppRunClient {
	public static void main(String[] args) throws Exception {
		TopicManager manager = new TopicManager();
		manager.createTopic("MLREQUEST");
		
		List<String> topics  = manager.getTopicList();
		System.out.println(topics);
		
		Properties props = new Properties();
		props.put("metadata.broker.list", "127.0.0.1:9092");
		props.put("serializer.class", "kafka.serializer.StringEncoder");
		
		ProducerConfig producerConfig = new ProducerConfig(props);
		Producer<String, String> producer = new Producer<String, String>(producerConfig);

		List<KeyedMessage<String, Gson>> messages = new ArrayList<KeyedMessage<String, Gson>>();
		Gson gson = new Gson();
		MethodParm mParam = new MethodParm();
		mParam.setAppkey("123456789");
		mParam.setMethod("sh");
		
		List<Pair<String, String>> methodParam = new ArrayList<Pair<String, String>>();
		methodParam.add(new MutablePair<>("echo", "'hi'"));
		mParam.setMethod_param(methodParam);
		String json = gson.toJson(mParam);
		System.out.println(json);
		KeyedMessage<String, String> message = new KeyedMessage<String, String>("MLREQUEST",gson.toJson(mParam));
		try {
			producer.send(message);
		}
		catch (Exception e) {
			System.out.println(e.toString());
		}
		producer.close();
	}
}
