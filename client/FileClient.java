package client;

import java.io.*;
import java.net.*;
import java.util.Scanner;

public class FileClient {

    private static final String HOST = "localhost";
    private static final int PORT = 5000;

    public static void main(String[] args) throws Exception {

        Scanner sc = new Scanner(System.in);

        Socket socket = new Socket(HOST, PORT);

        DataOutputStream out =
                new DataOutputStream(socket.getOutputStream());

        DataInputStream in =
                new DataInputStream(socket.getInputStream());

        // ---------- LOGIN ----------
        System.out.print("Username: ");
        String u = sc.nextLine();

        System.out.print("Password: ");
        String p = sc.nextLine();

        out.writeUTF(u);
        out.writeUTF(p);

        String response = in.readUTF();

        if(!response.equals("OK")){
            System.out.println("Authentication Failed");
            socket.close();
            return;
        }

        System.out.println("Login successful.");

        // ---------- COMMAND LOOP ----------
        while(true){

            System.out.println("\n1 LIST");
            System.out.println("2 UPLOAD");
            System.out.println("3 DOWNLOAD");
            System.out.println("4 DELETE");
            System.out.println("5 QUIT");

            int choice = sc.nextInt();
            sc.nextLine();

            if(choice == 5){
                out.writeUTF("QUIT");
                break;
            }

            // ---------- LIST ----------
            if(choice == 1){

                out.writeUTF("LIST");

                int n = in.readInt();

                for(int i = 0; i < n; i++){

                    String name = in.readUTF();
                    long size = in.readLong();

                    System.out.println(name + " (" + size + " bytes)");
                }
            }

            // ---------- UPLOAD ----------
            else if(choice == 2){

                out.writeUTF("UPLOAD");

                System.out.print("File path: ");
                String path = sc.nextLine();

                File file = new File(path);

                out.writeUTF(file.getName());
                out.writeLong(file.length());

                FileInputStream fis =
                        new FileInputStream(file);

                byte[] buffer = new byte[4096];
                int read;

                while((read = fis.read(buffer)) != -1)
                    out.write(buffer,0,read);

                fis.close();

                System.out.println(in.readUTF());
            }

            // ---------- DOWNLOAD ----------
            else if(choice == 3){

                out.writeUTF("DOWNLOAD");

                System.out.print("Filename: ");
                String name = sc.nextLine().trim();

                out.writeUTF(name);

                long size = in.readLong();

                if(size == -1){
                    System.out.println("File Not Found");
                    continue;
                }

                FileOutputStream fos =
                        new FileOutputStream(name);

                byte[] buffer = new byte[4096];
                int read;

                while(size > 0 &&
                        (read = in.read(buffer,0,
                                (int)Math.min(buffer.length,size))) != -1){

                    fos.write(buffer,0,read);
                    size -= read;
                }

                fos.close();

                System.out.println("Downloaded");
            }

            // ---------- DELETE ----------
            else if(choice == 4){

                out.writeUTF("DELETE");

                System.out.print("Filename: ");
                String name = sc.nextLine();

                out.writeUTF(name);

                System.out.println(in.readUTF());
            }
        }

        socket.close();
        System.out.println("Disconnected from server.");
    }
}