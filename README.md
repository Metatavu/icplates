# ICPlates
Computer Vision based application for categorizing plates

# Setup
These instructions assume that system is being installed on machine with Ubuntu 16.04 OS.

### prerequisites
#### Install Java 8 and Maven

```
sudo apt update
sudo apt install openjdk-8-jdk maven
```
### Installation

ICPlates is available only as source code, so you need to build it first.

#### Building from source
clone the repository
```
git clone git@github.com:Metatavu/icplates.git
```

```
cd icplates
mvn install
```

### Running
Run the application with following command:
```
java -jar target/icplates-0.0.1-SNAPSHOT-jar-with-dependencies.jar
```
### Options

    usage: java -jar icplates.jar
     -c,--thrad-count <arg>        Thread count used for analyzing. Defaults
                                   to 3
     -h,--help                     Prints help
     -i,--print-images <arg>       Print visualization of discovered patterns
                                   into specified folder.
     -s,--scrore-threshold <arg>   Score threshold (0 is prefect match).
                                   Defaults to -1000
     -t,--templates-folder <arg>   Templates folder
