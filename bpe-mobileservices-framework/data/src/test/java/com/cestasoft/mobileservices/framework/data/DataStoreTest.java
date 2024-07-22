package com.cestasoft.mobileservices.framework.data;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.junit.jupiter.api.*;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class DataStoreTest {
    private DataStore dataStore;

    @BeforeAll
    public void setUp() {
        dataStore = new DataStore("mongodb://localhost:27017");
    }

    @AfterAll
    public void tearDown() {
        dataStore.close();
    }

    @Test
    public void testStore() {
        Document document = new Document("testField", "testValue");
        Document result = dataStore.store(document, "ussd_db:testCollection");
        assertTrue(result.getBoolean("success"));
    }

    @Test
    public void testFetch() {
        ArrayList<Document> documents = dataStore.fetch("ussd_db:testCollection");
        assertNotNull(documents);
    }

    @Test
    public void testQuery() {
        Bson filter = new Document("testField", "testValue");
        ArrayList<Document> documents = dataStore.query(filter, "ussd_db:testCollection");
        assertNotNull(documents);
    }

    @Test
    public void testAsk() {
        Document data = new Document("testField", "testValue");
        dataStore.store(data, "ussd_db:testCollection");
        Bson filter = new Document("testField", "testValue");
        Document document = dataStore.ask(filter, "ussd_db:testCollection");
        assertNotNull(document);
    }

    @Test
    public void testAggregate() {
        List<Bson> pipeline = new ArrayList<>();
        pipeline.add(new Document("$match", new Document("testField", "testValue")));
        ArrayList<Document> documents = dataStore.aggregate(pipeline, "ussd_db:testCollection");
        assertNotNull(documents);
    }

    @Test
    public void testUpsert() {
        Document filter = new Document("testField", "testValue");
        Document document = new Document("testField", "testValueUpdated");
        Document result = dataStore.upsert(filter, document, "ussd_db:testCollection");
        assertTrue(result.getBoolean("success"));
    }

    @Test
    public void testStoreMany() {
        List<Document> documents = new ArrayList<>();
        documents.add(new Document("testField1", "testValue1"));
        documents.add(new Document("testField2", "testValue2"));
        Document result = dataStore.store(documents, "ussd_db:testCollection");
        assertTrue(result.getBoolean("success"));
    }

    @Test
    public void testDelete() {
        // Insert a document to ensure it exists before deleting
        Document document = new Document("testField", "testValueToDelete");
        dataStore.store(document, "ussd_db:testCollection");

        // Define the filter to delete the inserted document
        Bson filter = new Document("testField", "testValueToDelete");

        // Perform the delete operation
        Document result = dataStore.delete(filter, "ussd_db:testCollection");
        assertTrue(result.getBoolean("success"));

        // Verify the document has been deleted
        Document deletedDocument = dataStore.ask(filter, "ussd_db:testCollection");
        assertNull(deletedDocument);
    }


}
