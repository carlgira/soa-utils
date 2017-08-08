# Configuration

**Clone the repository**

	- git clone https://github.com/carlgira/soa-utils.git

**Open the project**
	
  - cd soa-utils/bpel-flowchart-preview

**Edit property "mdw.home" in pom.xml with ENV variable**

**Create Jar**
- mvn clean package

**Create directory for the configuration files (graph_dir)**

- mkdir -p /home/oracle/soa-server/config/graph_dir


**Make a copy of file bpel-flowchart-preview/src/test/resources/bpel-flowchart-preview.properties to /home/oracle/soa-server/config/bpel-flowchart-preview.properties**

- cp src/test/resources/bpel-flowchart-preview.properties /home/oracle/soa-server/config/bpel-flowchart-preview.properties

**Edit the file bpel-flowchart-preview.properties. The properties "server", "user", "password", "realm" and "graph_dir".**

server=t3://localhost:8001/soa-infra/ </br>
user=weblogic </br>
password=weblogic </br>
realm=jazn.com </br>
graph_dir=/home/oracle/soa-server/config/graph_dir </br>

**Add in weblogic startup the property bpel-flowchart-preview with the path of the bpel-flowchart-preview.properties file.** </br>
	-Dbpel-flowchart-preview=/home/oracle/soa-server/config/bpel-flowchart-preview.properties

**Install the war in Weblogic bpel-flowchart-preview-1.0.war.**

# Mermaid Graphs Templates

All the mermaid graph templates must be in the directory "graph_dir". Use as an example the file bpel-flowchart-preview/src/test/resources/default.TestSoaProject.1.0.TestBPEL.txt.

The name of the template must follow the next REGEX.

	<partition>.<CompositeName>.<version|"last">.<BPELName>.txt
 
You can specify the composite version or put the word "last". The "last" file works as default template if no version is founded. 

# Documentation

To see some details of the creation of mermaid graph templates, use maven site plugin:
	- mvn site:run

Open http://localhost:8080

# 
Mermaid JS 

https://knsv.github.io/mermaid/

https://knsv.github.io/mermaid/live_editor/
