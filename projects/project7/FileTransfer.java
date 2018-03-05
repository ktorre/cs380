// Author: Kevin De La Torre

import java.io.*;
import java.security.*;
import java.net.*;
import java.util.Scanner;
import java.util.zip.CRC32;
import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;

public class FileTransfer {
	public static void main( String[] args ) throws Exception {
		if ( args.length > 0 ) {
			if ( args[ 0 ].equals( "makekeys" ) ) {
				makeKeys();
			} else if ( args[ 0 ].equals( "server" ) ) {
				startServer( args );
			} else if ( args[ 0 ].equals( "client" ) ) {
				startClient( args );
			} else {
				printUsage();
			}

		} else {
			printUsage();
		}
	}

	public static void printUsage() {
		System.out.println( "Usage: FileTransfer <command> <command-args>" );
		System.out.println( "Commands:" );
		System.out.println( " - makekeys						- Generate public/private RSA key pair" );
		System.out.println( " - server <private key file> <listening port #> 	- Execute server mode" );
		System.out.println( " - client <public key file> <host> <listening port #>   - Execute client mode" );

	}

	public static void makeKeys() {
		try {
			System.out.print( "Generating keys..." );
			KeyPairGenerator gen = KeyPairGenerator.getInstance( "RSA" );
			gen.initialize( 4096 ); // you can use 2048 for faster key generation
			KeyPair keyPair = gen.genKeyPair();
			PrivateKey privateKey = keyPair.getPrivate();
			PublicKey publicKey = keyPair.getPublic();
			try ( ObjectOutputStream oos = new ObjectOutputStream( new FileOutputStream( new File( "public.bin" ) ) ) ) {
				oos.writeObject( publicKey );
			}
			try ( ObjectOutputStream oos = new ObjectOutputStream( new FileOutputStream( new File( "private.bin" ) ) ) ) {
				oos.writeObject( privateKey );
			}
			System.out.println( "done" );
		} catch ( NoSuchAlgorithmException | IOException e ) {
			e.printStackTrace( System.err );
		}
	}

	public static void startServer( String[] args ) throws Exception {
	    if ( args.length != 3 ) { printUsage(); }
	    System.out.print( "Initializing server on port " + args[ 2 ] + "..." );
	    try ( ServerSocket server = new ServerSocket( Integer.parseInt( args[ 2 ] ) ) ) {
		System.out.println( "success" );
				while ( true ) {
		    System.out.println( "Waiting on clients..." );
		    try ( Socket client = server.accept() ) {
			String address = client.getInetAddress().getHostAddress();
			System.out.printf( "Client connected: %s%n", address );
			ObjectOutputStream out = new ObjectOutputStream( client.getOutputStream() );
			ObjectInputStream in = new ObjectInputStream( client.getInputStream() );

			Message initialMessage = ( Message )in.readObject();
			if ( initialMessage.getType().toString().equals( "DISCONNECT" ) ) {
			    System.out.print( "Disconnecting client..." );
			    client.close();
			    System.out.println( "done" );
			} else if ( initialMessage.getType().toString().equals( "START" ) ) {
			    try { // Prep for file transfer
				StartMessage startMessage = ( StartMessage ) initialMessage;
				ObjectInputStream privateKey = new ObjectInputStream( new FileInputStream( new File( "private.bin" ) ) );
				Cipher c = Cipher.getInstance( "RSA" );
				c.init(Cipher.UNWRAP_MODE, (PrivateKey)privateKey.readObject() ); 
				SecretKey sessionKey = (SecretKey) c.unwrap( startMessage.getEncryptedKey(), "AES", Cipher.SECRET_KEY ); // Get session key
				FileOutputStream file = new FileOutputStream( new File( "Received_" + startMessage.getFile() ) );
				int chunkSize = startMessage.getChunkSize();
				int numChunks = ( int ) Math.ceil( ( new File( startMessage.getFile() ).length() ) / chunkSize );
				Cipher cipher = Cipher.getInstance("AES");
				cipher.init(Cipher.DECRYPT_MODE, sessionKey);
				CRC32 crc = new CRC32();

				out.writeObject( new AckMessage( 0 ) );

				for ( int i = 0; i <= numChunks; i++ ) {
				    try {
					Chunk chunk = ( Chunk )in.readObject();
					if ( chunk.getSeq() == i ) {
					    byte[] decryptedByte = cipher.doFinal( chunk.getData() );
					    crc.update( decryptedByte );
					    if ( (int)crc.getValue() == chunk.getCrc() ) {
						file.write( decryptedByte );

						System.out.printf( "Chunk received [ %d/%d ].%n", ( i + 1 ), numChunks );
						out.writeObject( new AckMessage( i + 1 ) );
					    } else {
						out.writeObject( new AckMessage( i-- ) ); // repeat ack
					    }
					} else {
					    out.writeObject( new AckMessage( i-- ) ); // same
					}
										
				    } catch ( Exception e ) {
					System.out.println( "Failed to retrieve chunk." );
				    }
				}
				file.close();
			    } catch ( Exception e ) {
				out.writeObject( new AckMessage( -1 ) );
			    }
			}
		    }
		}
		
	    }
	}

