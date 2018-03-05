CLIENTFILE = PhysLayerClient

$(CLIENTFILE).class : $(CLIENTFILE).java
	@echo -n Compiling $(CLIENTFILE)...
	@javac $(CLIENTFILE).java
	@echo Done

run:
	@clear
	@make --silent
	@echo -n -e Running...\n\n
	@java $(CLIENTFILE)

clean:
	@echo -n Cleaning...
	@rm -rf *.class
	@echo Done
