package sample.server;

import java.io.*;
import java.net.Socket;
import java.net.SocketAddress;

public class ClientConnectionHandler implements Runnable{
    private final File serverFiles = new File("resources/server");
    private final File clientFiles = new File("resources/client");
    private Socket socket;
    private SocketAddress socketAddress;
    public final static int fileSize = 5000000; // File size with overhead

    public ClientConnectionHandler(Socket socket) {
        this.socket = socket;
        socketAddress = socket.getLocalSocketAddress();
    }

    public void transferFile(File path) {
        try {
            DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
            OutputStream outputStream = new FileOutputStream((path + "/" + dataInputStream.readUTF()));

            long size = dataInputStream.readLong();
            byte[] bytesArray = new byte[fileSize];

            int bytesRead;
            while (size > 0 && (bytesRead = dataInputStream.read(bytesArray, 0, (int) Math.min(bytesArray.length, size))) > 1) {
                outputStream.write(bytesArray, 0, bytesRead);
                size -= bytesRead;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run(){
        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());

            String line;
            while ((line = bufferedReader.readLine()) != null) {
                if (line.equals("Download")) {
                    transferFile(clientFiles);
                } else if (line.equals("Upload")) {
                    transferFile(serverFiles);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
