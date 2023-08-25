package com.example.surveyclientserver;

import javafx.animation.AnimationTimer;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javafx.application.Platform;

public class ServerController {

    //private static String[][] data1 = new String[10][6];

    private List<QuestionInfo> lstQuestions;

    @FXML
    private Button startServer;
    @FXML
    private Button createForm;

    @FXML
    private TextField txtQuestion;

    @FXML
    private TextField txtOption1;

    @FXML
    private TextField txtOption2;

    @FXML
    private TextField txtOption3;

    @FXML
    private TextField txtOption4;

    @FXML
    private ListView<String> serverListView;

    private Socket          socket   = null;
    private ServerSocket server = null;
    
    private ExecutorService pool;

    public ServerController() {
        pool = Executors.newFixedThreadPool(3);
   
        lstQuestions = new ArrayList<>();
        initQuestions();
    }
    
    private void initQuestions() {
        QuestionInfo q = new QuestionInfo();
        q.setQuestion("Which protocol do you think is most effective for reliable data transmission in modern\n" +
                "data communications?");
        q.setOption1("Transmission Control Protocol (TCP)");
        q.setOption2("User Datagram Protocol (UDP)");
        q.setOption3("Datagram Congestion Transmission Protocol (SCTP)");
        lstQuestions.add(q);

        q = new QuestionInfo();
        q.setQuestion("What do you think is the most important aspect to consider when selecting a data\n" +
                "communication standard for a business?");
        q.setOption1("Speed of data transmission");
        q.setOption2("Security and data encryption");
        q.setOption3("Compatibility with existing infrastructure");
        q.setOption4("Cost effectiveness");
        q.setOption5("Scalability for future growth");
        lstQuestions.add(q);

        q = new QuestionInfo();
        q.setQuestion("What emerging technology do you believe will have the most significant impact on our\n" +
                "daily lives in the next decade?");
        q.setOption1("Artificial intelligence and machine learning");
        q.setOption2("Quantum computing");
        q.setOption3("blockchain and Cryptocurrency");
        q.setOption4("Autonomous Vehicles and Drones");
        lstQuestions.add(q);

        q = new QuestionInfo();
        q.setQuestion("Which feature is most important to you when choosing a new smartphone?");
        q.setOption1("Battery life");
        q.setOption2("Storage Capacity");
        q.setOption3("Camera Quality");
        q.setOption4("Price");
        q.setOption5("Screen Size / Resolution");
        lstQuestions.add(q);


        q = new QuestionInfo();
        q.setQuestion("What aspect of a video game is most important to you?");
        q.setOption1("Story line and narrative depth");
        q.setOption2("Graphics and visual quality");
        q.setOption3("Gameplay mechanics");
        q.setOption4("Game world immersion and detail");
        lstQuestions.add(q);

        q = new QuestionInfo();
        q.setQuestion("Which programming language do you find most versatile for a wide range of\n" +
                "applications?");
        q.setOption1("Python");
        q.setOption2("Java");
        q.setOption3("C++");
        lstQuestions.add(q);

        q = new QuestionInfo();
        q.setQuestion("What is your preferred dining choice");
        q.setOption1("Cooking at home");
        q.setOption2("Ordering takeout");
        q.setOption3("Dining out");
        lstQuestions.add(q);

        q = new QuestionInfo();
        q.setQuestion("Which form of entertainment do you prefer");
        q.setOption1("Movies");
        q.setOption2("Books");
        q.setOption3("Video games");
        q.setOption4("Board games");
        lstQuestions.add(q);
    }

    @FXML
    protected void startServer(){
    	if (!lstQuestions.isEmpty()) {
    		new Thread(()->activateServer()).start();
    	} else {
            showMessage("Please Create Survey Form First!!!");
        }
    }

