// Author: Kevin De La Torre

import java.net.Socket;
import java.io.*;
import java.util.Scanner;

public class TicTacToeClient {
	public static void main( String[] args ) throws Exception {
		try ( Socket client = new Socket( "codebank.xyz", 38006 ) ) {
			ObjectOutputStream out = new ObjectOutputStream( client.getOutputStream() );
			ObjectInputStream in = new ObjectInputStream( client.getInputStream() );
			Scanner input = new Scanner( System.in );

			out.writeObject( new ConnectMessage( "Kevin" ) );
			out.writeObject( new CommandMessage( CommandMessage.Command.NEW_GAME ) );
			boolean valid = true;
			while ( valid ) {
				BoardMessage bMessage = ( BoardMessage ) in.readObject();
				if ( (String)!bMessage.getStatus().equals("IN_PROGRESS") ) {
					System.out.println( "Game over." );
				} else {
					drawBoard(  bMessage.getBoard() );
					System.out.print( "Your move ( row ): " );
					byte x = input.nextByte();
					System.out.print( "Your move ( column ): " );
					byte y = input.nextByte();
					out.writeObject( new MoveMessage( x, y ) );
				}
				
			}
			


		} catch ( IOException e ) {
			System.out.println( "Error connecting to server." );
		}
	}

	public static void drawBoard( byte[][] board ) {
		for ( int i = 0; i < board[ 0 ].length; i++ ) {
			for ( int j = 0; j < board.length; j++ ) {
				System.out.print( board[ i ][ j ] );
			}
			System.out.println();
		}
	}
}
