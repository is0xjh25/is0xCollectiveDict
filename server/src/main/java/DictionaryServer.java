import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.Scanner;

public class DictionaryServer {

    private static int DEFAULT_PORT = 4444;
    private int port;
    private Dictionary dc;
    private FileWriter myWriter;
    private ServerSocket listeningSocket;
    private Socket clientSocket;
    HashMap<String, Date> connectionRecords;

    DictionaryServer(int port, char[] password) {
        this.port = port;
        openFile();
        dc = new Dictionary(this, password);
    }

    public void run() {
        try {
            listeningSocket = new ServerSocket(port);
            int i = 0;
            while (true) {
                writeFile("---------------------");
                writeFile("[LISTENING] -> " + listeningSocket.getLocalSocketAddress().toString());
                clientSocket = listeningSocket.accept();
                i++;
                writeFile("[CONNECTION SUCCEED] -> " + i + clientSocket.getRemoteSocketAddress().toString());
                writeFile("---------------------\n");
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                BufferedWriter out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
                String clientQuery;
                try {
                    while((clientQuery = in.readLine()) != null) {
                        String res = handleQuery(clientQuery, i + clientSocket.getRemoteSocketAddress().toString());
                        out.write(res);
                        out.flush();
                    }
                    writeFile("[CONNECTION CLOSED] -> " + i + clientSocket.getRemoteSocketAddress().toString());
                } catch(SocketException e) {
                    writeFile(e.getMessage());
                }
                clientSocket.close();
            }
        } catch (IOException e) {
            writeFile(e.getMessage());
        } finally {
            if (listeningSocket != null) {
                try {
                    listeningSocket.close();
                } catch (IOException e) {
                    writeFile(e.getMessage());
                }
            }
        }
    }

    public String handleQuery(String s, String clientAddress) {
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

        writeFile(Query.logPair(request, response, clientAddress));
        return response.stringify();
    }

    /* LOG FILE OPERATION */
    private void openFile() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd-HHmmss");
        LocalDateTime now = LocalDateTime.now();

        try {
            Files.createDirectories(Path.of("log"));
        } catch (IOException e) {
            writeFile("[LOG FILE DIRECTORY CREATING ERROR]");
        }

        try {
            File newFile = new File("log/" + dtf.format(now) + ".txt");
            newFile.createNewFile(); // if file already exists will do nothing
            myWriter = new FileWriter(newFile, true);
        } catch (IOException e) {
            writeFile("[LOG FILE CREATING ERROR]");
        }
    }

    public void writeFile(String s) {
        try {
            myWriter.write(s+"\n");
            myWriter.flush();
            System.out.println(s);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("[LOG FILE OPENING ERROR]");
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

    /* HELPER FUNCTIONS */
    public static char[] readPassword() {
        Scanner input = new Scanner(System.in);
        Console console = System.console();
        char[] password;
        if (console == null) {
            System.out.print("Enter password: ");
            password = input.nextLine().toCharArray();
        } else {
            password = console.readPassword("Enter password: ");
            System.out.println("");
        }
        return password;
    }

    public static char[] erasePassword(char[] password) {
        for (int i=0; i<password.length; i++) {
            password[i] = 'x';
        }
        return password;
    }

    public static int setPort(String[] args) {
        int port = DEFAULT_PORT;
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                port = DEFAULT_PORT;
                System.out.println("Invalid port, using default \"PORT:4444\"");
            }
        }
        return port;
    }


    /* MAIN */
    public static void main(String[] args) {
        int port = setPort(args);
        char[] password = readPassword();
        DictionaryServer ds = new DictionaryServer(port, password);
        erasePassword(password);
        ds.run();
        ds.closeFile();
    }
}
