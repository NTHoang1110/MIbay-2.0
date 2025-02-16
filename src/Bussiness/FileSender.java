package Bussiness;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.Socket;

import Application.MainApp;

public class FileSender {
    private String fileName;
    private String winnerAddress;

    public FileSender(String fileName, String winnerAddress) {
        this.fileName = fileName;
        this.winnerAddress = winnerAddress;
    }

    public void send() {
        try (Socket socket = new Socket(winnerAddress, MainApp.BROADCAST_PORT);
            FileInputStream fis = new FileInputStream("dateien/" + fileName);
            DataOutputStream dos = new DataOutputStream(socket.getOutputStream())) {

            File file = new File("dateien/" + fileName);
            dos.writeUTF(fileName);
            dos.writeLong(file.length());

            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                dos.write(buffer, 0, bytesRead);
            }

            System.out.println("File sent successfully!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
