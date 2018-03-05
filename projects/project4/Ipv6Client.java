// Author: Kevin De La Torre

import java.io.DataInputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.net.Inet6Address;

public class Ipv6Client { 

	public static void main( String args[] ) {
		try ( Socket client = new Socket( "codebank.xyz", 38004 ) ) {
			// Setup
			System.out.println( "Connected to server." );
			DataInputStream reader = new DataInputStream( client.getInputStream() );
			PrintStream writer = new PrintStream( client.getOutputStream(), true, "UTF-8" );

			for ( int i = 0, j = 2; i < 12; i++, j *= 2 ) {

				System.out.println( "Data length: " + j );

				byte[] packet = new byte[ 40 + j ]; // Allocate 36 bytes for header + bytes for data
				ByteBuffer buff = ByteBuffer.allocate( 2 );
				ByteBuffer checkSumBuff = ByteBuffer.allocate( 2 );
				buff.putShort( ( short )( j ) );
				byte[] tmpShortBuff = buff.array();

				// First 4 bytes - Version/traffic/flow
				packet[ 0 ] = ( byte )0b01100000;
				packet[ 1 ] = 0; 
				packet[ 2 ] = 0;
				packet[ 3 ] = 0;
				// Payload length - 2 Bytes
				packet[ 4 ] = tmpShortBuff[ 0 ];
				packet[ 5 ] = tmpShortBuff[ 1 ];
				// Next Header/Hop limit - 2 Bytes
				packet[ 6 ] = ( byte )0b00010001;
				packet[ 7 ] = ( byte ) 0b0010100;
				
				// Source address - 16 Bytes
				byte[] bytes = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, ( byte )0xFF, ( byte )0xFF, 0, 0, 0, 0 };

				byte[] sourceA = client.getLocalAddress().getAddress();

				bytes[ 12 ] = sourceA[ 0 ];
				bytes[ 13 ] = sourceA[ 1 ];
				bytes[ 14 ] = sourceA[ 2 ];
				bytes[ 15 ] = sourceA[ 3 ];

				int counter = 0;
				for ( byte b : bytes ) {
					packet[ 8 + counter++ ] = ( byte )( b & 0xff );
				}


				// Destination address - 16 Bytes
				byte[] dbytes = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, ( byte )0xFF, ( byte )0xFF, 0, 0, 0, 0 }; // Hardcoded ik I'm disgusting

				byte[] sourceB = client.getInetAddress().getAddress();
				dbytes[ 12 ] = sourceB[ 0 ];
				dbytes[ 13 ] = sourceB[ 1 ];
				dbytes[ 14 ] = sourceB[ 2 ];
				dbytes[ 15 ] = sourceB[ 3 ];

				counter = 0;
				for ( byte b : dbytes ) {
					packet[ 24 + counter++ ] = ( byte )( b & 0xff );
				}

				for ( int k = 0; k < j; k++ ) {
					packet[ 40 + k ] = 0x0;
				}

				// Fill packet array...

				for ( int k = 0; k < ( 40 + j ); k++ ) {
					writer.write( packet[ k ] );
				}
				//writer.write( packet, 0, ( 40 + j ) ); // Send full packet byte array

				byte[] response = new byte[ 4 ];
				for ( int k = 0; k < 4; k++ ) {
					response[ k ] = reader.readByte();
				}
				System.out.printf( "Response: 0x%08X\n\n", ByteBuffer.wrap( response ).getInt() );
			}

		} catch ( IOException e ) { System.out.println( "Error connecting to server." ); }
	}

}
