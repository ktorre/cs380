// Client

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Scanner;
import java.net.Socket;

public class Client {
	public static void main( String[] args ) throws Exception {
		try ( Socket client = new Socket( "localhost", 7200 ) ) {
			InputStream in = client.getInputStream();
			InputStreamReader inRead = new InputStreamReader( in, "UTF-8" );
			OutputStream out = client.getOutputStream();
			PrintStream outWrite = new PrintStream( out, true, "UTF-8" );
			Scanner reader = new Scanner( inRead );

			// Get server message
			if ( reader.hasNext() ) {
				System.out.println( reader.nextLine() );
			}

			Scanner input = new Scanner( System.in );
			String message;
			
			System.out.print( "Client: " );
			while ( ( message = input.nextLine() ) != null ) {
				outWrite.println( message );
				if ( message.equals( "exit" ) ) {
					break;
				}
				System.out.println( "Server: " + reader.nextLine() );
				System.out.print( "Client: " );
			}



			inRead.close();
			outWrite.close();
			client.close();
		} catch ( Exception e ) {
			System.out.println( "Server unavailable" );
		}
	}
}
