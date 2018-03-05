
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;

// Run the server first, it will listen for clients to connect and then
// communicate with them
public final class TextServer {

    public static void main(String[] args) throws Exception {

        // Sets up server socket for listening on port 22222
        try (ServerSocket serverSocket = new ServerSocket(22222)) {

            // Every loop iteration deals with one client connection
            while (true) {

                // Server blocks in accept() method call until a client connects, 
                // then Socket object for that client is returned
                try (Socket socket = serverSocket.accept()) {
                    String address = socket.getInetAddress().getHostAddress();
                    System.out.printf("Client connected: %s%n", address);

                    // Get OutputStream (byte stream) through network to connected client
                    OutputStream os = socket.getOutputStream();

                    // Make PrintStream (character stream) on top of client-connected OutputStream
                    // This allows us to write text in the given character encoding through the
                    // Socket's OutputStream
                    PrintStream out = new PrintStream(os, true, "UTF-8");

                    // Write text to the socket's output stream via the PrintStream
                    // The PrintStream deals with translating the text to bytes that are sent
                    // across the network
                    out.printf("Hi %s, thanks for connecting!%n", address);
                } // socket closes when leaving try-with-resources block
            }
        } // serverSocket closes when leaving try-with-resources block
    }
}
