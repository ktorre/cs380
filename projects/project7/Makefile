COMPILEFILE = FileTransfer

$(COMPILEFILE).class : $(COMPILEFILE).java
	@echo -n Compiling $(COMPILEFILE)...
	@javac $(COMPILEFILE).java
	@echo Done
	@echo -n Generating test file...
	@python generateTestFile.py 
	@echo Done

edit:
	@make --silent clean;
	@clear
	@vim $(COMPILEFILE).java

clean:
	@echo -n Cleaning...
	@rm -rf *.class
	@echo Done
