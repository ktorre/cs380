// Client

import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.BufferedReader;
import java.util.Scanner;
import java.net.Socket;

public class OldClient {

	public static void main( String[] args ) throws Exception {
		try ( Socket client = new Socket( "codebank.xyz", 38001 ) ) {
			BufferedReader reader = new BufferedReader( new InputStreamReader( client.getInputStream() ) );
			OutputStream out = client.getOutputStream();
			PrintStream outWrite = new PrintStream( out, true, "UTF-8" );
			Scanner input = new Scanner( System.in );

			System.out.print( "Enter desired nickname: " );
			String name = input.nextLine();
			outWrite.println( name );

			String message;
			System.out.print( "Client: " );
			while ( ( message = input.nextLine() ) != null ) {
				outWrite.println( message );
				if ( message.equals( "exit" ) ) {
					break;
				}

				do {
					System.out.println( "Server: " + reader.readLine() );
				} while ( reader.ready() );
				System.out.print( "Client: " );
			}



			reader.close();
			outWrite.close();
			client.close();
		} catch ( Exception e ) {
			System.out.println( "Server unavailable" );
		}
	}
}
