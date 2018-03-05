// Author: Kevin De La Torre

import java.io.InputStreamReader;
import java.util.Scanner;
import java.io.PrintStream;
import java.net.Socket;
import java.io.IOException;
import java.util.HashMap;
import java.nio.ByteBuffer;

public class Ipv4Client { 

	public static void main( String args[] ) {
		try ( Socket client = new Socket( "codebank.xyz", 38003 ) ) {
			// Setup
			System.out.println( "Connected to server." );
			Scanner reader = new Scanner( new InputStreamReader( client.getInputStream() ) );
			PrintStream writer = new PrintStream( client.getOutputStream(), true, "UTF-8" );

			for ( int i = 0, j = 2; i < 12; i++, j *= 2 ) {

				System.out.println( "Data length: " + j );

				byte[] packet = new byte[ 20 + j ]; // Allocate 20 bytes for header + bytes for data
				ByteBuffer buff = ByteBuffer.allocate( 2 );
				ByteBuffer checkSumBuff = ByteBuffer.allocate( 2 );
				buff.putShort( (short)( 20 + j ) );
				byte[] tmpShortBuff = buff.array();

				//packet[ 0 ] = (byte)01000101; // version/headerlength (5 words)
				packet[ 0 ] = (byte)0b01000101;
				packet[ 1 ] = 0; // tos ( not implemented )
				packet[ 2 ] = tmpShortBuff[ 0 ];
				packet[ 3 ] = tmpShortBuff[ 1 ];
				packet[ 4 ] = 0; // Ident 0-7 ( not implemented )
				packet[ 5 ] = 0; // Ident 8-15 ( not implemented )
				packet[ 6 ] = (byte)0b01000000;
				packet[ 7 ] = 0x0;
				packet[ 8 ] = (byte)0b00110010;
				packet[ 9 ] = (byte)0b00000110;
				packet[ 10 ] = 0; // tmp Checksum
				packet[ 11 ] = 0; // tmp Checksum pt2

				
				buff = ByteBuffer.allocate( 4 );
				byte[] bytes = client.getLocalAddress().getAddress();
				int counter = 0;
				for (byte b : bytes) {
					packet[ 12 + counter++ ] = ( byte )( b & 0xff );
				}

				packet[ 16 ] = ( byte )0b00110100; // 52   ik ugly but I'm out of time, so hardcode we go :/
				packet[ 17 ] = ( byte )0b00100001; // .33
				packet[ 18 ] = ( byte )0b10110101; // .181
				packet[ 19 ] = ( byte )0b01110010; // .114

				checkSumBuff.putShort( checksum( packet, 20 ) );
				byte[] tmpBuff = checkSumBuff.array();
				packet[ 10 ] = tmpBuff[ 0 ];
				packet[ 11 ] = tmpBuff[ 1 ];
	
				for ( int k = 1; k <= j; k++ ) {
					packet[ 19 + k ] = 0x0;
				}

				// Fill packet array...

				for ( int k = 0; k < ( 20 + j ); k++ ) {
					writer.write( packet[ k ] );
				}
				//writer.write( packet, 0, ( 20 + j ) ); // Send full packet byte array
				System.out.println( "Response: " + reader.nextLine() + "\n" );
			}


		} catch ( IOException e ) { System.out.println( "Error connecting to server." ); }
	}

	public static short checksum( byte[] b, int length ) {

		int sum = 0;
		int counter = 0;

		while ( length > 0 ) {
			if ( length == 1 ) {
				sum += ( b[ counter ] << 8 & 0xFF00 );
			} else {
				sum += ( ( ( b[ counter++ ] << 8 ) & 0xFF00 ) | ( b[ counter++ ] & 0xFF ) );
			}
				if ( ( sum & 0xFFFF0000 ) > 0 ) {
				sum &= 0xFFFF;
				sum++;
			}
			length -= 2;
		}

		sum = ~sum;
		sum = sum & 0xFFFF;
		return (short)sum;
	}
}
