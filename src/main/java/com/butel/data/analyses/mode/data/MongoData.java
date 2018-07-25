package com.butel.data.analyses.mode.data;

import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Created by ninghf on 2017/12/13.
 */
public class MongoData {

    private static final Logger logger = LoggerFactory.getLogger(MongoData.class);

    private String databaseName;
    private Map<String, List<Document>> collections;

    public MongoData(String databaseName, Map<String, List<Document>> collections) {
        this.databaseName = databaseName;
        this.collections = collections;
    }

    public void insertMany(MongoAPI mongoAPI) {
        if (collections == null) {
            throw new NullPointerException("入库数据表内容为空...");
        }
        collections.forEach((collectionName, documents) -> {
            if (collectionName == null) {
                return;
            }
            if (documents == null || documents.isEmpty()) {
                return;
            }
            try {
                mongoAPI.insertMany(databaseName, collectionName, documents);
            } catch (Exception e) {
                logger.error("入库任务异常:\n异常的库-表-数据:{}-{}-{}, \n",
                        databaseName, collectionName, documents, e);
                final List<Document> checkNulls = new ArrayList <>();
                documents.forEach(document -> {
                    if (!Objects.isNull(document))
                        checkNulls.add(document);
                });
                mongoAPI.insertMany(databaseName, collectionName, checkNulls);
                logger.info("CHECK-documents:库-表-CHECK:{}-{}-{}",
                        databaseName, collectionName, checkNulls);
            }
        });
    }

    public static void main(String[] args) {
        List<String> list = new ArrayList <>();
        list.add(null);
        if (list.isEmpty()) {
            logger.error("list is empty");
        } else
            logger.error("list is not empty");
    }
}
