Overview
========

Weblogic web app that provides a couple of services that gives a graphical preview of a BPEL process.The app use a graph template and update the template checking the state of the running instance

For the construction of the graph, the app use [mermaid.js](http://knsv.github.io/mermaid/#usage). The web services modify the template with all the styles so it is easy to know what is the exact execution flow and the actual state of the process. 

Bpel flowchart preview
----------------------

Java app that updates a template graph checking the real state of a BPEL process using the SOA and human task API. That template graph only shows the milestones that the user want to be shown, so many technical details of the process can maintain hidden.

The next image is a graph of an actual BPEL. It's easy to identify, the begin and end task, the human task with his outcome (green node), the conditional node (diamond gray node), service calls (blue node), and other objects.

![TestBPEL](/images/graph.png)


### MermaidJS

Is a javascript library for the construction of graphs, sequence diagramas o gant diagrams. The diagramas are constructed just with text, defining the nodes and the links between them. 

MermaidJS allow to

- Build graphs in text mode. (nodes, links and style)
- Mermaid renders the graph on the browser.
- Also mermaidJS has an utility of command line to transform the graph in an actual PNG image. 

