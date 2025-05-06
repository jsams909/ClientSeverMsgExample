package org.example.clientsevermsgexample;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ResourceBundle;

import static java.lang.Thread.sleep;

public class MainController implements Initializable {
    @FXML
    private ComboBox dropdownPort;

    @FXML
    private Button user1_client;

    @FXML
    private Button user2_server;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        dropdownPort.getItems().addAll("7",     // ping
                "13",     // daytime
                "21",     // ftp
                "23",     // telnet
                "71",     // finger
                "80",     // http
                "119",     // nntp (news)
                "161"      // snmp);
        );


        user1_client.setOnAction(this::openUser1Chat);
        user2_server.setOnAction(this::openUser2Chat);
    }

    @FXML
    private Button clearBtn;

    @FXML
    private TextArea resultArea;

    @FXML
    private Label server_lbl;

    @FXML
    private Button testBtn;

    @FXML
    private Label test_lbl;

    @FXML
    private TextField urlName;

    Socket socket1;

    Label lb122, lb12;
    TextField msgText;

    @FXML
    void checkConnection(ActionEvent event) {
        String host = urlName.getText();
        int port = Integer.parseInt(dropdownPort.getValue().toString());

        try {
            Socket sock = new Socket(host, port);
            resultArea.appendText(host + " listening on port " + port + "\n");
            sock.close();
        } catch (UnknownHostException e) {
            resultArea.setText(String.valueOf(e) + "\n");
            return;
        } catch (Exception e) {
            resultArea.appendText(host + " not listening on port "
                    + port + "\n");
        }
    }

    @FXML
    void clearBtn(ActionEvent event) {
        resultArea.setText("");
        urlName.setText("");
    }

    @FXML
    void startServer(ActionEvent event) {
        Stage stage = new Stage();
        Group root = new Group();
        Label lb11 = new Label("Server");
        lb11.setLayoutX(100);
        lb11.setLayoutY(100);

        lb12 = new Label("info");
        lb12.setLayoutX(100);
        lb12.setLayoutY(200);
        root.getChildren().addAll(lb11, lb12);
        Scene scene = new Scene(root, 600, 350);
        stage.setScene(scene);
        lb12.setText("Server is running and waiting for a client...");

        stage.setTitle("Server");
        stage.show();

        new Thread(this::runServer).start();
    }

    String message;

    private void runServer() {
        try {
            ServerSocket serverSocket = new ServerSocket(6666);
            updateServer("Server is running and waiting for a client...");
            while (true) { // Infinite loop
                try {
                    Socket clientSocket = serverSocket.accept();
                    updateServer("Client connected!");

                    new Thread(() -> {
                        try {
                            sleep(3000);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    });
                    DataInputStream dis = new DataInputStream(clientSocket.getInputStream());
                    DataOutputStream dos = new DataOutputStream(clientSocket.getOutputStream());

                    message = dis.readUTF();
                    updateServer("Message from client: " + message);


                    dos.writeUTF("Received: " + message);

                    dis.close();
                    dos.close();

                } catch (IOException e) {
                    updateServer("Error: " + e.getMessage());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                if (message.equalsIgnoreCase("exit")) break;
            }
        } catch (IOException e) {
            updateServer("Error: " + e.getMessage());
        }
    }

    private void updateServer(String message) {
        // Run on the UI thread
        javafx.application.Platform.runLater(() -> lb12.setText(message + "\n"));
    }

    @FXML
    void startClient(ActionEvent event) {
        Stage stage = new Stage();
        Group root = new Group();
        Button connectButton = new Button("Connect to server");
        connectButton.setLayoutX(100);
        connectButton.setLayoutY(300);
        connectButton.setOnAction(this::connectToServer);


        Label lb11 = new Label("Client");
        lb11.setLayoutX(100);
        lb11.setLayoutY(100);
        msgText = new TextField("msg");
        msgText.setLayoutX(100);
        msgText.setLayoutY(150);

        lb122 = new Label("info");
        lb122.setLayoutX(100);
        lb122.setLayoutY(200);
        root.getChildren().addAll(lb11, lb122, connectButton, msgText);

        Scene scene = new Scene(root, 600, 350);
        stage.setScene(scene);
        stage.setTitle("Client");
        stage.show();
    }

    private void connectToServer(ActionEvent event) {
        try {
            socket1 = new Socket("localhost", 6666);

            DataOutputStream dos = new DataOutputStream(socket1.getOutputStream());
            DataInputStream dis = new DataInputStream(socket1.getInputStream());

            dos.writeUTF(msgText.getText());
            String response = dis.readUTF();
            updateTextClient("Server response: " + response + "\n");

            dis.close();
            dos.close();
            socket1.close();
        } catch (Exception e) {
            updateTextClient("Error: " + e.getMessage() + "\n");
        }
    }

    private void updateTextClient(String message) {

        javafx.application.Platform.runLater(() -> lb122.setText(message + "\n"));
    }


    private void openUser1Chat(ActionEvent event) {
        openChatWindow("User 1", false);
    }

    private void openUser2Chat(ActionEvent event) {
        openChatWindow("User 2", true);
    }

    private void openChatWindow(String username, boolean isServer) {
        try {

            createChatWindow(username, isServer);
        } catch (Exception e) {
            e.printStackTrace();
            resultArea.appendText("Error opening chat window: " + e.getMessage() + "\n");
        }
    }

    private void createChatWindow(String username, boolean isServer) {

        Stage stage = new Stage();
        javafx.scene.layout.BorderPane root = new javafx.scene.layout.BorderPane();
        root.setPadding(new javafx.geometry.Insets(10));


        Label statusLabel = new Label("Status: Initializing...");
        root.setTop(statusLabel);


        TextArea chatArea = new TextArea();
        chatArea.setEditable(false);
        chatArea.setWrapText(true);
        root.setCenter(chatArea);


        javafx.scene.layout.HBox messageBox = new javafx.scene.layout.HBox(10);
        TextField messageField = new TextField();
        messageField.setPrefWidth(400);
        Button sendButton = new Button("Send");
        messageBox.getChildren().addAll(messageField, sendButton);


        Label userLabel = new Label("User: " + username + (isServer ? " (Server)" : " (Client)"));

        javafx.scene.layout.VBox bottomBox = new javafx.scene.layout.VBox(10);
        bottomBox.getChildren().addAll(messageBox, userLabel);
        bottomBox.setPadding(new javafx.geometry.Insets(10, 0, 0, 0));
        root.setBottom(bottomBox);


        Scene scene = new Scene(root, 600, 400);
        stage.setTitle("Chat - " + username);
        stage.setScene(scene);

        final Socket[] socket = new Socket[1];
        final ServerSocket[] serverSocket = new ServerSocket[1];
        final DataInputStream[] inputStream = new DataInputStream[1];
        final DataOutputStream[] outputStream = new DataOutputStream[1];
        final Thread[] receiveThread = new Thread[1];
        final boolean[] running = {true};


        Runnable appendToChatArea = (Runnable) () -> {
            return;
        };


        if (isServer) {
            new Thread(() -> {
                try {
                    serverSocket[0] = new ServerSocket(7777);
                    javafx.application.Platform.runLater(() ->
                            statusLabel.setText("Status: Server started. Waiting for client connection..."));

                    socket[0] = serverSocket[0].accept();
                    javafx.application.Platform.runLater(() ->
                            statusLabel.setText("Status: Client connected!"));


                    inputStream[0] = new DataInputStream(socket[0].getInputStream());
                    outputStream[0] = new DataOutputStream(socket[0].getOutputStream());


                    receiveThread[0] = new Thread(() -> {
                        while (running[0] && socket[0] != null && !socket[0].isClosed()) {
                            try {
                                String message = inputStream[0].readUTF();
                                final String msg = message;
                                javafx.application.Platform.runLater(() -> {
                                    chatArea.appendText("Other: " + msg + "\n");
                                    chatArea.setScrollTop(Double.MAX_VALUE);
                                });
                            } catch (IOException e) {
                                if (running[0]) {
                                    javafx.application.Platform.runLater(() ->
                                            statusLabel.setText("Status: Connection lost - " + e.getMessage()));
                                    e.printStackTrace();
                                    break;
                                }
                            }
                        }
                    });
                    receiveThread[0].setDaemon(true);
                    receiveThread[0].start();

                } catch (IOException e) {
                    javafx.application.Platform.runLater(() ->
                            statusLabel.setText("Status: Server error - " + e.getMessage()));
                    e.printStackTrace();
                }
            }).start();
        } else {

            new Thread(() -> {
                try {
                    javafx.application.Platform.runLater(() ->
                            statusLabel.setText("Status: Attempting to connect to server..."));


                    int maxRetries = 5;
                    int retryCount = 0;
                    boolean connected = false;

                    while (!connected && retryCount < maxRetries) {
                        try {
                            socket[0] = new Socket("localhost", 7777);
                            connected = true;

                            javafx.application.Platform.runLater(() ->
                                    statusLabel.setText("Status: Connected to server!"));


                            inputStream[0] = new DataInputStream(socket[0].getInputStream());
                            outputStream[0] = new DataOutputStream(socket[0].getOutputStream());


                            receiveThread[0] = new Thread(() -> {
                                while (running[0] && socket[0] != null && !socket[0].isClosed()) {
                                    try {
                                        String message = inputStream[0].readUTF();
                                        final String msg = message;
                                        javafx.application.Platform.runLater(() -> {
                                            chatArea.appendText("Other: " + msg + "\n");
                                            chatArea.setScrollTop(Double.MAX_VALUE);
                                        });
                                    } catch (IOException e) {
                                        if (running[0]) {
                                            javafx.application.Platform.runLater(() ->
                                                    statusLabel.setText("Status: Connection lost - " + e.getMessage()));
                                            break;
                                        }
                                    }
                                }
                            });
                            receiveThread[0].setDaemon(true);
                            receiveThread[0].start();

                        } catch (IOException e) {
                            retryCount++;
                            int finalRetryCount = retryCount;
                            javafx.application.Platform.runLater(() ->
                                    statusLabel.setText("Status: Connection attempt " + finalRetryCount +
                                            " failed. Retrying in 2 seconds..."));
                            try {
                                Thread.sleep(2000);
                            } catch (InterruptedException ie) {
                                Thread.currentThread().interrupt();
                            }
                        }
                    }

                    if (!connected) {
                        javafx.application.Platform.runLater(() ->
                                statusLabel.setText("Status: Failed to connect after " + maxRetries +
                                        " attempts. Is the server running?"));
                    }

                } catch (Exception e) {
                    javafx.application.Platform.runLater(() ->
                            statusLabel.setText("Status: Connection error - " + e.getMessage()));
                    e.printStackTrace();
                }
            }).start();
        }


        sendButton.setOnAction(e -> {
            String message = messageField.getText().trim();
            if (!message.isEmpty() && socket[0] != null && !socket[0].isClosed()) {
                try {
                    outputStream[0].writeUTF(message);
                    chatArea.appendText("You: " + message + "\n");
                    chatArea.setScrollTop(Double.MAX_VALUE);
                    messageField.clear();
                } catch (IOException ex) {
                    statusLabel.setText("Status: Failed to send message - " + ex.getMessage());
                    ex.printStackTrace();
                }
            }
        });


        stage.setOnCloseRequest(e -> {
            running[0] = false;
            try {
                if (inputStream[0] != null) inputStream[0].close();
                if (outputStream[0] != null) outputStream[0].close();
                if (socket[0] != null && !socket[0].isClosed()) socket[0].close();
                if (serverSocket[0] != null && !serverSocket[0].isClosed()) serverSocket[0].close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });


        stage.show();
    }
}