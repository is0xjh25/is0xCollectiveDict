import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

public class Dictionary {

    String fileURL;

    Dictionary(String fileURL) {
        this.fileURL = String.valueOf(getClass().getResourceAsStream(fileURL));
    }

    public Query update(String word, String definition) {
        Query.Status status;
        String code;
        String content;
        String suggestion;
        
        try {
            Properties dict = new Properties();
            dict.load(new FileReader(fileURL));
            if (dict.containsKey(word) && definition.length() > 0) {
                dict.setProperty(word, definition);
                dict.store(new FileOutputStream(fileURL), "Update: " + word);
                status = Query.Status.SUCCESS;
                code = "200";
                content = "The definition has been updated.";
                suggestion = null;
            } else if (dict.containsKey(word) && definition.trim().length() <= 0){
                status = Query.Status.ERROR;
                code = "400";
                content = "Definition cannot be empty.";
                suggestion = "Please report this issue.";
            } else {
                status = Query.Status.ERROR;
                code = "204";
                content = "The word is NOT FOUND.";
                suggestion = "Please report this issue.";
            }
        } catch (IOException e) {
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
            Properties dict = new Properties();
            dict.load(new FileReader(fileURL));
            if (dict.getProperty(word) == null) {
                status = Query.Status.ERROR;
                code = "204";
                content = "The word is NOT FOUND.";
                String similarWord = similarWord(dict, word);
                if (similarWord == null) {
                    suggestion = "Add the word to OpenSourced Dictionary.";
                } else {
                    suggestion = "Are you looking for " + "\"" + similarWord+ "\"?";
                }
            } else {
                status = Query.Status.SUCCESS;
                code = "200";
                content = dict.getProperty(word);
                suggestion = null;
            }
        } catch (IOException e) {
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
            Properties dict = new Properties();
            dict.load(new FileReader(fileURL));
            if (dict.containsKey(word)) {
                status = Query.Status.ERROR;
                code = "409";
                content = "The word is already exist.";
                suggestion = "Please use update to edit the definition.";
            } else if (!dict.containsKey(word) && definition.trim().length() <= 0){
                status = Query.Status.ERROR;
                code = "400";
                content = "Definition cannot be empty.";
                suggestion = "Please report this issue.";
            } else {
                dict.setProperty(word, definition);
                dict.store(new FileOutputStream(fileURL), "Add: " + word);
                status = Query.Status.SUCCESS;
                code = "200";
                content = "The definition has been added.";
                suggestion = null;
            }
        } catch (IOException e) {
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
            Properties dict = new Properties();
            dict.load(new FileReader(fileURL));
            if (dict.containsKey(word)) {
                dict.remove(word);
                dict.store(new FileOutputStream(fileURL), "Remove: " + word);
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
        } catch (IOException e) {
            status = Query.Status.ERROR;
            code = "500";
            content = e.getMessage();
            suggestion = "Please report this issue.";
        }

        return new Query(status, code, content, suggestion);
    }

    /* HELPER FUNCTION */
    private String similarWord(Properties dict, String word) {
        word = word.toLowerCase();
        char[] chars = word.toCharArray();
        for (int i = 0, n = (int) Math.pow(2, chars.length); i < n; i++) {
            char[] permutation = new char[chars.length];
            for (int j =0; j < chars.length; j++) {
                permutation[j] = (isBitSet(i, j)) ? Character.toUpperCase(chars[j]) : chars[j];
            }

            if (dict.containsKey(String.valueOf(permutation))) return String.valueOf(permutation);
        }
        return null;
    }

    private boolean isBitSet(int n, int offset) {
        return (n >> offset & 1) != 0;
    }
}
