# How to analyse Apex code with Kiuwan

This document explains how to analyse Apex code with Kiuwan. There are 2 main steps, preparation and execution.
Preparation should be done only once.

1.Set environment
=================

1.1 Convert PMD rulesets to Kiuwan rulesets:
   - Verify Java 8 is installed on your system and it can be found from a command line: execute *"java -version"*
   - Download this github project to any directory on Windows PC or Linux server.
   - in the ./ConversorPMDRules2KiuwanRules/rules directory you should copy the PMD Apex rulesets. You can take from the PMD distribution, folder: /pmd-src-{version}/pmd-apex/src/main/resources/**category**/apex
   -- Take into account that you should copy the    xml files from the category folder
   -- By default you will find in the folder "rules" PMD APEX rulesets from PMD 6.12.
   - Verify the category mappings configuration file: ./conf/CategoryMappings.csv
   Every line of this file has a mapping of a PMD ruleset file (file name without extension) --> Kiuwan rule category. These can have the values:
   PORTABILITY, RELIABILITY, EFFICIENCY, MAINTAINABILITY, SECURITY
   The line starting with "default_category" has the Kiuwan category for all PMD file names that are not in this list: the default value.
   - Open a command terminal (Windows CMD or Linux shell) and navigate to directory ./script
   - Execute .\start.cmd or ./start.sh for Windows or Linux, respectively. Both scripts only execute a Java program. 
   The script can be adapted as needed. Executing the script, it leaves a new directory in .\rules\Kiuwan with all the new Kiuwan rules (*.xml) and a zip file (.\rules\KiuwanRules.zip) containing these rules.
 
1.2 Import Kiuwan rules into Kiuwan model
 - In the Kiuwan web interface, import these rules: *Models management --> Rules --> (Menu) Install rule definitions --> Upload --> select KiuwanRules.zip file --> add to model*
 - Same for the xml definition of the Apex report plugin rule, import: CUS.MCP.KIUWAN.RULES.APEX.Plugin.rule.xml and add to the model.

**1.3 Publish model**
- Click on Publish to be able to use this model in your applications.

1.4 Add jar to Kiuwan Local Analyzer (KLA)
 - Copy "apex-kiuwan-plugin-1.0.jar" to the folder of your Kiuwan Local Analyzer installation: **\KiuwanLocalAnalyzer\lib.custom\
 - Or if you prefer you can compile the source code of this library (src folder)

2.How to execute an Apex analysis
=====================================

2.1 Execute PMD tool to generate XML report: apex.xml. The name of the report could be configured as parameter in the rule: "Apex report plugin rule"

Example windows execution:
pmd.bat -d c:\my\source\code -f xml -R rulesets/apex/quickstart.xml > c:\my\source\code\apex.xml

You can see how to generate an xml report here: 
https://pmd.github.io/latest/pmd_userdocs_installation.html#running-pmd-via-command-line

2.2 This xml must be copied or generated in the path application

Example if you are analyzing the application "My First Apex app" and the path is: "c:\my\source\code"
Then the xml PMD report should be generated in that path. The reason is because the Kiuwan rule will look for PMD report in the analysis path.

2.3 Execute Kiuwan Local Analyzer, or CLI command

Kiuwan Apex rules will look for the PMD report in the path of analysis (in the example "/source/apex/MyFirstApp/") or in any subfolder.

2.4 Look up results in Kiuwan web interface.

 
