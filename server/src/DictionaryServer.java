import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Timer;

public class DictionaryServer {

    final private static String DICTIONARY_URL = "/resources/dictionary.properties";
    final private static String LOG_FILE_URL = "/resources/log.txt";
    private Dictionary dc;
    private FileWriter myWriter;
    ServerSocket listeningSocket;
    Socket clientSocket;
    HashMap<String, Date> connectionRecords;

    public static void main(String[] args) {
        DictionaryServer ds = new DictionaryServer();
        ds.run();
        ds.closeFile();
    }

    DictionaryServer() {
        init();
    }

    private void init() {
        dc = new Dictionary(DICTIONARY_URL);
        openFile();
    }

    public void run() {
        try {
            listeningSocket = new ServerSocket(4444);
            int i = 0;
            while (true) {
                System.out.println("---------------------");
                System.out.println("[LISTENING] -> " + listeningSocket.getLocalSocketAddress());
                clientSocket = listeningSocket.accept();
                i++;
                System.out.println("[CLIENT NUMBER] -> " + i);
                System.out.println("[REMOTE HOSTNAME:PORT] -> " + clientSocket.getRemoteSocketAddress());
                System.out.println("---------------------");
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                BufferedWriter out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
                String clientQuery;
                try {
                    while((clientQuery = in.readLine()) != null) {
                        String res = handleQuery(clientQuery);
                        out.write(res);
                        out.flush();
                    }
                    System.out.println("[CONNECTION CLOSED]");
                } catch(SocketException e) {
                    System.out.println(e.getMessage());
                }
                clientSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (listeningSocket != null) {
                try {
                    listeningSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public String handleQuery(String s) {
        Query request = new Query(Query.User.SERVER, s);
        Query response;
        // check the request from client is valid, then send the response.
        if (request.isValid()) {
            switch (request.action) {
                case CONNECT -> response = new Query(Query.Status.SUCCESS, "200", "Welcome to OpenSourced Dictionary.", null);
                case UPDATE -> response = dc.update(request.word, request.definition);
                case SEARCH -> response = dc.search(request.word);
                case ADD -> response = dc.add(request.word, request.definition);
                case REMOVE -> response = dc.remove(request.word);
                default -> response = new Query(Query.Status.ERROR, "400", "Invalid action.", "Please report this issue.");
            }
        } else {
            response = new Query(Query.Status.ERROR, "404", "Unavailable query.", "Please report this issue.");
        }

        System.out.println(Query.logPair(request, response, "localhost:4444"));
        writeFile(Query.logPair(request, response, "localhost:4444"));
        return response.stringify();
    }

    /* LOG FILE OPERATION */
    private void openFile() {
        try {
            myWriter = new FileWriter(String.valueOf(getClass().getResourceAsStream(LOG_FILE_URL)), true);
        } catch (IOException e) {
            myWriter = null;
            e.printStackTrace();
            System.out.println("[LOG FILE OPENING ERROR]");
        }
    }

    private void writeFile(String s) {
        try {
            myWriter.write(s);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("[LOG FILE WRITING ERROR]");
        }
    }

    private void closeFile() {
        try {
            myWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("[LOG FILE CLOSING ERROR]");
        }
    }
}
