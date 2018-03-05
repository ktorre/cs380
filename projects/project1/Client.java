// Client

import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.OutputStreamWriter;
import java.io.BufferedWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.io.IOException;

public class Client {
    public static void main( String args[] ) throws Exception {
	try ( Socket client = new Socket( "codebank.xyz", 38001 ) ) {
	    // Client -> Server reader/writer

	    // Server message receiver
	    Runnable receiver = () -> {

		String message = null;
		BufferedReader reader = null;
		try {
			reader = new BufferedReader( new InputStreamReader( client.getInputStream() ) );
		} catch ( Exception e ) { System.out.println( "Error: " + e ); }

		try {
		    while ( ( message = reader.readLine() ) != null ) {
			System.out.println( "Server: " + message );
		    }
	    	    reader.close();
		} catch ( IOException e ) {
			System.out.println( "" )
		    System.out.println( "\nDisconnected" );
		}
	    };

	    // User Input Reader
	    Runnable userInput = () -> {

		PrintWriter writer = null;
		try {
			writer = new PrintWriter( new BufferedWriter( new OutputStreamWriter( client.getOutputStream() ) ) );
		} catch ( Exception e ) { System.out.println( "Error: " + e ); }

		try {
		    System.out.println( "printw" );
		    BufferedReader input = new BufferedReader( new InputStreamReader( System.in ) );
		    System.out.print( "Enter desired nickname: " );
		    String name = input.readLine();
		    writer.print( name );

		    String outMessage;
		    while ( true ) {
			System.out.print( name + ": " );
			outMessage = input.readLine();
			if ( !outMessage.toLowerCase().equals( "exit" ) ) {
			    writer.print( outMessage );
			} else {
			    break;
			}
		    }
		    writer.close();
		} catch ( Exception e ) { System.out.println( "Exception: " + e ); }
	    };

	    Thread receiverThread = new Thread( receiver );
	    Thread inputThread = new Thread( userInput );

	    inputThread.start();
	    receiverThread.start();

	}
    }
}
