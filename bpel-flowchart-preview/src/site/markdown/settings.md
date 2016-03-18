Instalaci&oacute;n y Configuraci&oacute;n
=========================================

Se deben seguir los siguientes pasos para poder realizar la instalación correcta del componente.


1 Crear en una carpeta del servidor un directorio donde se vayan a dejar las templates de los grafos mermaid.js. Crear además un directorio \<temp\> que se utilizará para guardar algunos ficheros temporalmente.

- \<template_dir\>
- \<template_di\>/Template1.txt
- \<template_di\>/Template2.txt
- \<template_di\>/temp

2 Realizar una copia del fichero de propiedades y dejarlo en el directorio creado previamente. bpel-flowchart-preview\src\test\resources\bpel-flowchart-preview.properties. Modificar las variables según corresponda con:

-	**sever:** Url de conexión de servidor SOA.
-	**user:** Usuario para conectarse a servidor Weblogic
-	**password:** Password de usuario para conectarse a weblogic
-	**realm:** Por defecto jazn.com
-	**graph_dir:** \<template_di\>
-	**node_command:** Comando de utilidad de mermaid.js para generar una imagen png del grafo. (ver detalles en sección de utilidad de línea de comandos de mermaid)

3 Añadir al arranque del servidor la propiedad “bpel-flowchart-preview“ con la ruta del fichero de propiedades

-	**-Dbpel-flowchart-preview**=Y:\AdminServerShared\config\bpel-flowchart-preview.properties

4	Reiniciar el servidor para que coja la propiedad del arranque.
5	Desplegar la aplicación en weblogic.
6	Dejar todos los ficheros de template dentro del directorio \<template_di\>


Utilidad de línea de comandos Mermaid.js (opcional)
---------------------------------------------------

Se describen dos formas para instalar la utilidad de línea de comandos de mermaid.js. En una se debe realizar la instalación de aplicaciones y añadir los binarios al PATH, el otro modo de instalación es “portable” y no necesita realizar la instalación de ninguna aplicación.

La utilidad de mermaid viene distribuida con npm y requiere la instalación de la dependencia de otra utilidad llamada phatmtomjs.

- Instalar nodejs. (Ir a página oficial de nodejs) 
- Instalar phatmtomjs

    npm install <font>-</font>g phantomjs@1.9.0-6
    
-	Instalar mermaidjs. 

    npm install <font>-</font>g mermaid


En OMIE por temas de seguridad se utiliza el modo portable.

Como usar utilidad de línea de comandos de Mermaid.js
-----------------------------------------------------

La utilidad de línea de comandos de mermaid.js se encarga de construir una imagen de un grafo desde un fichero de texto. Según si se ha instalado de  forma “regular”  o “portable” se puede ejecutar de una forma u otra.

En modo instalación “regular” los binarios están en el PATH y se pueden ejecutar directamente. Solo es ejecutar el comando de mermaid y dando como entrada el fichero del grafo en texto.

    mermaid graph.mmd
