package com.onycom.mesagehandler;

import java.util.Date;
import java.util.List;

import org.apache.zookeeper.ZooKeeper;
public class TopicManager {
	/*
	 * Please update zookeeper address and port
	 */
	public List<String> getTopicList() {
		ZooKeeper zk = null;
		List<String> topics = null;
		try {
			zk = new ZooKeeper("220.70.26.205:2181", 10000, null);
			topics = zk.getChildren("/brokers/topics", false);
		
	        for (String topic : topics) {
	            System.out.println(topic);
	        }
		}
        catch(Exception e) {
        	
        }
        return topics;
	}
	/*
	 * Please update zookeeper address and port
	 */
	public void createTopic(String TopicName)throws Exception {
		
		ankusZKConnect connector = new ankusZKConnect();
		ZooKeeper zk = connector.connect("220.70.26.205:2181");
		String newNode = TopicName;
        connector.createNode(newNode, new Date().toString().getBytes());
	}
}
