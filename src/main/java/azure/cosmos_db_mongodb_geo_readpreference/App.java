package azure.cosmos_db_mongodb_geo_readpreference;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.bson.Document;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoClientURI;
import com.mongodb.ReadPreference;
import com.mongodb.Tag;
import com.mongodb.TagSet;
import com.mongodb.client.MongoCollection;

/**
 * Hello world!
 *
 */
public class App 
{
	private String  appConfigPath = "src/main/resources/config";
	private String connectionString = "";
	private String readTargetRegion = "";
	private String dbName = "";
	private String collName = "";
	private Properties properties;
	private MongoClient client;
	
	App()
	{
		this.properties = new Properties();
		try {
			this.properties.load(new FileInputStream(appConfigPath));
			this.connectionString = this.properties.getProperty("connectionString");
			this.readTargetRegion = this.properties.getProperty("readTargetRegion");
			this.dbName = this.properties.getProperty("databaseName");
			this.collName = this.properties.getProperty("collectionName");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println(this.connectionString);
	}
	
	private void InsertDocs()
	{
		System.out.println("Inserting documents");
		MongoCollection<Document> coll = this.client.getDatabase(this.dbName).getCollection(this.collName);
		for(int i=0;i<10;i++)
		{
			coll.insertOne(new Document("x", i));
		}
		System.out.println("Inserting documents complete.");
	}
	
	private void ReadFromPrimaryRegion()
	{
		System.out.println("Reading documents from Primary");
		MongoCollection<Document> coll = this.client.getDatabase(this.dbName).getCollection(this.collName);
	    int cnt=0;
		for(Document doc : coll.find())
	    {
	    	cnt++;
	    }
	    System.out.println("Docs read from Write region: "+cnt);
	    
	}
	
	private void ReadFromSecondaryRegion()
	{

		System.out.println("Reading documents from Secondary");
		MongoCollection<Document> coll = this.client.getDatabase(this.dbName).getCollection(this.collName).withReadPreference(ReadPreference.secondaryPreferred());
	    int cnt=0;
		for(Document doc : coll.find())
	    {
	    	cnt++;
	    }
	    System.out.println("Docs read from read region if present : "+cnt);
	}
	
	private void ReadFromNearestRegion()
	{
		System.out.println("Reading documents from Nearest");
		MongoCollection<Document> coll = this.client.getDatabase(this.dbName).getCollection(this.collName).withReadPreference(ReadPreference.nearest());
	    int cnt=0;
		for(Document doc : coll.find())
	    {
	    	cnt++;
	    }
	    System.out.println("Docs read from Nearest region : "+cnt);
	}
	
	private void ReadFromSpecificRegion(String regionName)
	{
		List<TagSet> tgsetList = new ArrayList<TagSet>();
		TagSet tgset = new TagSet(new Tag("region", regionName));
		tgsetList.add(tgset);
		
		System.out.println("Reading documents from region: "+ regionName);
		MongoCollection<Document> coll = this.client.getDatabase(this.dbName).getCollection(this.collName).withReadPreference(ReadPreference.secondaryPreferred(tgsetList));
	    int cnt=0;
		for(Document doc : coll.find())
	    {
	    	cnt++;
	    }
	    System.out.println("Docs read from specified region : "+cnt);
	}
	
	private void InitializeMongoClient()
	{
		MongoClientOptions.Builder optionsBuilder = new MongoClientOptions.Builder();
        // Set values to prevent timeouts
        optionsBuilder.socketTimeout(10000);
        optionsBuilder.maxConnectionIdleTime(60000);
        optionsBuilder.heartbeatConnectTimeout(5000);
        
        MongoClientURI mongoClientURI = new MongoClientURI(this.connectionString, optionsBuilder);
		this.client = new MongoClient(mongoClientURI);		
	}
	public void RunSample()
	{
		System.out.println("Start sample run..");
		//Initialize Mongo Client
		InitializeMongoClient();		
		
		//Insert docs
		this.InsertDocs();
		
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//Read using different read preference modes
		this.ReadFromPrimaryRegion();
		
		this.ReadFromSecondaryRegion();
		this.ReadFromNearestRegion();
		this.ReadFromSpecificRegion(this.readTargetRegion);
		System.out.println("Sample run completed..");
	}
    public static void main( String[] args )
    {
        App sampleApp = new App();
        sampleApp.RunSample();
    }
}
