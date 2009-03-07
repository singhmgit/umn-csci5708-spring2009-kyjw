LIBPATH = ../../lib/bufmgrAssign.jar
CLASSPATH = .:..:$(LIBPATH)
JAVAC = javac -classpath $(CLASSPATH)
JAVA  = java  -classpath $(CLASSPATH)

PROGS = together

all: $(PROGS)

db:DB.java
	$(JAVAC) DB.java

together:*.java
	$(JAVAC) *.java

xx:
	jar tf $(LIBPATH)

clean:
	\rm -f *.class *~ \#* core 
