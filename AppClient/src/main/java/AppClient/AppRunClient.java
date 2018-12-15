package AppClient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import com.google.gson.Gson;
import com.onycom.mesagehandler.TopicManager;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;

public class AppRunClient {
	private static Properties createProducerConfig(String brokers) {
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
	public static void main(String[] args) throws Exception {
		String packageName = "sh";
		TopicManager manager = new TopicManager();
		manager.createTopic("MLREQUEST");
		Properties props = createProducerConfig("localhost:9092");
		KafkaProducer<String, String> producer = new KafkaProducer<String, String>(props);
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
		
		try {
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
		
		ConsummerProper consummerProper;
		KafkaConsumer<String, String> consumer ;
		consummerProper = new ConsummerProper();
		
		consumer = consummerProper.getConsumer();
		consumer.subscribe(Arrays.asList("MLREQUEST_123456789_RTN1"));
		boolean bResultReceive = false;
		try {
			while(bResultReceive == false) {
				ConsumerRecords<String, String> records = consumer.poll(500);
				for (ConsumerRecord<String, String> record : records) {
					switch (record.topic()) {
					case "MLREQUEST_"+"123456789_RTN1":
						String strInjson = record.value();
						System.out.println("Result:" + strInjson);
						bResultReceive = true;
						break;
					}
				}
			}
		} finally {
			consumer.close();
		}
	}
}