    private void activateServer() {
        
        	try {
	            if (server != null && !server.isClosed()) {
	                showMessage("Server has already started");
	                serverListView.getItems().add("Server is already running...");
	                return;
	            } else {
	            	server = new ServerSocket(1234);
	            	server.setReuseAddress(true);
	            }
	            serverListView.getItems().add("Server has Started...");
	            serverListView.getItems().add("Waiting for client connections...");
            
                
                while (true) {
	                Socket client = server.accept();
	                //updateStatus("New client connected" + " " +
	                  //      client.getInetAddress().getHostAddress());
	                ClientHandler clientSock = new ClientHandler(client, this);
	                pool.execute(clientSock);
                }
                //ServerHandler serverHandler = new ServerHandler(server, this);
                //new Thread(serverHandler).start();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (server != null) {
                    try {
                        server.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
    }


     void showMessage(String msg) {
   
        Runnable updater = new Runnable() {

            @Override
            public void run() {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Server Message");
                alert.setHeaderText(null);
                alert.setContentText(msg);
                alert.showAndWait();
            }
        };
        Platform.runLater(updater);
    }

    protected void updateStatus(String message) {
    	
    	Runnable updater = new Runnable() {

            @Override
            public void run() {
            	serverListView.getItems().add(message);
            }
        };
        Platform.runLater(updater);
    	
        
    }

    @FXML
    protected void addQuestion() {
        if (txtQuestion.getText().isBlank()) {
            showMessage("Question is empty");
            return;
        }
        if (txtOption1.getText().isBlank()) {
            showMessage("Option 1 is empty");
            return;
        }
        if (txtOption2.getText().isBlank()) {
            showMessage("Option 2 is empty");
            return;
        }
        if (txtOption3.getText().isBlank()) {
            showMessage("Option 3 is empty");
            return;
        }
        QuestionInfo questionInfo = new QuestionInfo();
        questionInfo.setQuestion(txtQuestion.getText());
        questionInfo.setOption1(txtOption1.getText());
        questionInfo.setOption2(txtOption2.getText());
        questionInfo.setOption3(txtOption3.getText());
        questionInfo.setOption4(txtOption4.getText());
        questionInfo.setOption5("All above option");
        lstQuestions.add(questionInfo);
        showMessage("Question added successfully. \nTotal questions are: " + lstQuestions.size());
        txtQuestion.setText("");
        txtOption1.setText("");
        txtOption2.setText("");
        txtOption3.setText("");
        txtOption4.setText("");
    }

    // ClientHandler class
    private static class ClientHandler implements Runnable {
        private final Socket clientSocket;
        private ServerController controller;

        public ClientHandler(Socket socket, ServerController controller) {
            this.clientSocket = socket;
            this.controller = controller;
        }

        public void run() {
            PrintWriter out = null;
            BufferedReader in = null;
            try {
                out = new PrintWriter(clientSocket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                

                controller.updateStatus("New client connected" + " " +
                		clientSocket.getInetAddress().getHostAddress());

                String line;
                while ((line = in.readLine()) != null) {
                    controller.updateStatus("Sent from the client: " + line);
                	//System.out.println("Sent from the client: " + line);
                	


                    String[] fruits = line.split("\\+");

//                    System.out.printf("client request: %s\n", fruits[0]);

                    if(fruits[0].equals("ans"))
                    {
//                        System.out.printf("client request: %s\n", fruits[1]);
                        if (fruits[1].equalsIgnoreCase("yes"))
                        {
                        	//controller.showMessage("Survey Form Sended\n");
                            // Serialize data1 and send it to the client
                            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                            ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
                            objectOutputStream.writeObject(controller.getData());
                            objectOutputStream.flush();
                            byte[] serializedData = byteArrayOutputStream.toByteArray();

                            out.println("survey_data");
                            out.println(serializedData.length);
                            out.flush();
                            clientSocket.getOutputStream().write(serializedData);
                            out.flush();
                        }
                        else
                        {
                            out.println(line);
                        }
                    }
                    else if (fruits[0].equals("opt"))
                    {
//                        System.out.printf("client request: %s\n", fruits[1]);
                        int j = 0;
                        for (int i = 1; i < 10; i++)
                        {

                        	//controller.showMessage("Question"+j+": "+fruits[i]);
                            j = j+1;
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (out != null) {
                        out.close();
                    }
                    if (in != null) {
                        in.close();
                        clientSocket.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private String[][] getData() {
        String[][] data = new String[lstQuestions.size()][6];
        for (int i=0; i<lstQuestions.size(); i++) {
            QuestionInfo questionInfo = lstQuestions.get(i);
            data[i][0] = questionInfo.getQuestion();
            data[i][1] = questionInfo.getOption1();
            data[i][2] = questionInfo.getOption2();
            data[i][3] = questionInfo.getOption3();
            data[i][4] = questionInfo.getOption4();
            data[i][5] = questionInfo.getOption5();
        }
        return data;
    }
}
