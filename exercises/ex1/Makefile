SERVER = Server.java
CLIENT = Client.java

all : Server.class Client.class

Server.class : Server.java
	@echo -n Compiling server...
	@javac Server.java
	@echo Done

Client.class : Client.java
	@echo -n Compiling client...
	@javac Server.java
	@echo Done

clean:
	@echo -n Deleting class files...
	@rm -rf *.class
	@echo Done
