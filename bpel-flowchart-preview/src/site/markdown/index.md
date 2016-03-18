Overview
========

Se ha construido un nuevo componente para visualizar el estado de una petición de un proceso BPEL. El componente cuenta con varios servicios que se encargan de construir un grafo utilizando una plantilla prediseñada de un componente y lo compara con el estado actual de una instancia en ejecución.

Se ha utilizado la librería de [mermaid.js](http://knsv.github.io/mermaid/#usage). para la construcción de los diagramas. Los servicios Java se encargan de modificar esa plantilla y enriquecerla con los estilos adecuados para poder identificar el flujo de ejecución y el estado actual del proceso.

El siguiente documento presenta todos los detalles técnicos relevantes del componente desarrollado,  información sobre su utilización, configuración e instalación.



Bpel flowchart preview
----------------------

Aplicación java que se encarga de realizar peticiones al motor de SOA utilizando el API de tareas humanas y el API de BPEL, para obtener el estado actual de instancias y actualizar una template de un grafo diseñado a partir de milestones de un proceso BPEL. Utilizando la instancia BPEL y la template de un proceso, los servicios se encargan de enriquecer la template de manera que se pueda notar fácilmente por donde exactamente ha pasado el proceso.

El siguiente es un grafo de ejemplo de un proceso de alta de agentes. Se puede identificar, las tareas de inicio o fin, tareas humanas con sus “outcomes” (nodos en verde), condicional (rombo gris) y llamadas a servicios (nodo azul)

![TestBPEL](/images/graph.png)


### MermaidJS

Es una librería en javascript para la construcción de grafos, diagramas de secuencia, o diagramas de gant.  La construcción de los diagramas se hace solo con texto, definiendo los nodos y los enlaces entre los nodos. 

- Construcción de grafos en modo texto. Se describen los nodos del grafo, los enlaces entre los nodos y el estilo de los nodos y enlaces.
- MermaidJs se encarga de realizar el render del grafo en texto a un grafo gráfico en el explorador.
- Además se utiliza la utilidad de línea de comandos para transformar el grafo en una imagen png.
Se mantiene una copia de un template de grafo de mermaid de un proceso,se lee y se modifica el estilo según corresponda.
