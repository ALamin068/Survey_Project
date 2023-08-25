package com.example.surveyclientserver;

import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javafx.application.Platform;

public class ClientController {
    @FXML
    private TextField txtUsername;
    @FXML
    private TextArea txtQuestion;
    @FXML
    private RadioButton rb1;
    @FXML
    private RadioButton rb2;
    @FXML
    private RadioButton rb3;
    @FXML
    private RadioButton rb4;
    @FXML
    private RadioButton rb5;
    @FXML
    private Button nextQuestion;
    
    @FXML
    private Button connectBtn;

    @FXML
    private Button startBtn;

    @FXML
    private Button quitButton;

    private PrintWriter out;

    private DataInputStream dataIn;

    private String[][] data1;

    private int index;

    private String userResponse;
    private String result;
    private ExecutorService pool;
    
    private Socket socket;

    public ClientController() {
    	pool = Executors.newFixedThreadPool(3);
        index = 0;
        //init();
    }

    private void init() {
    	new Thread(()->connectServer()).start();
    }
    
    private void connectServer() {
    	try {
    		socket = new Socket("localhost", 1234);
            out = new PrintWriter(socket.getOutputStream(), true);
            dataIn = new DataInputStream(socket.getInputStream());
            startBtn.setDisable(false);
        	quitButton.setDisable(false);
        	connectBtn.setDisable(true);
        } catch (IOException e) {
            showMessage(e.getMessage());
        }
    }
    
    @FXML
    protected void connectClick() {
    	startBtn.setDisable(true);
    	quitButton.setDisable(true);
    	init();
    }

    private int check() {
        if (rb1.isSelected())
            return 1;
        else if (rb2.isSelected())
            return 2;
        else if (rb3.isSelected())
            return 3;
        else if (rb4.isSelected())
            return 4;
        else if (rb5.isSelected())
            return 5;
        else
            return 0;
    }

    @FXML
    protected void startClick() throws IOException {
    	
        if (txtUsername.getText().isEmpty()) {
            showMessage("Please enter the username first!");
            return;
        }

        try {
            out = new PrintWriter(socket.getOutputStream(), true);
            dataIn = new DataInputStream(socket.getInputStream());
            result = "ans" + "+" + "yes";
            out.println(result);
            out.flush();
            String serverReply = dataIn.readLine();
            if ("survey_data".equalsIgnoreCase(serverReply)) {
                int dataSize = Integer.parseInt(dataIn.readLine());
                byte[] serializedData = new byte[dataSize];
                dataIn.readFully(serializedData);

                // Deserialize the data1 variable from the server
                ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(serializedData);
                ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
                data1 = (String[][]) objectInputStream.readObject();
                result = "opt";
                index = 0;
                if (data1 != null) {
                    showQuestion();
                } else {
                    showMessage("No data received");
                }
            }
        } catch (IOException ex) {
            showMessage(ex.getMessage());
        } catch (ClassNotFoundException ex) {
            showMessage(ex.getMessage());
        }
    }

    private void showQuestion() {
        ToggleGroup tgroup = new ToggleGroup();
        rb1.setToggleGroup(tgroup);
        rb2.setToggleGroup(tgroup);
        rb3.setToggleGroup(tgroup);
        rb4.setToggleGroup(tgroup);
        rb5.setToggleGroup(tgroup);
        rb1.setSelected(false);
        rb2.setSelected(false);
        rb3.setSelected(false);
        rb4.setSelected(false);
        rb5.setSelected(false);
        rb1.setVisible(false);
        rb2.setVisible(false);
        rb3.setVisible(false);
        rb4.setVisible(false);
        rb5.setVisible(false);
        if (index < data1.length) {
            txtUsername.setDisable(true);
            txtQuestion.setVisible(true);
            nextQuestion.setVisible(true);
            rb1.setVisible(true);
            rb2.setVisible(true);
            rb3.setVisible(true);
            txtQuestion.setText(data1[index][0]);
            rb1.setText(data1[index][1]);
            rb2.setText(data1[index][2]);
            rb3.setText(data1[index][3]);
            if (data1[index][4] == null || data1[index][4].isBlank()) {
                rb4.setSelected(false);
                rb4.setVisible(false);
            } else {
                rb4.setVisible(true);
                rb4.setText(data1[index][4]);
            }
            if (data1[index][5] == null || data1[index][5].isBlank()) {
                rb5.setSelected(false);
                rb5.setVisible(false);
            } else {
                rb5.setVisible(true);
                rb5.setText(data1[index][5]);
            }
            startBtn.setDisable(true);
            quitButton.setDisable(true);
        } else {
            showMessage("Successfully answered to all " + data1.length + " poll questions!");
            rb1.setVisible(false);
            rb2.setVisible(false);
            rb3.setVisible(false);
            rb4.setVisible(false);
            rb5.setVisible(false);
            txtQuestion.setText("You gave the following answers: \n" + userResponse);
            nextQuestion.setVisible(false);
            startBtn.setDisable(false);
            quitButton.setDisable(false);

            try {
				out = new PrintWriter(socket.getOutputStream(), true);
				out.println(userResponse);
	            out.flush();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
    }

    @FXML
    protected void quitSurvey() {
        try {

            out = new PrintWriter(socket.getOutputStream(), true);
            dataIn = new DataInputStream(socket.getInputStream());
            String result = "ans" + "+" + "exit";
            out.println(result);
            out.flush();
            String serverReply = dataIn.readLine();
            showMessage("Exiting");
            new Thread(()->exitScreen()).start();
        } catch (IOException ex) {
            showMessage(ex.getMessage());
        }
    }
    
    private void exitScreen() {
    	 Platform.exit();
    }

    @FXML
    protected void seeNext() {
        result = result + " = " + check();
        userResponse += "Question " + (index + 1) + ": " + data1[index][check()] + "\n";
        index++;
        showQuestion();
    }


    private void showMessage(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Program Message");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}