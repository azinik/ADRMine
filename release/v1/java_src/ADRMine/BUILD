# ~/gitroot/base_workspace/my-project/BUILD
java_binary(
    name = "adrmine",
    srcs = glob(["**/*.java","**/*.properties"]),
    deps = ["//rnlp-stb:rnlp", "//rnlp-stb:rnlp_jars", "adrmine_jars"],
    main_class = "edu.asu.diego.adrmine.ner.mainADRMinePipelineForExtraction",
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
            ],
    )
