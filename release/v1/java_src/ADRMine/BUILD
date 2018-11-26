# ~/gitroot/base_workspace/my-project/BUILD
java_binary(
    name = "adrmine",
    srcs = glob(["**/*.java","**/*.properties"]),
    deps = ["//rnlp:rnlp", "//rnlp:rnlp_jars", "adrmine_jars"],
    main_class = "edu.asu.diego.adrmine.ner.MainADRMine",
    resources = [
        "src/resources/DSOct314-classes-sorted.txt",
        "src/resources/TwitterOct0514-classes-sorted.txt",
        "src/resources/crfsuiteModels/generalHealthRelated/crfsuite.model",
        "src/resources/crfsuiteModels/twitter/crfsuiteTwitter.model",
     ],
     data = glob(["src/resources/ADRTokenizedLexiconLuneceIndex/**"])

)

java_import(
        name = "adrmine_jars",
        jars = [
            "libs/lucene-core-3.6.2.jar",
            "libs/lucene-spellchecker-3.6.1.jar",
            "libs/commons-io-2.5.jar",
            "libs/commons-cli-1.4.jar",
            ],
    )
