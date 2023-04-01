// is0xCollectiveDict
// COMP90015: Assignment1 - Multi-threaded Dictionary Server
// Developed By Yun-Chi Hsiao (1074004)
// GitHub: https://github.com/is0xjh25

import org.json.JSONException;
import org.json.JSONObject;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Query extends JSONObject {
    Type type;
    User user;

    JSONObject response;
    Status status;
    String code;
    String content;
    String suggestion;

    JSONObject request;
    Action action;
    String word;
    String definition;

    enum User {
        CLIENT,
        SERVER
    }

    enum Type {
        RESPONSE,
        REQUEST
    }

    enum Status {
        SUCCESS,
        ERROR
    }

    enum Action {
        CONNECT,
        SEARCH,
        UPDATE,
        ADD,
        REMOVE
    }

    private void init() {
        type = null;
        user = null;
        response = null;
        status = null;
        code = null;
        content = null;
        suggestion = null;
        request = null;
        action = null;
        word = null;
        definition = null;
    }

    /* REQUEST SENT FROM CLIENT */
    Query(Action action, String word, String definition) {
        init();
        this.type = Type.REQUEST;
        this.user = User.CLIENT;
        this.action = action;
        this.word = word;
        this.definition = definition;
        this.request = new JSONObject();
        if (action != null) request.put("action", action);
        if (word != null) request.put("word", word);
        if (definition != null) request.put("definition", definition);
    }

    /* RESPONSE SENT FROM SERVER */
    Query(Status status, String code, String content, String suggestion) {
        init();
        this.type = Type.RESPONSE;
        this.user = User.SERVER;
        this.status = status;
        this.code = code;
        this.content = content;
        this.suggestion = suggestion;
        this.response = new JSONObject();
        if (status != null) response.put("status", status);
        if (code != null) response.put("code", code);
        if (content != null) response.put("content", content);
        if (suggestion != null) response.put("suggestion", suggestion);
    }

    /* RESPONSE & REQUEST */
    Query(User user, String s) {
        s = s.replace("\n", "");

        if (user == User.SERVER) {
            init();
            this.type = Type.REQUEST;
            this.user = User.SERVER;
            request = new JSONObject(s);
            action = Action.valueOf(getValue(request, "action"));
            word = getValue(request, "word");
            definition = getValue(request, "definition");
        } else if (user == User.CLIENT) {
            init();
            this.type = Type.RESPONSE;
            this.user = User.CLIENT;
            response = new JSONObject(s);
            this.status = Status.valueOf(getValue(response, "status"));
            this.code = getValue(response, "code");
            this.content = getValue(response, "content");
            this.suggestion = getValue(response, "suggestion");
        }
    }

    static public String getValue(JSONObject json, String key) throws JSONException {
        String res;
        try {
            res = json.getString(key);
        } catch (JSONException e) {
            res = null;
        }
        return res;
    }

    public Boolean isValid() {

        if (type == Type.REQUEST) {
            if (action == null) return false;
            switch (action) {
                case CONNECT -> {return true;}
                case UPDATE, ADD -> {if(word == null || definition == null) return false;}
                case SEARCH, REMOVE -> {if(word == null) return false;}
            }
        } else if (type == Type.RESPONSE) {
            return status != null && code != null && content != null;
        } else {
            return false;
        }

        return true;
    }

    /* FOR TCP CONNECTION */
    public String stringify() {
        String res = null;
        if (type == Type.RESPONSE) res = response.toString() + "\n";
        if (type == Type.REQUEST) res = request.toString() + "\n";
        return res;
    }

    /* LOG */
    static public String logSingle(Query q, String clientAddress) {
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        Date date = new Date();

        return formatter.format(date) + " | " + clientAddress  + "\n" +
                q.toString() + "\n";
    }

    static public String logPair(Query request, Query response, String clientAddress) {
        String divideLine = "---------------------\n";
        return divideLine + logSingle(request, clientAddress) + logSingle(response, clientAddress) + divideLine;
    }

    @Override
    public String toString() {
        String res = "[UNEXPECTED ERROR]";

        if (type == Type.RESPONSE) {
            res =
                    "Response -> [" +
                            "status=" + status +
                            " | code='" + code + '\'' +
                            " | content='" + content + '\'' +
                            " | suggestion='" + suggestion + '\'' +
                            ']';
        } else if (type == Type.REQUEST) {
            res =
                    "Request -> [" +
                            "action=" + action +
                            " | word='" + word + '\'' +
                            " | definition='" + definition + '\'' +
                            ']';
        }

        return res;
    }

    public static void main(String[] args) {
        System.out.println("This is Query Class designed for both Client and Server.");
    }
}

