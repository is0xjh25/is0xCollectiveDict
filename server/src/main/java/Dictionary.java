import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import com.mongodb.*;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.regex.Pattern;

import static com.mongodb.client.model.Filters.eq;

public class Dictionary {

    private MongoCollection<Document> collection;
    private DictionaryServer ds;
    final private String keyName = "word";
    final private String valueName = "definition";
    final private String databaseName = "dictionary";
    final private String collectionName = "vocabularies";

    Dictionary(DictionaryServer ds, char[] password) {
        this.ds = ds;
        connectDatabase(password);
    }

    private void connectDatabase(char[] password) {
        ((LoggerContext) LoggerFactory.getILoggerFactory()).getLogger("org.mongodb.driver").setLevel(Level.WARN);
        ConnectionString connectionString = new ConnectionString("mongodb+srv://" + "is0xjh25" + ":" + String.valueOf(password) + "@cluster0.8ucd1at.mongodb.net/?retryWrites=true&w=majority");
        MongoClientSettings settings = MongoClientSettings.builder().applyConnectionString(connectionString).serverApi(ServerApi.builder().version(ServerApiVersion.V1).build()).build();
        MongoClient mongoClient = MongoClients.create(settings);
        MongoDatabase db = mongoClient.getDatabase(databaseName);
        collection = db.getCollection(collectionName);
        ds.writeFile("[DATABASE CONNECTED]");
    }


    public Query update(String word, String definition) {
        Query.Status status;
        String code;
        String content;
        String suggestion;
        try {
            Document doc = collection.find(eq(keyName, word)).first();
            if (doc != null) {
                if (definition == null) {
                    status = Query.Status.ERROR;
                    code = "400";
                    content = "Definition cannot be empty.";
                    suggestion = "Please report this issue.";
                } else {
                    collection.updateOne(doc, Updates.set(valueName, definition), new UpdateOptions().upsert(true));
                    status = Query.Status.SUCCESS;
                    code = "200";
                    content = "The definition has been updated.";
                    suggestion = null;
                }
            } else {
                status = Query.Status.ERROR;
                code = "204";
                content = "The word is NOT FOUND.";
                suggestion = "Please report this issue.";
            }
        } catch (MongoWriteException e) {
            ds.writeFile(e.getMessage());
            status = Query.Status.ERROR;
            code = "500";
            content = e.getMessage() + ".";
            suggestion = "Please report this issue.";
        }


        return new Query(status, code, content, suggestion);
    }

    public Query search(String word) {
        Query.Status status;
        String code;
        String content;
        String suggestion;

        try {
            Document doc = collection.find(eq(keyName, word)).first();
            if (doc != null) {
                status = Query.Status.SUCCESS;
                code = "200";
                content = doc.get(valueName).toString();
                suggestion = null;
            } else {
                status = Query.Status.ERROR;
                code = "204";
                content = "The word is NOT FOUND.";
                Pattern regex = Pattern.compile("^"+word+"$", Pattern.CASE_INSENSITIVE);
                Document similarDoc = collection.find(eq(keyName, regex)).first();
                if (similarDoc == null) {
                    suggestion = "Add the word to OpenSourced Dictionary.";
                } else {
                    String similarWord = similarDoc.get(keyName).toString();
                    suggestion = "Are you looking for " + "\"" + similarWord + "\"?";
                }
            }
        } catch (MongoWriteException e) {
            ds.writeFile(e.getMessage());
            status = Query.Status.ERROR;
            code = "500";
            content = e.getMessage() + ".";
            suggestion = "Please report this issue.";
        }

        return new Query(status, code, content, suggestion);
    }

    public Query add(String word, String definition) {
        Query.Status status;
        String code;
        String content;
        String suggestion;

        try {
            Document doc = collection.find(eq(keyName, word)).first();
            if (doc != null) {
                status = Query.Status.ERROR;
                code = "409";
                content = "The word is already exist.";
                suggestion = "Please use update to edit the definition.";
            } else if (definition == null) {
                status = Query.Status.ERROR;
                code = "400";
                content = "Definition cannot be empty.";
                suggestion = "Please report this issue.";
            } else {
                Document newDoc = new Document().append(keyName, word).append(valueName, definition);
                collection.insertOne(newDoc);
                status = Query.Status.SUCCESS;
                code = "200";
                content = "The definition has been added.";
                suggestion = null;
            }
        } catch (MongoWriteException e) {
            ds.writeFile(e.getMessage());
            status = Query.Status.ERROR;
            code = "500";
            content = e.getMessage() + ".";
            suggestion = "Please report this issue.";
        }

        return new Query(status, code, content, suggestion);
    }

    public Query remove(String word) {
        Query.Status status;
        String code;
        String content;
        String suggestion;

        try {
            Document doc = collection.find(eq(keyName, word)).first();
            if (doc != null) {
                collection.deleteOne(eq(keyName, word));
                status = Query.Status.SUCCESS;
                code = "200";
                content = "The word has been removed.";
                suggestion = null;
            } else {
                status = Query.Status.ERROR;
                code = "204";
                content = "The word is NOT FOUND.";
                suggestion = "Please report this issue.";
            }
        } catch (MongoWriteException e) {
            ds.writeFile(e.getMessage());
            status = Query.Status.ERROR;
            code = "500";
            content = e.getMessage() + ".";
            suggestion = "Please report this issue.";
        }

        return new Query(status, code, content, suggestion);
    }
}
