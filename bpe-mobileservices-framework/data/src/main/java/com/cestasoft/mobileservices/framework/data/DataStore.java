package com.cestasoft.mobileservices.framework.data;

import com.mongodb.MongoException;
import com.mongodb.client.*;
import com.mongodb.client.model.ReplaceOptions;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.InsertManyResult;
import com.mongodb.client.result.InsertOneResult;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class DataStore {

    final static Logger logger = LoggerFactory.getLogger(DataStore.class);

    private MongoClient mongoClient;

    public DataStore(String dbUri) {
        this.mongoClient = MongoClients.create(dbUri);
    }

    public ArrayList<Document> fetch(String model) {
        ArrayList<Document> result = new ArrayList<>();
        try {
            String[] pair = model.split(":");
            MongoDatabase db = mongoClient.getDatabase(pair[0]);
            MongoCollection<Document> coll = db.getCollection(pair[1]);
            try (MongoCursor<Document> cursor = coll.find().iterator()) {
                while (cursor.hasNext()) {
                    result.add(cursor.next());
                }
            }
        } catch (Exception e) {
            logger.error("Failed to fetch document", e);
        }
        return result;
    }

    public ArrayList<Document> query(Bson filter, String model) {
        ArrayList<Document> result = new ArrayList<>();
        try {
            String[] pair = model.split(":");
            MongoDatabase db = mongoClient.getDatabase(pair[0]);
            MongoCollection<Document> coll = db.getCollection(pair[1]);
            try (MongoCursor<Document> cursor = coll.find(filter).iterator()) {
                while (cursor.hasNext()) {
                    result.add(cursor.next());
                }
            }
        } catch (Exception e) {
            logger.error("Failed to query documents", e);
        }
        return result;
    }

    public Document ask(Bson filter, String model) {
        Document result = new Document();
        try {
            String[] pair = model.split(":");
            MongoDatabase db = mongoClient.getDatabase(pair[0]);
            MongoCollection<Document> coll = db.getCollection(pair[1]);
            return coll.find(filter).first();
        } catch (Exception e) {
            logger.error("Failed to ask document", e);
        }
        return result;
    }

    public ArrayList<Document> aggregate(List<Bson> pipeline, String model) {
        ArrayList<Document> result = new ArrayList<>();
        try {
            String[] pair = model.split(":");
            MongoDatabase db = mongoClient.getDatabase(pair[0]);
            MongoCollection<Document> coll = db.getCollection(pair[1]);
            try (MongoCursor<Document> cursor = coll.aggregate(pipeline).iterator()) {
                while (cursor.hasNext()) {
                    result.add(cursor.next());
                }
            }
        } catch (Exception e) {
            logger.error("Failed to aggregate documents", e);
        }
        return result;
    }

    public Document upsert(Document filter, Document document, String model) {
        Document doc = new Document();
        doc.put("success", false);
        try {
            String[] pair = model.split(":");
            MongoDatabase db = mongoClient.getDatabase(pair[0]);
            MongoCollection<Document> coll = db.getCollection(pair[1]);
            try {
                ReplaceOptions opts = new ReplaceOptions().upsert(true);
                UpdateResult result = coll.replaceOne(filter, document, opts);
                doc.put("upsertedId", result.getUpsertedId());
                doc.put("success", true);
                return doc;
            } catch (MongoException me) {
                logger.error("MongoException during upsert", me);
            }
        } catch (Exception e) {
            logger.error("Failed to upsert document", e);
        }
        return doc;
    }

    public Document store(Document document, String model) {
        Document doc = new Document();
        doc.put("success", false);
        try {
            String[] pair = model.split(":");
            MongoDatabase db = mongoClient.getDatabase(pair[0]);
            MongoCollection<Document> coll = db.getCollection(pair[1]);
            try {
                InsertOneResult insRes = coll.insertOne(document);
                doc.put("insertedId", insRes.getInsertedId().toString());
                doc.put("success", true);
                return doc;
            } catch (MongoException me) {
                logger.error("MongoException during store", me);
            }
        } catch (Exception e) {
            logger.error("Failed to store document", e);
        }
        return doc;
    }

    public Document store(List<Document> documents, String model) {
        Document doc = new Document();
        doc.put("success", false);
        try {
            String[] pair = model.split(":");
            MongoDatabase db = mongoClient.getDatabase(pair[0]);
            MongoCollection<Document> coll = db.getCollection(pair[1]);
            try {
                InsertManyResult insRes = coll.insertMany(documents);
                doc.put("insertedCount", insRes.getInsertedIds().size());
                doc.put("success", true);
                return doc;
            } catch (MongoException me) {
                logger.error("MongoException during store many", me);
            }
        } catch (Exception e) {
            logger.error("Failed to store documents", e);
        }
        return doc;
    }

    public Document delete(Bson filter, String model) {
        Document doc = new Document();
        doc.put("success", false);
        try {
            String[] pair = model.split(":");
            MongoDatabase db = mongoClient.getDatabase(pair[0]);
            MongoCollection<Document> coll = db.getCollection(pair[1]);
            try {
                DeleteResult result = coll.deleteOne(filter);
                doc.put("deletedCount", result.getDeletedCount());
                doc.put("success", result.getDeletedCount() > 0);
                return doc;
            } catch (MongoException me) {
                logger.error("MongoException during delete", me);
            }
        } catch (Exception e) {
            logger.error("Failed to delete document", e);
        }
        return doc;
    }

    public void close() {
        try {
            mongoClient.close();
        } catch (Exception e) {
            logger.error("Error closing datastore connection", e);
        }
    }
}
