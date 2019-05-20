package com.onycom.AppRunner;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import org.openankus.ZooKeeperHandler.TopicHandler;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.PeriodicTrigger;

public class UserScheduler {
	private ThreadPoolTaskScheduler scheduler;
	HashMap<String, Integer> topicAtiveHash = new HashMap<String, Integer>();
	
	public UserScheduler() {
		startScheduler();
	}
	

	public void stopScheduler() {
        scheduler.shutdown();
    }
 
    public void startScheduler() {
        scheduler = new ThreadPoolTaskScheduler();
        scheduler.initialize();
        // 스케쥴러가 시작되는 부분 
        scheduler.schedule(getRunnable(), getTrigger());
    }
    private Runnable getRunnable(){
        return () -> {
            // do something 
            topicScanner();
            consumerExecuter();
        };
    }
 
    private Trigger getTrigger() {
        // 작업 주기 설정 
        return new PeriodicTrigger(1, TimeUnit.SECONDS);
    }
    
    private void topicScanner() {
    	/*
    	 * topics을 hashmap에 저장
    	 */
    	TopicHandler topicHandler = new TopicHandler();		
    	
//		List<String> topics  = topicHandler.getTopicList();
//		for(String topic: topics) {
//			if (topicAtiveHash.containsKey(topic) != true) {
//				topicAtiveHash.put(topic, 0);
//			}
//		}
    }
    
    public void consumerExecuter() {
    	/*
		 * thread에 등록되지 않는 것만 추가로 등록
		 */
		// 방법2
		for(Entry<String, Integer> topicStatus : topicAtiveHash.entrySet() ){
        	if ( topicStatus.getValue() == 0 ) {
        		String topic = topicStatus.getKey();
        		topicAtiveHash.put(topic, 1);
        		
        		String brokers = "localhost:9092";
    			String groupId = "ankus-analyzer-p";
    			int numberOfConsumer = 3; 
    			// Start group of Notification Consumers
    			NotificationConsumerGroup consumerGroup = new NotificationConsumerGroup(brokers, groupId, topic, numberOfConsumer);
    			consumerGroup.execute();
        	}
        }
    }
    
	
}
