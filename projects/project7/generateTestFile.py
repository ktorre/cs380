import sys
import random

def main():
    filename = "testFile.txt"
    file = open( filename, 'w' )

    for y in range( random.randrange( 0, 10000) ):
        for x in range( random.randrange( 0, 10 ) ):
            file.write( str( random.randrange( 0, 999 ) ))
        file.write( "\n" )

main()
