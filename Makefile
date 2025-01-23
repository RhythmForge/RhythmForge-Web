install:
	apt install -y openjdk-21-jdk
	apt install -y maven
	mvn clean install

build-clean:
	mvn clean compile exec:java -Dexec.mainClass="dev.osunolimits.main.App"

build:
	mvn compile exec:java -Dexec.mainClass="dev.osunolimits.main.App"


build-e:
	mvn compile exec:java -Dexec.mainClass="dev.osunolimits.main.App" -e

run:
	mvn exec:java -Dexec.mainClass="dev.osunolimits.main.App"
	
