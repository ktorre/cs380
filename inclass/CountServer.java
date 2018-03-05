
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

// Run the server first, it will listen for clients to connect and then
// communicate with them
public final class CountServer {

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

                    // Writes 10 bytes to the client
                    for (int i = 1; i <= 10; i++) {
                        os.write(i);
                    }
                } // socket closes when leaving try-with-resources block
            }
        } // serverSocket closes when leaving try-with-resources block
    }
}