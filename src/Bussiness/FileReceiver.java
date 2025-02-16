package Bussiness;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import Application.MainApp;

public class FileReceiver {
    public void receive() {
        try (ServerSocket serverSocket = new ServerSocket(MainApp.BROADCAST_PORT);
                Socket socket = serverSocket.accept();
                DataInputStream dis = new DataInputStream(socket.getInputStream())) {

            String fileName = dis.readUTF();
            long fileSize = dis.readLong();

            File receivedFile = new File(fileName);
            try (FileOutputStream fos = new FileOutputStream(receivedFile)) {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while (fileSize > 0 && (bytesRead = dis.read(buffer)) != -1) {
                    fos.write(buffer, 0, bytesRead);
                    fileSize -= bytesRead;
                }
            }

            System.out.println("File received: " + receivedFile.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
