package(default_visibility = ["//visibility:public"])

java_library(
    name = "rnlp",
    srcs = glob(["**/*.java","**/*.properties"]),
    deps = ["rnlp_jars"],
    resources = [
        "src/main/resources/crfsuite",
        "src/main/resources/configuration.conf",
	"src/main/resources/hibernate.cfg.xml",
	"src/main/resources/log4j.properties",
        "src/main/resources/crfsuiteModels/generalHealthRelated/crfsuite.model",
        "src/main/resources/crfsuiteModels/twitter/crfsuiteTwitter.model",
        "src/main/resources/DSOct314-classes-sorted.txt",
        "src/main/resources/TwitterOct0514-classes-sorted.txt",
	   ],
     data =["exported_data"],
	

)
java_import(
        name = "rnlp_jars",
        jars = [
            "libs/antlr-2.7.7.jar",
            "libs/dom4j-1.6.1.jar",
            "libs/dragontool.jar",
            "libs/hibernate-commons-annotations-4.0.1.Final.jar",
            "libs/hibernate-core-4.1.3.Final.jar",
            "libs/hibernate-envers-4.1.3.Final.jar",
            "libs/hibernate-jpa-2.0-api-1.0.1.Final.jar",
            "libs/javassist-3.15.0-GA.jar",
            "libs/jboss-logging-3.1.0.GA.jar",
            "libs/jboss-transaction-api_1.1_spec-1.0.0.Final.jar",
            "libs/jgrapht-jdk1.6.jar",
            "libs/junit-4.12.jar",
            "libs/log4j-over-slf4j-1.6.4.jar",
            "libs/mallet-2.0.7.jar",
            "libs/mysql-connector-java-5.1.14-bin.jar",
            "libs/slf4j-api-1.6.4.jar",
            "libs/slf4j-log4j12-1.6.4.jar",
            "libs/slf4j-simple-1.6.4.jar",
            "libs/stanford-parser-2012-05-22-models.jar",
            "libs/stanford-parser.jar",
            "libs/stanford-postagger.jar",
            "libs/stringtotime-1.0.4.jar",
            "libs/weka.jar",
            ]),
 
filegroup(
    name = "exported_data",
    srcs = glob([
		"lemmatiser/**",
		"**/lemmatiser/**",
        "**/lemmatiser/*.exc",
		"**/lemmatiser/*.list",
		"**/lemmatiser/*.index"
    ]),
)
