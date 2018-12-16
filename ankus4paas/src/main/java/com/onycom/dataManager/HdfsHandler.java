package com.onycom.dataManager;

import java.io.InputStream;
import java.net.URI;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hdfs.DistributedFileSystem;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.bson.Document;
import org.openankus.ZooKeeperHandler.TopicHandler;

import java.util.Properties;
public class HdfsHandler {
	
	public String getHdfsDir(String path, String appkey) {
		Document directory = new Document();
		try {
			if (path.equals("root"))
				path  = "";
            FileSystem fs = new DistributedFileSystem();
            String Url = "hdfs://localhost:9000/"+ appkey + "/" + path;
            fs.initialize(new URI(Url), new Configuration());
            FileStatus[] status = fs.listStatus(new Path(Url));
            for (int i = 0; i < status.length; i++) {
            	 System.out.println(status[i].getPath().toString());
            	 directory.append(appkey, status[i].getPath().toString());
            }
        } catch (Exception ex) {
            return ex.toString();
        }
		return directory.toJson().toString();
	}
	private static Properties createProducerConfig(String brokers) {
		Properties props = new Properties();
		props.put("bootstrap.servers", brokers);
		props.put("acks", "all");
		props.put("retries", 0);
		props.put("batch.size", 16384);
		props.put("linger.ms", 1);
		props.put("buffer.memory", 33554432);
		props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
		props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
		return props;
	}
	
	/* requestTopic + path로 토픽을 만들어
	 * path안의 내용을 바이터 배열로 프로듀싱 함..
	 */
	private void getFileContents(String requestTopic, String path, String appkey) {
		TopicHandler topicHandler = new TopicHandler();
		try {
			topicHandler.CreateTopic("ankus-analzer-p", requestTopic + "_"+ path);
		}
		catch(Exception e) {
			System.out.println(e.toString());
		}
		
		Properties props = createProducerConfig(requestTopic + "_"+ path);
		KafkaProducer<String, byte[]> producer = new KafkaProducer<String, byte[]>(props);
		Configuration conf = new Configuration();

		try {
			String uri="hdfs://localhost:9000/"+ appkey + "/" + path;
			FileSystem fs = FileSystem.get(URI.create(uri), conf);
			
			InputStream in = null;
			byte buffer[] = new byte[256];
		
			in = fs.open(new Path(uri));
			in.read(buffer);
			while (in.read(buffer) > 0) {
//				KeyedMessage<String, byte[]> message = new KeyedMessage<String, byte[]>(requestTopic + "_"+ path, buffer); 
				producer.send(new ProducerRecord<String, byte[]>(requestTopic + "_"+ path, buffer), new Callback() {
			        public void onCompletion(RecordMetadata metadata, Exception e) {
			          if (e != null) {
			            e.printStackTrace();
			          }
			          System.out.println("Sent:" + buffer + ", Partition: " + metadata.partition() + ", Offset: "
			              + metadata.offset());
			        }
			      });
				
			}
			
			producer.close();
		} catch(Exception e) {

		}
	}
}
