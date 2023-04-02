// is0xCollectiveDict
// COMP90015: Assignment1 - Multi-threaded Dictionary Server
// Developed By Yun-Chi Hsiao (1074004)
// GitHub: https://github.com/is0xjh25

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

public class DictionaryServer {
    private static final int DEFAULT_PORT = 4444;
    private static int CLIENT_COUNT;
    private Dictionary dc;
    private FileWriter myWriter;

    DictionaryServer() {
        CLIENT_COUNT = 0;
    }

    public void setDc(Dictionary dc) {
        this.dc = dc;
    }

    /* CLIENT HANDLER */
    private static class ClientHandler implements Runnable {
        private final DictionaryServer ds;
        private final Socket clientSocket;
        private final int clientNumber;

        public ClientHandler(DictionaryServer ds, Socket clientSocket, int clientNumber) {
            this.ds = ds;
            this.clientSocket = clientSocket;
            this.clientNumber = clientNumber;
        }

        @Override
        public void run() {
            try {
                String clientAddress = clientSocket.getRemoteSocketAddress().toString();
                ds.writeFile("[CONNECTION SUCCEED] -> " + clientNumber + clientAddress  + "\n");
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                BufferedWriter out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
                String clientQuery;

                try {
                    while ((clientQuery = in.readLine()) != null) {
                        String res = ds.handleQuery(clientQuery, clientNumber + clientSocket.getRemoteSocketAddress().toString());
                        out.write(res);
                        out.flush();
                    }
                    ds.writeFile("[CONNECTION CLOSED] -> " + clientNumber + clientSocket.getRemoteSocketAddress().toString() + "\n");
                } catch (IOException e) {
                    ds.writeFile(e.getMessage());
                }
            } catch (IOException e) {
                ds.writeFile(e.getMessage());
            }
        }
    }

    public synchronized String handleQuery(String s, String clientAddress) {
        Query request = new Query(Query.User.SERVER, s);
        Query response;
        // check the request from client is valid, then send the response.
        if (request.isValid()) {
            switch (request.action) {
                case CONNECT -> response = new Query(Query.Status.SUCCESS, "200", "Welcome to is0xCollectiveDict.", null);
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
            Files.createDirectories(Path.of("server-log"));
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("[LOG FILE DIRECTORY CREATING ERROR]");
        }

        try {
            File newFile = new File("server-log/" + dtf.format(now) + ".txt");
            newFile.createNewFile(); // if file already exists will do nothing.
            myWriter = new FileWriter(newFile, true);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("[LOG FILE CREATING ERROR]");
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
            System.out.print("Enter the MongoDB Password: "); // for IDE terminal.
            password = input.nextLine().toCharArray();
        } else {
            password = console.readPassword("Enter the MongoDB Password: "); // for terminal.
        }
        return password;
    }

    public static int setPort(String[] args) {
        int port = DEFAULT_PORT;
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.out.println("Invalid Port, using default [PORT:4444]");
            }
        }
        return port;
    }

    /* MAIN */
    public static void main(String[] args) {
        int port = setPort(args);
        DictionaryServer ds = new DictionaryServer();
        ds.openFile();
        Dictionary dc = new Dictionary(ds, readPassword());
        ds.setDc(dc);

        // start the server.
        try {
            ServerSocket listeningSocket = new ServerSocket(port);
            listeningSocket.setReuseAddress(true);
            ds.writeFile("[LISTENING] -> " + listeningSocket.getLocalSocketAddress().toString() + "\n");
            while (true) {
                Socket clientSocket = listeningSocket.accept();
                CLIENT_COUNT++;
                ClientHandler clientSock = new ClientHandler(ds, clientSocket, CLIENT_COUNT);
                new Thread(clientSock).start();
            }
        } catch (IOException e) {
            ds.writeFile(e.getMessage());
        }

        ds.closeFile();
    }
}
