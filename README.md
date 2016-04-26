# ADRMine
ADRMine is an NER (Named Entity Recognition) tool which is trained for extraction of medical concepts, particularly ADRs (Adverse Drug Reactions) from user posts in social media.

Requirements

ADRMine uses MYSQL database, please install MYSQL if you don't have on your machine.

1)First create a mysql database, e.g. ADRMineDB:
           CREATE DATABASE `ADRMineDB`;

2)Restore the provided schema:

mysql --user=[MYSQL_user] --password=[your_MYSQL_password]  ADRMineDB< adrmine-db-schema-nodata.sql

Note: The code uses Hibernate for connecting to the MYSQL database. It is possible to automatically generate the MYSQL tables, but since by default it generates the MYSQL “InnoDB” tables and we intend to create “MyISAM” tables (since it is faster), we provided the schema which needs to be restored first.

How to use ADRMine?
The current version of ADRMine includes the trained models for  ADR extraction. It is a jar file (adrmine_deploy.jar) that gets the test sentences as input and automatically tags the ADR mentions in the given sentences and generates a text file with ADR tags. 

Usage example:
java -jar adrmine_deploy.jar /tmp/test_sentences.tsv    twitter1 ADRMineDB root password  /home/azadeh/software-packages/crfsuite-0.12-2/bin/crfsuite

Parameters:
1 ) test file (e.g. /tmp/test_sentences.tsv)
	This input  is a text file that each line contains the text id and the content. The content, for example can be a user tweet containing one or more sentences.  
Example line: ID123<tab>This drug made me gain a lot of weight.

2) Corpus name: This is a name that you choose for your dataset. Please choose a different name for each separate experiment.
Please note that the system is trained on user posts in health related social networks (e.g. DailyStrength.org) and  Twitter. We trained a different model for user tweets, so if your test sentences are from twitter, include “twitter” as part of your corpus name “e.g. My-twitter-set1”.
3) your database name (e.g. ADRMinedb)
4) database user
4) database pass
6) path to the CRFSuite executable file (optional)
The CRFSuite executable file is an optional parameter. The system uses the crfsuite executable file, but if you have CRFsuite on your system, you can also pass the binary file path.

Output:
The output file includes the text ID, start and end character offset, type, extracted text span
Example output line:
ID123	18	37	ADR	gain a lot of weight

TODO (future releases)
Adding normalization to UMLS Ids
Add training functionality to the tool, so that it can be trained on any annotated corpus for concept extraction
Improving the speed of training and test. Currently it may be relatively slow for large corpora with more than hundred thousands sentences.


Citing ADRMine
Please cite this paper: Nikfarjam A, et al. Journal of the American Medical Informatics Association 2015;0:1–11. doi:10.1093/jamia/ocu041

Contact
If you have any questions please contact Azadeh Nikfarjam: anikfarj@asu.edu


