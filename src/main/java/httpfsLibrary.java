import java.net.*;
import java.io.*;
import java.util.Arrays;

public class httpfsLibrary {

    private Socket clientSocket;
    private boolean is_verbose = false;
    private String path = "./";
    private PrintWriter writer;
    private BufferedReader reader;

    public httpfsLibrary(String[] args, Socket client) {
        clientSocket = client;
        setArgs(args);
        System.out.println("Server path to dir " + path);
    }

    private void setArgs(String[] args){
        is_verbose = Arrays.asList(args).contains("-v");

        //Add the path to dir
        if(Arrays.asList(args).contains("-d")){
            boolean path_arg = false;

            for (String arg : args) {
                if(arg.equalsIgnoreCase("-d")){
                    path_arg = true;
                    continue;
                }

                if(path_arg){
                    path = arg;
                    path_arg = false;
                }
            }
        }
    }

    public void parseClientRequest() throws IOException{

        try{
            System.out.println("Client and Server are connected from httpfsLibrary.");
            writer = new PrintWriter(clientSocket.getOutputStream(), true);
            reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            String responseLine;

            try{
                while((responseLine = reader.readLine())!=null) {
                    System.out.println(responseLine);

                    if(responseLine.startsWith("GET")){
                        //Do code for Get
                        processGetRequest(responseLine.split(" ")[1]);
                    }

                    else if(responseLine.startsWith("POST")){
                        //Process post here
                        System.out.println("process POST method here");
                        processPostRequest();
                    }

                    //For now I break on empty line, but will change it after to break on something else like end of request body...
                        if(responseLine.isEmpty()) {
                            break;
                        }
                }

            }catch(Exception e){
                e.printStackTrace();
            }

            writer.println("HTTP/1.1 200 OK\r\nContent-Type:text/html\r\nContent-length:12\r\n\r\nHello There!");

        }catch (IOException e){
            System.out.println("Connection Problem with connection or port ");
            System.out.println(e.getMessage());
        }

    }

    //In this function we check if the path provided is not empty, then if it is a folder or file
    private void processGetRequest(String requestPathLine){
        System.out.println("process GET method here");
        System.out.println("Request path line " + requestPathLine);
    }

    private void processPostRequest(){

    }
}
