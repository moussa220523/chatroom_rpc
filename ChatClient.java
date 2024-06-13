import org.apache.xmlrpc.XmlRpcClient;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.net.URL;
import java.util.Vector;

import java.util.Timer;
import java.util.TimerTask;


public class ChatClient {
    private String pseudo;
    private JFrame window;
    private JTextArea txtOutput;
    private JTextField txtMessage;
    private XmlRpcClient server;

    public ChatClient() {
        this.createIHM();
        this.requestPseudo();
        this.startPolling();
    }

    public void createIHM() {
        window = new JFrame("Chatroom");
        txtOutput = new JTextArea();
        txtMessage = new JTextField();
        JButton btnSend = new JButton("Envoyer");

        JPanel panel = (JPanel) window.getContentPane();
        JScrollPane sclPane = new JScrollPane(txtOutput);
        panel.add(sclPane, BorderLayout.CENTER);
        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.add(txtMessage, BorderLayout.CENTER);
        southPanel.add(btnSend, BorderLayout.EAST);
        panel.add(southPanel, BorderLayout.SOUTH);

        window.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        window.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                window_windowClosing(e);
            }
        });
        btnSend.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                btnSend_actionPerformed(e);
            }
        });
        txtMessage.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent event) {
                if (event.getKeyChar() == '\n')
                    btnSend_actionPerformed(null);
            }
        });

        txtOutput.setBackground(new Color(220, 220, 220));
        txtOutput.setEditable(false);
        window.setSize(500, 400);
        window.setVisible(true);
        txtMessage.requestFocus();
    }

    public void requestPseudo() {
        this.pseudo = JOptionPane.showInputDialog(
                this.window, "Entrez votre pseudo : ",
                "Chatroom", JOptionPane.OK_OPTION);

        if (this.pseudo == null)
            System.exit(0);

        try {
            server = new XmlRpcClient(new URL("http://localhost:8080/RPC2"));
            Vector<String> params = new Vector<>();
            params.add(pseudo);
            server.execute("ChatRoomServer.subscribe", params);
        } catch (Exception ex) {
            System.err.println("Erreur côté client : " + ex.toString());
            ex.printStackTrace();
        }
    }

    public void window_windowClosing(WindowEvent e) {
        try {
            Vector<String> params = new Vector<>();
            params.add(pseudo);
            server.execute("ChatRoomServer.unsubscribe", params);
            System.exit(-1);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void btnSend_actionPerformed(ActionEvent e) {
        try {
            Vector<String> params = new Vector<>();
            params.add(pseudo);
            params.add(this.txtMessage.getText());
            server.execute("ChatRoomServer.postMessage", params);
        } catch (Exception ex) {
            System.err.println("Erreur côté client : " + ex.toString());
            ex.printStackTrace();
        }
        this.txtMessage.setText("");
        this.txtMessage.requestFocus();
    }

    public void displayMessage(String message) {
        txtOutput.append(message + "\n");
    }

    public void startPolling() {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                pollMessages();
            }
        }, 0, 1000);
    }

    public void pollMessages() {
        try {
            Vector<String> params = new Vector<>();
            params.add(pseudo);
            Vector<?> messages = (Vector<?>) server.execute("ChatRoomServer.getMessages", params);
            for (Object message : messages) {
                displayMessage((String) message);
            }
        } catch (Exception ex) {
            System.err.println("Erreur lors de la récupération des messages : " + ex.toString());
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new ChatClient();
    }
}
