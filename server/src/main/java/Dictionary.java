// is0xCollectiveDict
// COMP90015: Assignment1 - Multi-threaded Dictionary Server
// Developed By Yun-Chi Hsiao (1074004)
// GitHub: https://github.com/is0xjh25

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
import org.bson.conversions.Bson;
import org.slf4j.LoggerFactory;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Pattern;
import static com.mongodb.client.model.Filters.eq;
import static java.util.concurrent.TimeUnit.SECONDS;

public class Dictionary {

    private static final String KEY_NAME = "word";
    private static final String VALUE_NAME = "definition";
    private static final String DATABASE_NAME = "dictionary";
    private static final String COLLECTION_NAME = "vocabularies";
    private static final String USER = "is0xjh25";
    private static final String CLUSTER = "cluster0.8ucd1at.mongodb.net";
    private static final String AUTH_MECHANISM = "retryWrites=true&w=majority";
    private final DictionaryServer ds;
    private MongoCollection<Document> collection;

    Dictionary(DictionaryServer ds, char[] password) {
        this.ds = ds;
        connectDatabase(password);
    }

    private void connectDatabase(char[] password) {
        ((LoggerContext) LoggerFactory.getILoggerFactory()).getLogger("org.mongodb.driver").setLevel(Level.WARN);

        try {
            ConnectionString connectionString = new ConnectionString("mongodb+srv://" + USER + ":" + String.valueOf(password) + "@" + CLUSTER + "/?" + AUTH_MECHANISM);
            MongoClientSettings settings = MongoClientSettings.builder()
                    .applyConnectionString(connectionString)
                    .serverApi(ServerApi.builder().version(ServerApiVersion.V1).build())
                    .applyToSocketSettings(builder -> builder.connectTimeout(5, SECONDS))
                    .build();
            MongoClient mongoClient = MongoClients.create(settings);
            MongoDatabase db = mongoClient.getDatabase(DATABASE_NAME);
            collection = db.getCollection(COLLECTION_NAME);
                DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
                LocalDateTime now = LocalDateTime.now();
                Bson ping = new BasicDBObject("ping", dtf.format(now));
                db.runCommand(ping);
                ds.writeFile("[DATABASE CONNECTED] -> " + dtf.format(now) + "\n");
        } catch (MongoSecurityException e) {
            ds.writeFile("[AUTHENTICATION FAILURE]" + "\n");
            connectDatabase(DictionaryServer.readPassword());
        }
    }

    public Query update(String word, String definition) {
        Query.Status status;
        String code;
        String content;
        String suggestion;

        try {
            Document doc = collection.find(eq(KEY_NAME, word)).first();
            if (doc != null) {
                if (definition == null) {
                    status = Query.Status.ERROR;
                    code = "400";
                    content = "Definition cannot be empty.";
                    suggestion = "Please report this issue.";
                } else {
                    collection.updateOne(doc, Updates.set(VALUE_NAME, definition), new UpdateOptions().upsert(true));
                    status = Query.Status.SUCCESS;
                    code = "200";
                    content = "The definition has been updated.";
                    suggestion = null;
                }
            } else {
                status = Query.Status.ERROR;
                code = "204";
                content = "The word is no longer exist.";
                suggestion = "The other user might just remove it.";
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
            Document doc = collection.find(eq(KEY_NAME, word)).first();
            if (doc != null) {
                status = Query.Status.SUCCESS;
                code = "200";
                content = doc.get(VALUE_NAME).toString();
                suggestion = null;
            } else {
                status = Query.Status.ERROR;
                code = "204";
                content = "The word is NOT FOUND.";
                Pattern regex = Pattern.compile("^"+word+"$", Pattern.CASE_INSENSITIVE);
                Document similarDoc = collection.find(eq(KEY_NAME, regex)).first();
                if (similarDoc == null) {
                    suggestion = "Add the word to CollectiveDict.";
                } else {
                    String similarWord = similarDoc.get(KEY_NAME).toString();
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
            Document doc = collection.find(eq(KEY_NAME, word)).first();
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
                Document newDoc = new Document().append(KEY_NAME, word).append(VALUE_NAME, definition);
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
            Document doc = collection.find(eq(KEY_NAME, word)).first();
            if (doc != null) {
                collection.deleteOne(eq(KEY_NAME, word));
                status = Query.Status.SUCCESS;
                code = "200";
                content = "The word has been removed.";
                suggestion = null;
            } else {
                status = Query.Status.ERROR;
                code = "204";
                content = "The word is no longer exist.";
                suggestion = "The other user might just remove it.";
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