	public static void startClient( String[] args ) throws Exception {
	    if ( args.length != 4 ) { printUsage(); }
	    System.out.print( "Connecting to " + args[ 2 ] + ":" + args[ 3 ] + "..." );
	    try ( Socket client = new Socket( args[ 2 ], Integer.parseInt( args[ 3 ] ) ) ) {
		System.out.println( "connected" );
		ObjectOutputStream out = new ObjectOutputStream( client.getOutputStream() );
		ObjectInputStream in = new ObjectInputStream( client.getInputStream() );
		ObjectInputStream publicKey = new ObjectInputStream( new FileInputStream( new File( "public.bin" ) ) );
		Scanner input = new Scanner( System.in );

		Cipher c = Cipher.getInstance( "RSA" );

		SecretKey sessionKey = new SecretKeySpec(new byte[16], "AES");
		c.init( Cipher.WRAP_MODE, (PublicKey)publicKey.readObject() );
		byte[] encryptedKey = c.wrap( sessionKey );

		String path;
		do {
		    System.out.print( "Enter path: " );
		    path = input.nextLine();
		    if ( path.equals( "DISCONNECT" ) ) { continue; }
		    File file = new File( path );
		    if ( file.exists() ) {
			FileInputStream fileReader = new FileInputStream( file );

			System.out.print( "Enter chunk size [ 1024 ]: " );
			double chunkSize;
			String chSizeTest = input.nextLine(); // Used to check for empty string
			if ( chSizeTest.equals( "" ) ) { // Empty string => default
			    chunkSize = 1024;
			} else {
			    chunkSize = Double.parseDouble( chSizeTest );
			}

			int numChunks = ( int ) Math.ceil( file.length() / chunkSize );
			out.writeObject( new StartMessage( file.getName(), encryptedKey, ( int ) chunkSize ) );
			System.out.printf( "Sending: %s  |  File size: %d.%n", file.getName(), file.length() );
			System.out.printf( "Sending %d chunks.%n", numChunks );

			CRC32 crc = new CRC32();
			System.out.print( "Waiting for server status..." );
			AckMessage initialAck = (AckMessage) in.readObject(); // Need initial ack of 0 to begin transfer, else cancel
			Cipher cipher = Cipher.getInstance("AES");
			cipher.init(Cipher.ENCRYPT_MODE, sessionKey);
			byte[] encryptedChunk;

			if ( initialAck.getSeq() == 0 ) {
			    System.out.println( "acknowledged." );
			    for ( int i = 0; i < numChunks; i++ ) {
				byte[] currentChunk;
			        if ( i == ( numChunks - 1 ) ) { // if last one, shorten bytearray to what's left
				    currentChunk = new byte[ ( int ) file.length() % ( int )chunkSize ];
			        } else {
			            currentChunk = new byte[ ( int ) chunkSize ];
			        }
				fileReader.read( currentChunk );
				crc.update( currentChunk );
				
				encryptedChunk = cipher.doFinal( currentChunk );
				Chunk chunk = new Chunk( i, encryptedChunk, ( int )crc.getValue() ); // Create chunk

				try {
				    out.writeObject( chunk ); // Send chunk
				    System.out.printf( "Chunks completed [ %d/%d ].%n", ( i + 1 ), numChunks );
				    AckMessage ack = ( AckMessage ) in.readObject(); // Get next ack from server
				    if ( ack.getSeq() == -1 ) {
					System.out.println( "File transfer error." );
				    } else if ( ack.getSeq() != ( i + 1 ) && ( i + 1 ) < numChunks ) { // If server sends same ack, repeat chunk
					i--; // Repeat chunk if not sent correctly
				    }
				} catch ( Exception e ) { 
				    out.writeObject( new StopMessage( file.getName() ) ); // If something happens stop transfer
				}
			    }
			} else {
			    System.out.println( "File transfer error." );
			}
		    } else {
		    System.out.println( "Invalid file." );
		}

		} while ( !path.equals( "DISCONNECT" ) ); 
		out.writeObject( new DisconnectMessage() );
	    } catch ( IOException e ) {
		System.out.println( "Error connecting to server." );
	    }
	}
}
