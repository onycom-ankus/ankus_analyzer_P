package zookeeperHandler;
import java.util.Properties;
import kafka.admin.AdminUtils;
import kafka.admin.RackAwareMode;
import kafka.utils.ZKStringSerializer$;
import kafka.utils.ZkUtils;
import kafka.zk.AdminZkClient;
import kafka.zk.KafkaZkClient;
import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.ZkConnection;
import org.apache.kafka.common.utils.Time;

public class TopicHandler {
	//groupName = "ankus-analzer-p"
	public void CreateTopic(String groupName, String topicName) {
		String zookeeperHost = "127.0.0.1:2181";
		Boolean isSucre = false;
		int sessionTimeoutMs = 200000;
		int connectionTimeoutMs = 15000;
		int maxInFlightRequests = 10;
		Time time = Time.SYSTEM;
		String metricGroup = groupName;
		String metricType = "1";
		KafkaZkClient zkClient = KafkaZkClient.apply(zookeeperHost,isSucre,sessionTimeoutMs,connectionTimeoutMs,maxInFlightRequests,time,metricGroup,metricType);
		
		AdminZkClient adminZkClient = new AdminZkClient(zkClient);

		int partitions = 2;
		int replication = 1;
		Properties topicConfig = new Properties();

		adminZkClient.createTopic(topicName,partitions,replication,topicConfig,RackAwareMode.Disabled$.MODULE$);
	}
	
	public boolean topicExist(String groupName, String topicName) {
		String zookeeperHost = "127.0.0.1:2181";
		Boolean isSucre = false;
		int sessionTimeoutMs = 200000;
		int connectionTimeoutMs = 15000;
		int maxInFlightRequests = 10;
		Time time = Time.SYSTEM;
		String metricGroup = groupName;
		String metricType = "1";
		KafkaZkClient zkClient = KafkaZkClient.apply(zookeeperHost,isSucre,sessionTimeoutMs,connectionTimeoutMs,maxInFlightRequests,time,metricGroup,metricType);
		return zkClient.topicExists(topicName);
	}
}
