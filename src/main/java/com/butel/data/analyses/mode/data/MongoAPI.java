/*===================================================================
 * 北京红云融通技术有限公司
 * 日期：2016年10月18日 下午2:37:10
 * 作者：ninghf
 * 版本：1.0.0
 * 版权：All rights reserved.
 *===================================================================
 * 修订日期           修订人               描述
 * 2016年10月18日     ninghf      创建
 */
package com.butel.data.analyses.mode.data;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.ServerAddress;
import com.mongodb.WriteConcern;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.UpdateOptions;
import io.netty.util.internal.StringUtil;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class MongoAPI {

	private static final Logger LOGGER = LoggerFactory.getLogger(MongoAPI.class);

	private String replicaSet;
	private MongoClient mongoClient;

	public MongoAPI(String replicaSet) {
		this.replicaSet = replicaSet;
		this.init();
	}

	public void init() {
		if (StringUtil.isNullOrEmpty(replicaSet)) {
			LOGGER.info("请联系管理员配置Mongo复制集连接信息!!!");
			return;
		}
		String[] replicaSets = replicaSet.split(",");
		List<ServerAddress> seeds = new ArrayList<ServerAddress>();
		for (String tmp : replicaSets) {
			if (!StringUtil.isNullOrEmpty(tmp)) {
				String[] address = tmp.split(":");
				ServerAddress serverAddress = new ServerAddress(address[0], Integer.valueOf(address[1]));
				seeds.add(serverAddress);
			}
		}
		MongoClientOptions options = MongoClientOptions.builder().writeConcern(WriteConcern.UNACKNOWLEDGED).build();
		mongoClient = new MongoClient(seeds, options);
		LOGGER.info("Mongo连接初始化完毕!");
		//TODO 需要根据表结构，创建表索引
	}
	
	public void stop() {
		if (mongoClient != null) {
			LOGGER.info("开始关闭Mongo连接...");
			mongoClient.close();
		}
	}
	
	public MongoCollection<Document> getMongoCollection(String databaseName, String collectionName) {
		MongoDatabase database = mongoClient.getDatabase(databaseName);
		MongoCollection<Document> mongoCollection = database.getCollection(collectionName);
		return mongoCollection;
	}
	
	public void insertMany(String databaseName, String collectionName, List<? extends Document> documents) {
		MongoCollection<Document> mongoCollection = getMongoCollection(databaseName, collectionName);
		mongoCollection.insertMany(documents);
	}
	
	public void replaceOne(String databaseName, String collectionName, Bson filter, Document update) {
		MongoCollection<Document> mongoCollection = getMongoCollection(databaseName, collectionName);
		UpdateOptions updateOptions = new UpdateOptions();
		mongoCollection.replaceOne(filter, update, updateOptions.upsert(true));
	}
	
	public void updateOne(String databaseName, String collectionName, Bson filter, Document update) {
		MongoCollection<Document> mongoCollection = getMongoCollection(databaseName, collectionName);
		UpdateOptions updateOptions = new UpdateOptions();
		mongoCollection.updateOne(filter, update, updateOptions.upsert(true));
	}
	
	public FindIterable<Document> find(String databaseName, String collectionName, Bson filter) {
		MongoCollection<Document> mongoCollection = getMongoCollection(databaseName, collectionName);
		FindIterable<Document> result = mongoCollection.find(filter);
		return result;
	}
}
