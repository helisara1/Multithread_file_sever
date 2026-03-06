package server;

import java.net.*;
import java.util.concurrent.*;

public class FileServer {

    private static final int PORT = 5000;
    private static final int THREADS = 5;

    public static void main(String[] args) throws Exception {

        ServerSocket serverSocket = new ServerSocket(PORT);

        ExecutorService pool =
                Executors.newFixedThreadPool(THREADS);

        System.out.println("File Server started on port " + PORT);

        while(true){

            Socket client = serverSocket.accept();
            System.out.println("Client connected: " + client.getInetAddress());

            pool.execute(new ClientHandler(client));
        }
    }
}