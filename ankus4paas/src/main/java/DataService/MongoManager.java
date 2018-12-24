package DataService;

import java.util.Arrays;

import org.bson.Document;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

public class MongoManager {
	private MongoDatabase userDb = null;
	
	
	@SuppressWarnings("deprecation")
	public MongoManager() {
		MongoClient mongoClient = null;
		
		String user = "userid";//mongo userid
		String passwd = ""; //mongo pwd
		String host = "localhost";//mongodb server ip
		String port = "27017"; //mongodb port
		String database = "ankusUser";

		MongoCredential credential = MongoCredential.createCredential(user, database, passwd.toCharArray());
		MongoClientOptions option = MongoClientOptions.builder().sslEnabled(false).build();

		try{
			mongoClient = new MongoClient(new ServerAddress(host,Integer.parseInt(port)), Arrays.asList(credential),option);
			userDb = mongoClient.getDatabase(database);
		}
		catch(Exception e)
		{
			System.out.println(e.toString());
		}
	}
	public MongoCollection<Document> getUserAppCollection(String collectionName)
	{
		MongoCollection<Document> collection = userDb.getCollection(collectionName);
		return collection;
	}
}
