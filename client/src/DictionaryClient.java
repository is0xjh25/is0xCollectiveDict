// is0xCollectiveDict
// COMP90015: Assignment1 - Multi-threaded Dictionary Server
// Developed By Yun-Chi Hsiao (1074004)
// GitHub: https://github.com/is0xjh25

import java.awt.event.ActionListener;
import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DictionaryClient {
    final private static String DEFAULT_IP = "localhost";
    final private static String DEFAULT_PORT = "4444";

    private FileWriter myWriter;
    private Gui gui;
    private Socket socket;
    private String ipAndPort;
    private String word;
    private String definition;
    private String suggestion;
    private String message;
    private Page currentPage;
    private Page action;
    private BufferedReader in;
    private BufferedWriter out;
    private Query request;
    private Query response;
    String serverResponse;

    enum Page {
        CONNECT,
        MENU,
        UPDATE,
        SEARCH,
        ADD,
        REMOVE,
        STATUS,
        WAITING,
        CONNECTING,
        RECONNECT
    }

    DictionaryClient(String[] ipAndPort) {
        setCurrentPage(Page.CONNECT);
        setIpAndPort(ipAndPort[0] + ":" + ipAndPort[1]);
        setWord("");
        setMessage("");
        setGui(new Gui(this));
    }

    public void connect() {
        Runnable runnable = new Runnable() {
            public void run() {
                try {
                    setCurrentPage(Page.CONNECTING);
                    String ip = getIpAndPort().split(":")[0];
                    int port = Integer.parseInt(getIpAndPort().split(":")[1]);
                    setSocket(new Socket(ip, port));
                    setIn(new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8)));
                    setOut(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8)));
                    sendQuery();
                    writeFile("---------------------");
                    writeFile("[CONNECTING FROM] -> " + socket.getLocalSocketAddress().toString().replace("/", ""));
                    writeFile("[CONNECTING TO] -> " + getIpAndPort());
                    writeFile("---------------------\n");
                    while ((serverResponse = getIn().readLine()) != null) {
                        handleResponse(getServerResponse());
                    }
                    writeFile("[CONNECTION CLOSED] -> " + getIpAndPort());
                } catch (IOException e) {
                    getGui().getPm().getContent().setErrorMessageLabel(e.getMessage() + ".");
                    getGui().getPm().pageControl(Page.RECONNECT);
                    writeFile("[ERROR] -> " + e.getMessage() + ".\n");
                } finally {
                    if (getSocket() != null) {
                        try {
                            getSocket().close();
                        } catch (IOException e) {
                            writeFile("[ERROR] -> " + e.getMessage() + ".\n");
                        }
                    }
                }
            }
        };
        new Thread(runnable).start();
    }

    public void sendQuery() {
        if (getAction() == Page.CONNECT || getAction() == Page.RECONNECT) {
            getGui().getPm().pageControl(Page.CONNECTING);
        } else {
            getGui().getPm().pageControl(Page.WAITING);
        }

        switch (getAction()) {
            case CONNECT, RECONNECT-> setRequest(new Query(Query.Action.CONNECT, null, null));
            case UPDATE -> setRequest(new Query(Query.Action.UPDATE, getWord(), getDefinition()));
            case SEARCH -> setRequest(new Query(Query.Action.SEARCH, getWord(), null));
            case ADD -> setRequest(new Query(Query.Action.ADD, getWord(), getDefinition()));
            case REMOVE -> setRequest(new Query(Query.Action.REMOVE, getWord(), null));
        }

        try {
            getOut().write(getRequest().stringify());
            getOut().flush();
        } catch (IOException e) {
            getGui().getPm().getContent().setErrorMessageLabel(e.getMessage() + ".");
            writeFile("[ERROR] -> " + e.getMessage() + ".\n");
            getGui().getPm().pageControl(Page.RECONNECT);
        }
    }

    public void handleResponse(String s) {
        Query response = new Query(Query.User.CLIENT, s);
        setResponse(response);
        Page status;

        if (response.isValid()) {
            switch (response.status) {
                case SUCCESS:
                    switch (getAction()) {
                        case CONNECT, RECONNECT -> {
                            getGui().getPm().getContent().setSuccessMessageLabel(getResponse().content);
                            status = Page.MENU;
                        }
                        case SEARCH -> {
                            getGui().getPm().getContent().setDefinitionLabel(getResponse().content);
                            status = Page.SEARCH;
                        }
                        default -> {
                            getGui().getPm().getContent().setSuccessMessageLabel(getResponse().content);
                            getGui().getPm().getContent().setSuggestionLabel(getResponse().suggestion);
                            status = Page.STATUS;
                        }
                    }
                    break;
                case ERROR:
                    getGui().getPm().getContent().setErrorMessageLabel(getResponse().content);
                    getGui().getPm().getContent().setSuggestionLabel(getResponse().suggestion);
                    status = Page.STATUS;
                    break;
                default:
                    getGui().getPm().getContent().setErrorMessageLabel("Invalid response.");
                    getGui().getPm().getContent().setSuggestionLabel("Please report this issue.");
                    status = Page.STATUS;
                    break;
            }
        } else {
            getGui().getPm().getContent().setErrorMessageLabel("Invalid response.");
            getGui().getPm().getContent().setSuggestionLabel("Please report this issue.");
            status = Page.STATUS;
        }

        // log the full TCP transmission.
        writeFile(Query.logPair(getRequest(), getResponse(), getIpAndPort()));

        // set stage to status.
        ActionListener taskPerformer = evt -> {
            getGui().getPm().pageControl(status);
        };

        getGui().getPm().setTimer(2000, taskPerformer);
    }

    public void reset() {
        setWord("");
        setMessage("");
        setDefinition("");
        setSuggestion("");
        setCurrentPage(Page.MENU);
        setAction(Page.MENU);
    }

    /* GETTERS & SETTERS */
    public Gui getGui() {
        return gui;
    }
    public void setGui(Gui gui) {
        this.gui = gui;
    }
    public Socket getSocket() {
        return socket;
    }
    public void setSocket(Socket socket) {
        this.socket = socket;
    }
    public String getIpAndPort() {
        return ipAndPort;
    }
    public void setIpAndPort(String ipAndPort) {
        this.ipAndPort = ipAndPort;
    }
    public String getWord() {
        return word;
    }
    public void setWord(String word) {
        this.word = word;
    }
    public String getDefinition() {
        return definition;
    }
    public void setDefinition(String definition) {
        this.definition = definition;
    }
    public String getSuggestion() {
        return suggestion;
    }
    public void setSuggestion(String suggestion) {
        this.suggestion = suggestion;
    }
    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }
    public Page getCurrentPage() {
        return currentPage;
    }
    public void setCurrentPage(Page currentPage) {
        this.currentPage = currentPage;
    }
    public Page getAction() {
        return action;
    }
    public void setAction(Page action) {
        this.action = action;
    }
    public BufferedReader getIn() {
        return in;
    }
    public void setIn(BufferedReader in) {
        this.in = in;
    }
    public BufferedWriter getOut() {
        return out;
    }
    public void setOut(BufferedWriter out) {
        this.out = out;
    }
    public Query getRequest() {
        return request;
    }
    public void setRequest(Query request) {
        this.request = request;
    }
    public Query getResponse() {
        return response;
    }
    public void setResponse(Query response) {
        this.response = response;
    }
    public String getServerResponse() {
        return serverResponse;
    }
    public void setServerResponse(String serverResponse) {
        this.serverResponse = serverResponse;
    }

    /* LOG FILE OPERATION */
    public void openFile() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd-HHmmss");
        LocalDateTime now = LocalDateTime.now();

        try {
            Files.createDirectories(Path.of("client-log"));
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("[LOG FILE DIRECTORY CREATING ERROR]");
        }

        try {
            File newFile = new File("client-log/" + dtf.format(now) + ".txt");
            newFile.createNewFile(); // if file already exists will do nothing
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

    public void closeFile() {
        try {
            myWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("[LOG FILE CLOSING ERROR]");
        }
    }

    /* HELPER FUNCTIONS */
    public static String[] setUp(String[] args) {
        if (args.length != 2) {
            System.out.println("Invalid port, using default \"IP:PORT->localhost:4444\"");
            return new String[]{DEFAULT_IP, DEFAULT_PORT};
        }
        return args;
    }

    /* MAIN */
    public static void main(String[] args) {
        DictionaryClient dc = new DictionaryClient(setUp(args));
        dc.getGui().run();
    }
}