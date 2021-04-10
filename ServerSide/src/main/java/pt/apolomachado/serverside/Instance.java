package pt.apolomachado.serverside;

import pt.apolomachado.serverside.database.DatabaseProvider;

import java.io.DataInputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class Instance {

    protected List<Socket> connectedClients = new ArrayList<>();
    protected final DatabaseProvider databaseProvider;
    protected long startedDate;

    public List<Socket> getConnectedClients() {
        return connectedClients;
    }

    public Instance() {
        startedDate = System.currentTimeMillis();
        databaseProvider = new DatabaseProvider(this);
        startListening();
    }

    public DatabaseProvider getDatabaseProvider() {
        return databaseProvider;
    }

    protected void startListening() {
        try {
            ServerSocket socket = new ServerSocket(9999);
            System.out.println("[INFO] Listening in port 9999");
            while (!socket.isClosed()) {
                Socket client = socket.accept();
                while (!client.isClosed()) {
                    try {
                        getConnectedClients().add(client);
                        log("Connected.", client.getInetAddress().getHostAddress());
                        DataInputStream inputStream = new DataInputStream(client.getInputStream());
                        OutputStream outputStream = client.getOutputStream();
                        String username = inputStream.readUTF();
                        String password = inputStream.readUTF();
                        if (getDatabaseProvider().existsUsername(username) && password.equalsIgnoreCase(getDatabaseProvider().getPassword(username))) {
                            outputStream.write(1);
                            log("[" + username + "] Valid credentials.", client.getInetAddress().getHostAddress());
                            getDatabaseProvider().updateLastLogin(username, System.currentTimeMillis());
                            int code = inputStream.read();
                            if(code == 1) {
                                outputStream.write(1);
                                outputStream.flush();
                                log("[" + username + "] Shutdown server by user request.", client.getInetAddress().getHostAddress());
                                inputStream.close();
                                outputStream.close();
                                client.close();
                                socket.close();
                                return;
                            } else {
                                if (code == -1) {
                                    log("[" + username + "] Client is disconnecting...", client.getInetAddress().getHostAddress());
                                    return;
                                }
                                log("[" + username + "] Invalid request [Code: " + code + "].", client.getInetAddress().getHostAddress());
                            }
                        } else {
                            outputStream.write(0);
                            inputStream.close();
                            outputStream.close();
                            client.close();
                            getConnectedClients().remove(client);
                            log("[" + username + "] Invalid credentials.", client.getInetAddress().getHostAddress());
                            log("[" + username + "] Disconnected client.", client.getInetAddress().getHostAddress());
                        }
                    } catch (Exception e) {
                        log("An error occurred.", client.getInetAddress().getHostAddress());
                        client.close();
                        e.printStackTrace();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void log(String message, String ip) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        System.out.println("[" + simpleDateFormat.format(System.currentTimeMillis()) + "]" + " [" + ip + "] " + message);
    }
}