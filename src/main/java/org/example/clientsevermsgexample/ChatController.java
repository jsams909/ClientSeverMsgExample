package org.example.clientsevermsgexample;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.util.ResourceBundle;

public class ChatController implements Initializable {
    @FXML
    private TextArea chatArea;

    @FXML
    private TextField messageField;

    @FXML
    private Button sendButton;

    @FXML
    private Label statusLabel;

    @FXML
    private Label userLabel;

    private Socket socket;
    private ServerSocket serverSocket;
    private DataInputStream inputStream;
    private DataOutputStream outputStream;
    private boolean isServer;
    private String username;
    private boolean running;
    private Thread receiveThread;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    public void initializeAsServer(String username) {
        this.username = username;
        this.isServer = true;
        userLabel.setText("User: " + username + " (Server)");
        startServer();
    }

    public void initializeAsClient(String username) {
        this.username = username;
        this.isServer = false;
        userLabel.setText("User: " + username + " (Client)");
        connectToServer();
    }

    private void startServer() {
        running = true;
        new Thread(() -> {
            try {
                serverSocket = new ServerSocket(7777);
                updateStatus("Server started. Waiting for client connection...");

                socket = serverSocket.accept();
                updateStatus("Client connected!");

                setupCommunication();
            } catch (IOException e) {
                updateStatus("Server error: " + e.getMessage());
                e.printStackTrace();
            }
        }).start();
    }

    private void connectToServer() {
        running = true;
        new Thread(() -> {
            try {
                updateStatus("Connecting to server...");
                socket = new Socket("localhost", 7777);
                updateStatus("Connected to server!");

                setupCommunication();
            } catch (IOException e) {
                updateStatus("Connection error: " + e.getMessage());
                e.printStackTrace();
            }
        }).start();
    }

    private void setupCommunication() {
        try {
            inputStream = new DataInputStream(socket.getInputStream());
            outputStream = new DataOutputStream(socket.getOutputStream());

            startMessageReceiver();
        } catch (IOException e) {
            updateStatus("Communication setup error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void startMessageReceiver() {
        receiveThread = new Thread(() -> {
            while (running && socket != null && !socket.isClosed()) {
                try {
                    String message = inputStream.readUTF();
                    appendToChatArea("Other: " + message);
                } catch (IOException e) {
                    if (running) {
                        updateStatus("Connection lost: " + e.getMessage());
                        e.printStackTrace();
                        cleanUp();
                    }
                    break;
                }
            }
        });
        receiveThread.setDaemon(true);
        receiveThread.start();
    }

    @FXML
    void sendMessage(ActionEvent event) {
        String message = messageField.getText().trim();
        if (!message.isEmpty() && socket != null && !socket.isClosed()) {
            try {
                outputStream.writeUTF(message);
                appendToChatArea("You: " + message);
                messageField.clear();
            } catch (IOException e) {
                updateStatus("Failed to send message: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void updateStatus(String status) {
        Platform.runLater(() -> statusLabel.setText("Status: " + status));
    }

    private void appendToChatArea(String message) {
        Platform.runLater(() -> {
            chatArea.appendText(message + "\n");

            chatArea.setScrollTop(Double.MAX_VALUE);
        });
    }

    public void cleanUp() {
        running = false;

        try {
            if (inputStream != null) inputStream.close();
            if (outputStream != null) outputStream.close();
            if (socket != null && !socket.isClosed()) socket.close();
            if (serverSocket != null && !serverSocket.isClosed()) serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void closeWindow() {
        cleanUp();
    }
}