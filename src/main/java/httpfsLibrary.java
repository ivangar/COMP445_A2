import java.net.*;
import java.io.*;
import java.nio.file.*;
import java.util.Arrays;

public class httpfsLibrary {

    private Socket clientSocket;
    private boolean is_verbose = false;
    private Path root = Paths.get("").toAbsolutePath();  //default system current dir
    private PrintWriter writer;
    private BufferedReader reader;
    private String statusLine = " 200 OK";

    public httpfsLibrary(String[] args, Socket client) {
        clientSocket = client;
        setArgs(args);
        System.out.println("Server directory is " + root.toString());
    }

    private void setArgs(String[] args){
        is_verbose = Arrays.asList(args).contains("-v");

        //Add the root to dir
        if(Arrays.asList(args).contains("-d")){
            boolean root_arg = false;

            for (String arg : args) {
                if(arg.equalsIgnoreCase("-d")){
                    root_arg = true;
                    continue;
                }

                if(root_arg){
                    root = Paths.get(root.toString(), arg);
                    try {
                        Files.createDirectories(root);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    root_arg = false;
                }
            }
        }
    }

    public void parseClientRequest() throws IOException{

        try{
            System.out.println("Client and Server are connected from httpfsLibrary.");
            writer = new PrintWriter(clientSocket.getOutputStream(), true);
            reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            StringBuilder response = new StringBuilder();

            String responseLine;

            try{
                while((responseLine = reader.readLine())!=null) {
                    System.out.println(responseLine);


                    if(responseLine.startsWith("GET")){
                        //Do code for Get
                        processGetRequest(responseLine.split(" ")[1], response);
                    }

                    else if(responseLine.startsWith("POST")){
                        //Process post here
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

            int contentLength = response.length();
            writer.println("HTTP/1.1" + statusLine + " \r\nServer:localhost\r\nContent-Type:text/html\r\nContent-length:"+ contentLength +"\r\nConnection: Closed\r\n\r\n" + response.toString());
            writer.flush();
            reader.close();
            writer.close();
            clientSocket.close();

        }catch (IOException e){
            System.out.println("Connection Problem with connection or port ");
            System.out.println(e.getMessage());
        }

    }

    //In this function we check if the path provided exists, then if it is a folder or file
    private void processGetRequest(String requestPathLine, StringBuilder response){

        Path searchPath = Paths.get(root.toString(), requestPathLine);

        if (Files.exists(searchPath)){
            System.out.println("the path exists");

            //If it is a directory, print all the list of files
            if(Files.isDirectory(searchPath)){
                try (DirectoryStream<Path> stream = Files.newDirectoryStream(searchPath)) {
                    for (Path file: stream) {
                        if(file.toFile().isFile()){
                            response.append(file.getFileName() + "\r\n");
                        }

                    }
                } catch (IOException | DirectoryIteratorException x) {
                    statusLine = " 500 Internal Server Error";
                    System.err.println(x);
                }
            }

            //If it is a file, get all contents and send to client
            else if(Files.isRegularFile(searchPath)){

                try {
                    String data = new String(Files.readAllBytes(searchPath));
                    response.append(data);
                } catch (IOException e) {
                    statusLine = " 500 Internal Server Error";
                    e.printStackTrace();
                }
            }

        }

        else{
            statusLine = " 404 Not Found";
            response.append("File or folder not found");
        }

    }

    private void processPostRequest(){
        System.out.println("process POST method here");
    }
}
