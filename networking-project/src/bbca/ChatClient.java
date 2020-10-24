package bbca;
 
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatClient {
    private static Socket socket;
    private static BufferedReader socketIn;
    private static PrintWriter out;
    
    public static void main(String[] args) throws Exception {
        Scanner userInput = new Scanner(System.in);
        
        System.out.println("What's the server IP? ");
        String serverip = userInput.nextLine();
        System.out.println("What's the server port? ");
        int port = userInput.nextInt();
        userInput.nextLine();

        socket = new Socket(serverip, port);
        socketIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);

        // start a thread to listen for server messages
        ClientServerHandler listener = new ClientServerHandler(socketIn);
        Thread t = new Thread(listener);
        t.start();

        System.out.print("Entering chat...");

        String line = userInput.nextLine().trim();
        while(!line.toLowerCase().startsWith("/quit")) {
            if (listener.state == 1) {
                String msg = String.format("NAME %s", line); 
                out.println(msg);
                line = userInput.nextLine().trim();
            } else if (listener.state == 2) {
                if (line.startsWith("@")) {
                    Pattern p = Pattern.compile("@([^\\W]+) (.*)");
                    Matcher m = p.matcher(line);

                    Boolean match = m.matches();
                    String recipient = m.group(1);
                    String chat = m.group(2);
                    
                    String msg = String.format("PCHAT %s %s", recipient, chat); 
                    out.println(msg);
                    line = userInput.nextLine().trim();
                } else {
                    String msg = String.format("CHAT %s", line); 
                    out.println(msg);
                    line = userInput.nextLine().trim();
                }
            }
        }
        out.println("QUIT");
        out.close();
        userInput.close();
        socketIn.close();
        socket.close();
        
    }
}
