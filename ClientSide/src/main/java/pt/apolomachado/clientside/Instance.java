package pt.apolomachado.clientside;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Scanner;

public class Instance {

    long sessionStarted;

    public Instance() {
        startClient();
    }

    protected void startClient() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Username: ");
        String username = scanner.nextLine();
        System.out.println("Password: ");
        String password = crypt(scanner.nextLine());
        try {
            Socket client = new Socket("0.0.0.0", 9999);
            System.out.println("Connected successfully.");
            while (!client.isClosed()) {
                DataInputStream inputStream = new DataInputStream(client.getInputStream());
                DataOutputStream outputStream = new DataOutputStream(client.getOutputStream());
                outputStream.writeUTF(username);
                outputStream.flush();
                outputStream.writeUTF(password);
                outputStream.flush();
                System.out.println("Handshaking...");
                int returnedValue = inputStream.read();
                if(returnedValue == 1) {
                    System.out.println("Authenticated.");
                } else {
                    System.out.println("Login failed.");
                }
                sessionStarted = System.currentTimeMillis();
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
                System.out.println("You have logged in at: " + simpleDateFormat.format(sessionStarted));
                System.out.println("Terminal opened, you can type some cool commands like 'help', 'quit', 'stop' and others.");
                boolean shutdown = false;
                while (!shutdown) {
                    switch (scanner.nextLine()) {
                        case "quit": {
                            client.close();
                            System.out.println("Connection closed.");
                            shutdown = true;
                            break;
                        }
                        case "help": {
                            System.out.println("1. Type 'quit' to close your connection.");
                            System.out.println("2. Type 'stop' to close server's socket.");
                            break;
                        }
                        case "stop": {
                            outputStream.write(1);
                            outputStream.flush();
                            System.out.println("[SHUTDOWN] Sending shutdown request to server...");
                            System.out.println("Server is closing...");
                            System.out.println("Closing client...");
                            outputStream.close();
                            client.close();
                            shutdown = true;
                            break;
                        }
                        default: {
                            System.out.println("Type 'help' to get some help.");
                            break;
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String crypt(String passwordToHash){
        String generatedPassword = null;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-512");
            byte[] bytes = md.digest(passwordToHash.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte aByte : bytes) {
                sb.append(Integer.toString((aByte & 0xff) + 0x100, 16).substring(1));
            }
            generatedPassword = sb.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return generatedPassword;
    }
}