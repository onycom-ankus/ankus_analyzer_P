package com.onycom.mesagehandler;

import java.util.Date;
import java.util.List;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.apache.zookeeper.proto.WatcherEvent;

class TopicWatcher extends WatcherEvent{
	
}
public class TopicManager {
	/*
	 * Please update zookeeper address and port
	 */
	public List<String> getTopicList() {
		
		ZooKeeper zk = null;
		List<String> topics = null;
		try {
			zk = new ZooKeeper("localhost:2181", 10000,  null);
			topics = zk.getChildren("/brokers/topics", false);
		
		}
        catch(Exception e) {
        	System.out.println(e.toString());
        }
        return topics;
	}
	/*
	 * Please update zookeeper address and port
	 */
	public void createTopic(String TopicName)throws Exception {
		
		ankusZKConnect connector = new ankusZKConnect();
		ZooKeeper zk = connector.connect("localhost:2181");
		String newNode = TopicName;
        connector.createNode(newNode, new Date().toString().getBytes());
	}
}
