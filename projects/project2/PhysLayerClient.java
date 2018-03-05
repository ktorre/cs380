// Author: Kevin De La Torre

import java.io.DataInputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.io.IOException;
import java.util.HashMap;

public class PhysLayerClient {
	static final int PREAMBLE_LENGTH = 64;
	static final int MESSAGE_LENGTH = 32;
	public static void main( String args[] ) {
		try ( Socket client = new Socket( "codebank.xyz", 38002 ) ) {
			// Setup
			System.out.println( "Connected to server." );
			DataInputStream reader = new DataInputStream( client.getInputStream() );
			PrintStream writer = new PrintStream( client.getOutputStream(), true, "UTF-8" );

			// Establish baseline
			float average = 0;
			for ( int i = 0; i < PREAMBLE_LENGTH; i++ ) {
				average += reader.readByte() & 0xFF;
			}
			average /= PREAMBLE_LENGTH;
			System.out.printf( "Baseline established from preamble: %.2f\n", average );

			// Retrieve 32 bytes of 4b/5b encoded data
			byte[] receivedBytes = new byte[ MESSAGE_LENGTH ];
			System.out.print( "Received " + MESSAGE_LENGTH + " bytes: " );
			String signal = "";
			int[] signalBits = new int[ MESSAGE_LENGTH ];
			for ( int i = 0; i < MESSAGE_LENGTH; i++ ) {
				receivedBytes[ i ] = reader.readByte();
				System.out.printf( "%02X", receivedBytes[ i ] );
				if ( ( receivedBytes[ i ] & 0xFF ) > average ) {
					signal += "1";
					signalBits[ i ] = 1;
				} else {
					signal += "0";
					signalBits[ i ] = 0;
				}
			}
			System.out.println( "\nPre-NZRI:  " + signal );

			int[] newBits = new int[ MESSAGE_LENGTH ];
			newBits[ 0 ] = signalBits[ 0 ];
			String bits = "";
			bits += newBits[ 0 ];
			for ( int i = 0; i < ( MESSAGE_LENGTH - 1 ); i++ ) {
				if ( signalBits[ i ] != signalBits[ i + 1 ] ) {
					newBits[ i + 1 ] = 1;
				} else { newBits[ i + 1 ] = 0; }
				bits += newBits[ i + 1 ];
			}
			System.out.println( "Post-NZRI: " + bits );

			HashMap dict = loadDict();
			String bitByte = "";
			byte[] msgBytes = new byte[ MESSAGE_LENGTH ];
			//int counter = 0;
			for ( int i = 0; i < MESSAGE_LENGTH; i++ ) {
				msgBytes[ i ] = (byte)newBits[ i ];
				//bitByte += newBits[ i ];
				//if ( ( i + 1 ) % 5 == 0 ) {
				//	msgBytes[ counter++ ] = (byte)dict.get( bitByte );
				//	bitByte = "";	
				//}
			}

			

			// Send byte array
			writer.write( msgBytes, 0, 32 );
			System.out.print( "\nResponse: " );
			if ( reader.readByte() == 1 ) {
				System.out.println( "good." );
			} else {
				System.out.println( "bad." );
			}

			// Cleanup
			reader.close();
			writer.close();
			client.close();
			System.out.println( "Disconnected from server." );

		} catch ( IOException e ) { System.out.println( "Error connecting to server." ); }
	}

	public static HashMap loadDict() {
		// code contains 5 bits
		HashMap dict = new HashMap( 16 );

		dict.put( "11110", (byte)0000 );
		dict.put( "01001", (byte)0001 );
		dict.put( "10100", (byte)0010 );
		dict.put( "10101", (byte)0011 );
		dict.put( "01010", (byte)0100 );
		dict.put( "01011", (byte)0101 );
		dict.put( "01110", (byte)0110 );
		dict.put( "01111", (byte)0111 );
		dict.put( "10010", (byte)1000 );
		dict.put( "10011", (byte)1001 );
		dict.put( "10110", (byte)1010 );
		dict.put( "10111", (byte)1011 );
		dict.put( "11010", (byte)1100 );
		dict.put( "11011", (byte)1101 );
		dict.put( "11100", (byte)1110 );
		dict.put( "11101", (byte)1111 );
		return dict;
	}
}
