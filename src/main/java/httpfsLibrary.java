import java.net.*;
import java.io.*;
import java.nio.file.*;
import java.util.Arrays;

public class httpfsLibrary {

    private Socket clientSocket;
    private boolean is_verbose = false;
    private String path = Paths.get("").toAbsolutePath().toString();  //default current dir
    private PrintWriter writer;
    private BufferedReader reader;

    public httpfsLibrary(String[] args, Socket client) {
        clientSocket = client;
        setArgs(args);
        System.out.println("Server directory is " + path);
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
                    path = path.concat(arg);
                    try {
                        Files.createDirectories(Paths.get(path));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

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
            writer.println("HTTP/1.1 200 OK\r\nServer:localhost\r\nContent-Type:text/html\r\nContent-length:"+ contentLength +"\r\nConnection: Closed\r\n\r\n" + response.toString());
            writer.flush();

            reader.close();
            writer.close();
            clientSocket.close();

        }catch (IOException e){
            System.out.println("Connection Problem with connection or port ");
            System.out.println(e.getMessage());
        }

    }

    //In this function we check if the path provided is not empty, then if it is a folder or file
    private void processGetRequest(String requestPathLine, StringBuilder response){
        System.out.println("process GET method here");

        // list all the files
        try {

            Path test = Paths.get(requestPathLine).toRealPath();
            if (Files.exists(test))
                System.out.println("File exists");

            File folder = new File(requestPathLine);

            // list all the files
            File[] files = folder.listFiles();
            for(File file : files) {
                if(file.isFile()) {
                    System.out.println(file.getName());
                    response.append(file.getName() + "\r\n");
                }
            }
        } catch (Exception e) {
            e.getStackTrace();
        }


        /*
        //Another way to loop through directory contents
        Path dir = Paths.get(path);
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
            for (Path file: stream) {
                //System.out.println(file.getFileName());
                if(file.toFile().isFile()){
                    System.out.println(file.getFileName());
                    response.append(file.getFileName() + "\r\n");
                }

            }
        } catch (IOException | DirectoryIteratorException x) {
            System.err.println(x);
        }*/
    }

    private void processPostRequest(){
        System.out.println("process POST method here");
    }
}
