JFLAGS = -g
JC = javac

CLASSES = \
    Hangman.java \
    FileUtils.java \
    Server.java \
	ServerClientConnection.java \
    ClientGame.java \
    ClientTCP.java \
    ServerTCP.java \
    ClientUDP.java \
    ServerUDP.java

COMMON_CLASSES = \
	Server.class \
	Hangman.class \
	FileUtils.class \
	ServerClientConnection.class \
	ClientGame.class

default: compile

compile:
	$(JC) $(JFLAGS) $(CLASSES)
	
runTCP: ServerTCP.class ClientTCP.class $(COMMON_CLASSES)
	java ServerTCP $(F) &
	sleep 1
	java ClientTCP

runUDP: ServerUDP.class ClientUDP.class $(COMMON_CLASSES)
	java ServerUDP $(F) &
	sleep 1
	java ClientUDP

.PHONY: clean
clean:
	rm -rf *.class
