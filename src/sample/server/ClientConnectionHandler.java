package sample.server;

import java.io.*;
import java.net.Socket;
import java.net.SocketAddress;

public class ClientConnectionHandler implements Runnable{
    private File serverFiles = new File("resources/server");
    private File clientFiles = new File("resources/client");
    private Socket socket;
    private SocketAddress socketAddress;

    public final static int fileSize = 7000000; // File size (larger on purpose)

    public ClientConnectionHandler(Socket socket) {
        this.socket = socket;
        socketAddress = socket.getLocalSocketAddress();
    }

    public void transferFile(File path) {
        try {
            DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
            String fileName = dataInputStream.readUTF();
            OutputStream outputStream = new FileOutputStream((path + "/" + fileName));

            int bytes;
            long size = dataInputStream.readLong();
            byte[] bytesArray = new byte[fileSize];

            while (size > 0 && (bytes = dataInputStream.read(bytesArray, 0, (int) Math.min(bytesArray.length, size))) > 1) {
                outputStream.write(bytesArray, 0, bytes);
                size -= bytes;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run(){
        try {
            PrintWriter printWriter = new PrintWriter(socket.getOutputStream(), true);
            printWriter.println(this.serverFiles.getName());
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
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
