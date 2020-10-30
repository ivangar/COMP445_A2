import java.net.*;
import java.io.*;
import java.nio.file.*;
import java.util.Arrays;

public class httpfsLibrary {

    private Socket clientSocket;
    private boolean is_verbose = false;
    private Path root = Paths.get("").toAbsolutePath();  //default system current dir
    private String statusLine = " 200 OK";

    // root = ...COMP445_A2
    public httpfsLibrary(String[] args, Socket client) {
        clientSocket = client;
        setArgs(args);
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

        // Default root folder
        else{
            root = Paths.get(root.toString(), "root");
        }
    }

    public void parseClientRequest() throws IOException{
        try(  PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true);
              BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));)
        {

            StringBuilder request = new StringBuilder();
            StringBuilder entity_body = new StringBuilder();
            StringBuilder response = new StringBuilder();

            System.out.println();

            settings(writer, reader, request, entity_body);

            if (request.toString().startsWith("GET")) {
                processGetRequest(request.toString().split(" ")[1], response);
            } else if (request.toString().startsWith("POST")) {
                processPostRequest(request.toString().split(" ")[1], entity_body);
            }

            int contentLength = response.length();
            writer.println("HTTP/1.1" + statusLine + " \r\nServer:localhost\r\nContent-Type:text/html\r\nContent-length:" + contentLength + "\r\nConnection: Closed\r\n\r\n" + response.toString());
            writer.flush();

            if(!is_verbose)
                System.out.println("Request from client finalized");
            System.out.println("---------------------------------------------------------------\n\n");

        }catch (IOException e){
            System.out.println("Connection Problem with connection or port ");
            System.out.println(e.getMessage());
        }

    }

    private void settings(PrintWriter writer, BufferedReader reader, StringBuilder request, StringBuilder entity_body) throws IOException{
        try{
            boolean has_body = false;
            boolean crlf = false;
            StringBuilder line = new StringBuilder();

            // I made this for getting the value of content-header
            int content_length=0;

            int character;
            while((character = reader.read()) != -1){
                line = line.append((char) character);

                // line always has a line.
                if(character=='\n'){
                    if(is_verbose)
                        System.out.print(line);

                    // Find the content-length header and find the length of the entity body.
                    if(line.toString().matches("Content-Length:(.*)\r\n")){
                        String string_line = line.toString();
                        string_line = string_line.substring(15).replaceAll("\\s+","");
                        content_length = Integer.parseInt(string_line);
                        has_body = true;
                    }

                    if(crlf){
                        entity_body.append(line);
                    }

                    // crlf between header and the entity body. Also, it has the content-length header.
                    // get request will not execute this. cuz it doesn't have content-length header
                    if(line.toString().matches("\r\n") && has_body){
                        crlf = true;
                    }

                    request = request.append(line);

                    if(request.toString().startsWith("GET") && line.toString().matches("\r\n")){
                        break;
                    }else if(request.toString().startsWith("POST")){
                        // might have an error here later
                        if(entity_body.toString().length() == content_length+4 && crlf){
                            break;
                        }
                    }
                    line.setLength(0);
                }
            }
        }catch (IOException e){
            System.out.println("Connection Problem with connection or port ");
            System.out.println(e.getMessage());
        }

    }

    //In this function we check if the path provided exists, then if it is a folder or file
    private void processGetRequest(String requestPathLine, StringBuilder response){
        Path normalizePath = Paths.get(requestPathLine).normalize();
        String relativePath = normalizePath.toString();

        Path searchPath = Paths.get(root.toString(), relativePath);

        if (Files.exists(searchPath)){

            //If it is a directory, print all the list of files
            if(Files.isDirectory(searchPath)){
                try (DirectoryStream<Path> stream = Files.newDirectoryStream(searchPath)) {
                    for (Path file: stream) {
                        response.append(file.getFileName() + "\r\n");
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

    private void processPostRequest(String requestPathLine, StringBuilder entity_body){
        Path normalizePath = Paths.get(requestPathLine).normalize();
        String relativePath = normalizePath.toString();

        Path searchPath = Paths.get(root.toString(), relativePath);

        // Either the file or directory exists
        if (Files.exists(searchPath)){

            // If it is a file, overwrite.
            if(Files.isRegularFile(searchPath)){
                try {
                    if(Files.isWritable(searchPath)){
                        byte[] content = entity_body.toString().getBytes();
                        Files.write(searchPath, content);
                        System.out.println("File is overwrited.");
                    }else
                        System.out.println("File can be read only.");

                } catch (IOException exception) {
                    exception.printStackTrace();
                }
            }

            // If it is a directory, do nothing.
            else{
                System.out.println("It's a directory. So, it will not do anything.");
            }
        }

        // If the file doesn't exist, create a text file.
        else{
            try {
                Files.createDirectories(searchPath.getParent());
                Files.createFile(searchPath);
                byte[] content = entity_body.toString().getBytes();
                Files.write(searchPath, content);
                System.out.println("The file doesn't exist, so it creates a file.");
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }
    }
}
