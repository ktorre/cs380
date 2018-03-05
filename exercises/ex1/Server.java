// Server

import java.net.ServerSocket;
import java.net.Socket;

import java.io.OutputStream;
import java.io.PrintStream;
import java.io.InputStreamReader;
import java.util.Scanner;

public class Server {
	public static void main( String[] args ) throws Exception {
		System.out.print( "Initializing server..." );
		try ( ServerSocket server = new ServerSocket( 7200 ) ) {
			System.out.println( "Success" );
			System.out.println( "Waiting on clients..." );
			while ( true ) {
				try ( Socket client = server.accept() ) {
					String address = client.getInetAddress().getHostAddress();	
					System.out.printf( "Client connected: %s%n", address );
					OutputStream os = client.getOutputStream();
					PrintStream out = new PrintStream( os, true, "UTF-8" );

					Scanner in = new Scanner( new InputStreamReader( client.getInputStream() ) );

					out.printf( "Hi %s, thanks for connecting!%n", address );

					String message = "";
					while ( !message.equals( "exit" ) ) {
						message = in.nextLine();
						if ( message != null ) {
							System.out.println( "Client ( " + address + " ): " + message );
							out.println( message );
						}
					}

					client.close();
					System.out.printf( "Client disconnected: %s%n", address );

				}
			}
		}
	}
}

