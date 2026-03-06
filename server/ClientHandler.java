package server;

import java.io.*;
import java.net.*;

public class ClientHandler implements Runnable {

    private static final String USER = "helinia";
    private static final String PASS = "1234";

    private Socket socket;
    private static final String DIR = "storage/";

    public ClientHandler(Socket socket){
        this.socket = socket;
    }

    @Override
    public void run(){

        try(
            DataInputStream in =
                    new DataInputStream(socket.getInputStream());

            DataOutputStream out =
                    new DataOutputStream(socket.getOutputStream());
        ){

            // ---------- AUTHENTICATION ----------
            String user = in.readUTF();
            String pass = in.readUTF();

            if(!USER.equals(user) || !PASS.equals(pass)){
                out.writeUTF("FAIL");
                return;
            }

            out.writeUTF("OK");

            // ---------- COMMAND LOOP ----------
            while(true){

                String command = in.readUTF();

                if(command.equals("QUIT")){
                    System.out.println("Client disconnected");
                    break;
                }

                try{

                    switch(command){

                        case "LIST":
                            listFiles(out);
                            break;

                        case "UPLOAD":
                            receiveFile(in);
                            out.writeUTF("UPLOAD OK");
                            break;

                        case "DOWNLOAD":
                            sendFile(in, out);
                            break;

                        case "DELETE":
                            deleteFile(in, out);
                            break;

                        default:
                            out.writeUTF("INVALID COMMAND");
                    }

                }catch(Exception e){

                    System.out.println("Client error: " + e.getMessage());
                    out.writeUTF("SERVER ERROR");
                }
            }

        }catch(Exception e){
            e.printStackTrace();
        }
    }

    // ---------- LIST ----------
    private void listFiles(DataOutputStream out) throws Exception {

        File folder = new File(DIR);

        if(!folder.exists())
            folder.mkdirs();

        File[] files = folder.listFiles();

        if(files == null){
            out.writeInt(0);
            return;
        }

        out.writeInt(files.length);

        for(File f : files){
            out.writeUTF(f.getName());
            out.writeLong(f.length());
        }
    }

    // ---------- UPLOAD ----------
    private synchronized void receiveFile(DataInputStream in)
            throws Exception {

        String name = in.readUTF();
        long size = in.readLong();

        FileOutputStream fos =
                new FileOutputStream(DIR + name);

        byte[] buffer = new byte[4096];
        int read;

        while(size > 0 &&
                (read = in.read(buffer,0,
                        (int)Math.min(buffer.length,size))) != -1){

            fos.write(buffer,0,read);
            size -= read;
        }

        fos.close();

        System.out.println("Uploaded: " + name);
    }

    // ---------- DOWNLOAD ----------
    private void sendFile(DataInputStream in,
                          DataOutputStream out)
            throws Exception {

        String name = in.readUTF().trim();

        File file = new File(DIR + name);

        if(!file.exists()){
            out.writeLong(-1);
            return;
        }

        out.writeLong(file.length());

        FileInputStream fis = new FileInputStream(file);

        byte[] buffer = new byte[4096];
        int read;

        while((read = fis.read(buffer)) != -1){
            out.write(buffer,0,read);
        }

        fis.close();
    }

    // ---------- DELETE ----------
    private void deleteFile(DataInputStream in,
                            DataOutputStream out)
            throws Exception {

        String name = in.readUTF();

        File file = new File(DIR + name);

        if(file.exists() && file.delete())
            out.writeUTF("DELETED");
        else
            out.writeUTF("FAILED");
    }
}