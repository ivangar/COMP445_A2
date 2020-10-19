import java.net.Socket;
import java.net.ServerSocket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.io.*;

public class httpfs {

    private static int serverPort;

    public static void main(String[] args){

        setServerPort(args);
        new httpfs().runServer(args);

    }

    public void runServer(String[] args){

        try{

            ServerSocket server = new ServerSocket(serverPort);
            System.out.println("Server is connected at port " + serverPort + " waiting for the Client to connect.");
            Socket client = server.accept();

            httpfsLibrary httpfsLib = new httpfsLibrary(args, client);
            httpfsLib.parseClientRequest();

        }catch (IOException e){
            System.out.println("Connection Problem with connection or port " + serverPort);
            System.out.println(e.getMessage());
        }
    }

    private static void setServerPort(String[] args){
        int findP = Arrays.asList(args).indexOf("-p");
        if(findP == -1){
            serverPort = 8080;
        }
        else
            serverPort = Integer.parseInt(args[findP+1]);
    }
}
