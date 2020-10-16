import java.net.Socket;
import java.net.ServerSocket;
import java.util.*;
import java.io.*;

public class httpfs {

    public static void main(String[] args){
        int port = getPort(args);

        try(ServerSocket server = new ServerSocket(port)){
            System.out.println("Server is waiting for the Client to connect.");
            while(true){
                Socket socket = server.accept();
                System.out.println("Client and Server are connected.");

                PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                String responseLine;
                boolean response_content = false;

                try{
                    while((responseLine = reader.readLine())!=null) {
                        System.out.println(responseLine);
                    }

                }catch(Exception e){
                    e.printStackTrace();
                }

                reader.close();

                writer.print("HTTP/1.1 200 OK\r\nContent-type:text/html\r\nContent-length:12\r\n\r\nHello There!");
                writer.flush();
                writer.close();
                socket.close();
            }
        }catch (IOException e){
            System.out.println("Connection Problem.");
        }
    }

    private static int getPort(String[] args){
        int findP = Arrays.asList(args).indexOf("-p");
        if(findP == -1){
            return 8080;
        }
        return Integer.parseInt(args[findP+1]);
    }
}
