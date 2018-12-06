package com.onycom.dataManager;

import java.io.InputStream;
import java.net.URI;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hdfs.DistributedFileSystem;
import org.bson.Document;

import com.onycom.mesagehandler.TopicManager;

import java.util.Properties;
import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;
import kafka.producer.ProducerConfig;
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
	
	/* requestTopic + path로 토픽을 만들어
	 * path안의 내용을 바이터 배열로 프로듀싱 함..
	 */
	private void getFileContents(String requestTopic, String path, String appkey) {
		TopicManager manager = new TopicManager();
		try {
			manager.createTopic(requestTopic + "_"+ path);
		}
		catch(Exception e) {
			System.out.println(e.toString());
		}
		
		Properties props = new Properties();
		props.put("metadata.broker.list", "220.70.26.205:9092");
		props.put("serializer.class", "kafka.serializer.StringEncoder");
		ProducerConfig producerConfig = new ProducerConfig(props);
		Producer<String, byte[]> producer = new Producer<String, byte[]>(producerConfig);
		
		Configuration conf = new Configuration();

		try {
			String uri="hdfs://localhost:9000/"+ appkey + "/" + path;
			FileSystem fs = FileSystem.get(URI.create(uri), conf);
			
			InputStream in = null;
			byte buffer[] = new byte[256];
		
			in = fs.open(new Path(uri));
			in.read(buffer);
			while (in.read(buffer) > 0) {
				KeyedMessage<String, byte[]> message = new KeyedMessage<String, byte[]>(requestTopic + "_"+ path, buffer); 
				producer.send(message);
				
			}
		} catch(Exception e) {

		}
	}
}
