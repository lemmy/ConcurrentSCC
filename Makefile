compile:
	javac -d bin/ -cp src src/tarjanUF/*.java

clean:
	rm -rf bin
	mkdir bin

run:
	java -cp bin tarjanUF.Main ./sample.txt
