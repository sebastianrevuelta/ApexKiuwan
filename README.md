# Apex in Kiuwan

This document explains wow to analyse Apex files with Kiuwan.

1.Prepare test environment: only to do once (Except changes)
=====================================

1.1 Convert PMD rulesets to Kiuwan rulesets:
   - Verify Java 8 is installed on your system and it can be found from a command line: execute *"java -version"*
   - Unpack the tool RulesPMDtoKiuwan.zip to any directory on Windows PC or Linux server.
   - in the ./rules directory you should copy the PMD Apex rulesets. You can take from the PMD distribution, folder: /pmd-src-{version}/pmd-apex/src/main/resources/**category**/apex
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

2.How to execute an Apex analysis
=====================================

2.1 Execute PMD tool to generate XML report: apex.xml. You can see how to generate an xml report here: https://pmd.github.io/latest/pmd_userdocs_installation.html#running-pmd-via-command-line

2.2 This xml must be copied or generated in the path application
Example if you are analyzing the application "My First Apex app" and the path is: "/source/apex/MyFirstApp/
Then the xml PMD report should be in that path.

2.3 Execute Kiuwan Local Analyzer, or CLI command
Kiuwan Apex rules will look for the PMD report in the path of analysis (in the example "/source/apex/MyFirstApp/") or in any subfolder.

2.4 Look up results in Kiuwan web interface.

 
