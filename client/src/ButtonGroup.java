// is0xCollectiveDict
// COMP90015: Assignment1 - Multi-threaded Dictionary Server
// Developed By Yun-Chi Hsiao (1074004)
// GitHub: https://github.com/is0xjh25

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

public class ButtonGroup extends JPanel {
    PageManager pm;
    HashMap<String, JButton> buttons;
    JLabel warning;

    ButtonGroup(PageManager pm) {
        this.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
        this.setPreferredSize(new Dimension(400, 25));
        this.setBackground(Color.BLACK);
        setPm(pm);
        setWarning(new JLabel("", SwingConstants.CENTER));
        getWarning().setPreferredSize(new Dimension(400, 25));
        getWarning().setForeground(Color.ORANGE);
        this.add(getWarning());
        createButtons(); // set up all buttons.
        for (JButton b : getButtons().values()) {
            this.add(b);
        }
    }

    void createButtons() {
        List<String> buttonsName = Arrays.asList("connect", "update", "search", "add", "remove", "discard", "confirm", "menu", "reconnect");
        setButtons(new HashMap<>());
        for (String name : buttonsName)  {
            JButton b = new JButton(name.toUpperCase());
            b.setPreferredSize(new Dimension(100, 25));
            // set functionality for every button
            b.addActionListener(e -> {
                switch (name) {
                    case "connect" -> {
                        getPm().getDc().writeFile("[CONNECT]\n");
                        getPm().getDc().setAction(DictionaryClient.Page.CONNECT);
                        getPm().getDc().connect();
                    }
                    case "search" -> {
                        getPm().getDc().writeFile("[SEARCH]\n");
                        getPm().getDc().setAction(DictionaryClient.Page.SEARCH);
                        getPm().getDc().sendQuery();
                    }
                    case "update" -> {
                        getPm().getDc().writeFile("[UPDATE]\n");
                        getPm().getDc().setAction(DictionaryClient.Page.UPDATE);
                        getPm().pageControl(DictionaryClient.Page.UPDATE);
                    }
                    case "add" -> {
                        getPm().getDc().writeFile("[ADD]\n");
                        getPm().getDc().setAction(DictionaryClient.Page.ADD);
                        getPm().pageControl(DictionaryClient.Page.ADD);
                    }
                    case "remove" -> {
                        getPm().getDc().writeFile("[REMOVE]\n");
                        getPm().getDc().setAction(DictionaryClient.Page.REMOVE);
                        getPm().pageControl(DictionaryClient.Page.REMOVE);
                    }
                    case "discard" -> {
                        getPm().getDc().writeFile("[DISCARD]\n");
                        getPm().pageControl(DictionaryClient.Page.MENU);
                    }
                    case "confirm" -> {
                        getPm().getDc().writeFile("[CONFIRM]\n");
                        getPm().getDc().sendQuery();
                    }
                    case "menu" -> {
                        getPm().getDc().writeFile("[MENU]\n");
                        getPm().getDc().setAction(DictionaryClient.Page.MENU);
                        getPm().pageControl(DictionaryClient.Page.MENU);
                    }
                    case "reconnect" -> {
                        getPm().getDc().writeFile("[RECONNECT]\n");
                        getPm().getDc().setAction(DictionaryClient.Page.RECONNECT);
                        getPm().getDc().connect();
                    }
                }
            });
            buttons.put(name, b);
        }
    }

    void showWarning(String s) {
        for (JButton b : buttons.values()) {
            b.setVisible(false);
        }
        getWarning().setText(s);
        getWarning().setVisible(true);
    }

    void showButtons() {
        // hide all buttons.
        getWarning().setVisible(false);
        for (JButton b : getButtons().values()) {
            b.setVisible(false);
        }
        // determine if the entered text is available.
        if (getPm().getDc().getCurrentPage() == DictionaryClient.Page.WAITING) {
            showWarning("...anything worth having takes time...");
            return;
        } else if (getPm().getDc().getCurrentPage() == DictionaryClient.Page.UPDATE || getPm().getDc().getCurrentPage() == DictionaryClient.Page.ADD ) {
            if (getPm().getDc().getDefinition().length() <= 0) {
                showWarning("The definition cannot be blank!");
                return;
            }
        } else if ((getPm().getDc().getCurrentPage() == DictionaryClient.Page.CONNECT || getPm().getDc().getCurrentPage() == DictionaryClient.Page.RECONNECT)) {
            if (!validIP(getPm().getDc().getIpAndPort())) {
                showWarning("Invalid ip address and port!");
                return;
            }
        } else {
            if (getPm().getDc().getWord().length() <= 0) {
                showWarning("Type something, magic will do the rest.");
                return;
            } else if (!validWord(getPm().getDc().getWord())) {
                showWarning("Invalid word!");
                return;
            }
        }
        switch (getPm().getDc().getCurrentPage()) {
            case CONNECT:
                buttons.get("connect").setVisible(true);
                break;
            case MENU:
                buttons.get("search").setVisible(true);
                buttons.get("add").setVisible(true);
                break;
            case SEARCH:
                buttons.get("update").setVisible(true);
                buttons.get("remove").setVisible(true);
                buttons.get("menu").setVisible(true);
                break;
            case UPDATE:
            case ADD:
            case REMOVE:
                buttons.get("confirm").setVisible(true);
                buttons.get("discard").setVisible(true);
                break;
            case STATUS:
                buttons.get("menu").setVisible(true);
                break;
            case WAITING:
                break;
            case RECONNECT:
                buttons.get("reconnect").setVisible(true);
                break;
        }
    }

    /* GETTERS & SETTERS */
    public PageManager getPm() {
        return pm;
    }
    public void setPm(PageManager pm) {
        this.pm = pm;
    }
    public HashMap<String, JButton> getButtons() {
        return buttons;
    }
    public void setButtons(HashMap<String, JButton> buttons) {
        this.buttons = buttons;
    }
    public JLabel getWarning() {
        return warning;
    }
    public void setWarning(JLabel warning) {
        this.warning = warning;
    }

    /* HELPER FUNCTIONS */
    private Boolean validIP(String ip) {
        Pattern p = Pattern.compile("^(localhost|((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)):[0-9]{1,4}$");
        return p.matcher(ip).matches();
    }

    private Boolean validWord(String word) {
        Pattern p = Pattern.compile("[A-Za-z'-]+");
        return p.matcher(word).matches();
    }
}
