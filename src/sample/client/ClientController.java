package sample.client;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import java.io.*;
import java.net.Socket;

public class ClientController {
    @FXML AnchorPane anchorPane;
    @FXML ListView<String> clientView, serverView;

    private static String server = "localhost"; // localhost address is: 127.0.0.1
    public final static int SERVER_PORT = 6000;
    private static Socket socket;
    private final File serverFiles = new File("resources/server");
    private final File clientFiles = new File("resources/client");

    @FXML
    private void initialize() {
        // Initialize list of files on startup
        serverView.setItems(FXCollections.observableArrayList(serverFiles.list()));
        clientView.setItems(FXCollections.observableArrayList(clientFiles.list()));
        try {
            socket = new Socket(server, SERVER_PORT);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Called when download button is pressed.
     * Parses a file by calling transfer() function
     * and displays it on ListView for client
     *
     * @param actionEvent
     * @throws IOException
     */
    @FXML
    private void downloadButton(ActionEvent actionEvent) throws IOException {
        PrintStream printStream = new PrintStream(socket.getOutputStream());
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());

        printStream.println("Download");
        transfer(serverFiles + "/" + serverView.getSelectionModel().getSelectedItem());

        clientView.setItems(null);  // Clear ListView
        clientView.setItems(FXCollections.observableArrayList(clientFiles.list())); // Add new items to ListView
        restartClient();
    }

    /**
     * Called when upload button is pressed.
     * Parses a file by calling transfer() function
     * and displays it on ListView for client
     * @param actionEvent
     * @throws IOException
     */
    @FXML
    private void uploadButton(ActionEvent actionEvent) throws IOException{
        PrintStream printStream = new PrintStream(socket.getOutputStream());
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());

        printStream.println("Upload");
        transfer(clientFiles + "/" + clientView.getSelectionModel().getSelectedItem());

        serverView.setItems(null);
        serverView.setItems(FXCollections.observableArrayList(serverFiles.list()));
        restartClient();
    }

    @FXML
    private void exit() {
        System.exit(0);
    }

    /**
     * Method handles transferring files to/from server
     * @param fileName
     */
    public static void transfer(String fileName) {
        try {
            File file = new File(fileName);
            byte[] bytesArray = new byte[(int) file.length()];  // Create an byte array equivalent to byte length of file

            DataInputStream dataInputStream = new DataInputStream(new BufferedInputStream(new FileInputStream(file)));
            dataInputStream.readFully(bytesArray, 0, bytesArray.length);

            OutputStream outputStream = socket.getOutputStream();

            DataOutputStream dataOutputStream = new DataOutputStream(outputStream);
            dataOutputStream.writeUTF(file.getName());
            dataOutputStream.writeLong(bytesArray.length);
            dataOutputStream.write(bytesArray, 0, bytesArray.length);
            dataOutputStream.flush();

            // Find caller method in stack
            StackTraceElement[] stacktrace = Thread.currentThread().getStackTrace();
            StackTraceElement element = stacktrace[2];
            String callerMethod = element.getMethodName();

            // Inline if statement to print a message based on whether downloadButton or uploadButton was the previous method in stack
            System.out.println(callerMethod.equals("downloadButton") ? file.getName() + " downloaded" : file.getName() + " uploaded to server");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Creates a new scene and replace the previous. This is done to update
     * the listView and visibly allow changes to take effect.
     */
    void restartClient() {
        Stage oldStage = (Stage) anchorPane.getScene().getWindow();
        oldStage.hide();
        System.out.println("Loading Files...\n");
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("clientUI.fxml"));
            Stage newStage = new Stage();
            newStage.setScene(new Scene(fxmlLoader.load()));
            newStage.show();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

}
