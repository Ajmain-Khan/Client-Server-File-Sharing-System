package sample.client;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import java.io.*;
import java.net.Socket;

public class ClientController {
    @FXML AnchorPane anchorPane;
    @FXML ListView<String> clientView, serverView;

    private static String server = "localhost"; // localhost addr: 127.0.0.1
    public final static int SERVER_PORT = 6000;

    private static Socket socket;

    private File serverFiles = new File("resources/server");
    private File clientFiles = new File("resources/client");

    @FXML
    private void initialize() { // Initialize list of files on startup
        serverView.setItems(FXCollections.observableArrayList(serverFiles.list()));
        clientView.setItems(FXCollections.observableArrayList(clientFiles.list()));

        try {
            socket = new Socket(server, SERVER_PORT);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Restart AnchorPane
    void restartClient(){
        Stage prevStage = (Stage) anchorPane.getScene().getWindow();
        prevStage.hide();
        System.out.println("Loading CLIENT Files...\nLoading SERVER Files...");

        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("clientUI.fxml"));
            Parent root2 = fxmlLoader.load();
            Stage stage = new Stage();
            stage.setScene(new Scene(root2));
            stage.show();
        } catch(Exception e) {
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
    void downloadButton(ActionEvent actionEvent) throws IOException {
        PrintStream printStream = new PrintStream(socket.getOutputStream());

        printStream.println("DOWNLOAD");
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
    void uploadButton(ActionEvent actionEvent) throws IOException{
        PrintStream printStream = new PrintStream(socket.getOutputStream());
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());

        printStream.println("UPLOAD");
        transfer(clientFiles + "/" + clientView.getSelectionModel().getSelectedItem());

        serverView.setItems(null);
        serverView.setItems(FXCollections.observableArrayList(serverFiles.list()));
        restartClient();
    }

    /**
     * Method handles transferring files to/from server
     * @param fileName
     */
    public static void transfer(String fileName) {
        try {
            File file = new File(fileName);
            byte[] bytesArray = new byte[(int) file.length()];

            DataInputStream dataInputStream = new DataInputStream(new BufferedInputStream(new FileInputStream(file)));
            dataInputStream.readFully(bytesArray, 0, bytesArray.length);

            DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
            dataOutputStream.writeUTF(file.getName());
            dataOutputStream.writeLong(bytesArray.length);
            dataOutputStream.write(bytesArray, 0, bytesArray.length);
            dataOutputStream.flush();
            System.out.println(file.getName() + " has been successfully transferred");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
