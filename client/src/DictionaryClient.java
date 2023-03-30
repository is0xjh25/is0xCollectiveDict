import java.awt.event.ActionListener;
import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class DictionaryClient {

    final private static String LOG_FILE_URL = "/log.txt";
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
                    while ((serverResponse = getIn().readLine()) != null) {
                        handleResponse(getServerResponse());
                    }
                } catch (IOException e) {
                    getGui().getPm().getContent().setErrorMessageLabel(e.getMessage() + ".");
                    getGui().getPm().pageControl(Page.RECONNECT);
                    System.out.println("[ERROR] -> " + e.getMessage());
                } finally {
                    if (getSocket() != null) {
                        try {
                            getSocket().close();
                        } catch (IOException e) {
                            System.out.println("[ERROR] -> " + e.getMessage());
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
            System.out.println(Query.logSingle(getRequest(), getIpAndPort()));
            getOut().write(getRequest().stringify());
            getOut().flush();
        } catch (IOException e) {
            getGui().getPm().getContent().setErrorMessageLabel(e.getMessage() + ".");
            System.out.println("[ERROR] -> " + e.getMessage());
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
        System.out.println(Query.logSingle(getResponse(), getIpAndPort()));
        writeFile(Query.logPair(getRequest(), getResponse(), getIpAndPort()));

        // set stage to status.
        ActionListener taskPerformer = evt -> {
            getGui().getPm().pageControl(status);
        };

        getGui().getPm().setTimer(1500, taskPerformer);
    }

    void reset() {
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
    void openFile() {
        try {
            myWriter = new FileWriter(String.valueOf(getClass().getResourceAsStream(LOG_FILE_URL)), true);
        } catch (IOException e) {
            myWriter = null;
            e.printStackTrace();
            System.out.println("[LOG FILE OPENING ERROR]");
        }
    }

    void writeFile(String s) {
        try {
            myWriter.write(s);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("[LOG FILE WRITING ERROR]");
        }
    }

    void closeFile() {
        try {
            myWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("[LOG FILE CLOSING ERROR]");
        }
    }

    /* THE MAIN METHOD */
    public static void main(String[] args) {
        String[] ip = {"localhost", "4444"};
        DictionaryClient dc = new DictionaryClient(ip);
        dc.getGui().run();
    }
}
