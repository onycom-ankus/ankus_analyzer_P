package com.onycom.mesagehandler;

import java.util.List;

public class Kakfa_Manager_Test 
{
	
	public static void main(String[] args) throws Exception {
		TopicManager manager = new TopicManager();
		List<String> topics  = manager.getTopicList();
		System.out.println(topics);
		manager.createTopic("/brokers/topics/ChamberMonitor");
	}
}
