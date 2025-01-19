install:
	apt install -y openjdk-21-jdk
	apt install -y maven
	mvn clean install

build:
	mvn compilee exec:java -Dexec.mainClass="dev.osunolimits.main.App"

run:
	mvn exec:java -Dexec.mainClass="dev.osunolimits.main.App"
	
