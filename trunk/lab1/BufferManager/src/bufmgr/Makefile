LIBPATH = ../../lib/bufmgrAssign.jar
CLASSPATH = .:..:$(LIBPATH)
BINPATH = $(JDKPATH)/bin
JAVAC = javac -classpath $(CLASSPATH)
JAVA  = java  -classpath $(CLASSPATH)
PROGS = together

all: $(PROGS)

bm:BufMgr.java
	$(JAVAC) BufMgr.java

together:*.java
	$(JAVAC) *.java

xx:
	jar tf $(LIBPATH)

clean:
	\rm -f *.class *~ \#* core 
