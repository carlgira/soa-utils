Build a Graph template 
======================

The construction of the template must follow the next rules so the parser can read the template.
 
The file must be structured in the next 4 parts

1.	Node Definition
2.	Link Creation
3.	Node Style
4.	Link Style

Node Definition
---------------

The first thing to do is to define all the nodes of the graph. Each node has 4 properties; a type, an node id, a shape and a message.

- There are 6 node **types**.  \'ht\' (human task), \'ws\' (web service), \'bpel\' (bpel process), \'init\' (init activity), \'fn\' (end activity), \'obj\' (any other object a conditional, flow object, or informative notes). Every node id must begin with his type.
- The **node id** \"have\" to be a exact copy of the activity name in the bpel process. This is the way that the app is able to relates the graph nodes and the activities inside of the bpel.
- There are several shapes in mermaisJS, each one using a combination of characters. It can be used circles,  rects, round rects, diamonds and informative nodes.
- The label is the message that is going to appear in the graph.

<b><font color="red">init</font>_<font color="green">receiveInput</font>((<font color="blue">Begin</font>))</b>

-	<font color="red">init</font> : Type of object
-	<font color="green">receiveInput</font> : Id of the activity in the bpel process
-	<font color="blue">Crear Tarjeta</font> : Label that is going to appear in the graph
-	**((  ))** (the double parenthesis): The shape of the node, in this case a circle


<br/>

<table>
    <tr>
        <td>Node Id</td>
        <td>Description</td>
        <td>Image</td>
    </tr>
    <tr>
        <td>init_receiveInput((Begin))</td>
        <td>
            <ul>
                <li>Type: Init node</li>
                <li>Id: receiveInput</li>
                <li>Label: Begin</li>
                <li>Shape: Circle (double parenthesis)</li>
            </ul>
        </td>
        <td><img src="/images/init_node.png"/></td>
    </tr>
    <tr>
        <td>ht_receiveCompletedTask_Validate(Validate Task)</td>
        <td>
            <ul>
                <li>Type: Human task node</li>
                <li>Id: receiveCompletedTask_Validate</li>
                <li>Label: Validate Task</li>
                <li>Shape: Round rect  (parenthesis)</li>
             </ul>
        </td>
        <td><img src="/images/ht_node.png"/></td>
    </tr>
    <tr>
        <td>obj_TaskSwitch{"Validate?"}</td>
        <td>
            <ul>
                <li>Type: Object node</li>
                <li>Id: TaskSwitch</li>
                <li>Label: Validate?</li>
                <li>Shape: Diamond (Llaves)</li>
             </ul>
        </td>
        <td><img src="/images/cond_node.png"/></td>
    </tr>
    <tr>
        <td>ws_GenerarContrato[Generar Contrato]</td>
        <td>
            <ul>
                <li>Type: Nodo de web Service</li>
                <li>Id: GenerararContrato</li>
                <li>Label: Generar Contrato</li>
                <li>Shape: Rectángulo (corchetes)</li>
             </ul>
        </td>
        <td><img src="/images/ws_node.png"/></td>
    </tr>
    <tr>
        <td>bpel_InvokeCrearTarjeta[Crear Tarjeta]</td>
        <td>
            <ul>
                <li>Type: Nodo de BPEL</li>
                <li>Id: InvokeCrearTarjeta</li>
                <li>Label: Crear tarjeta</li>
                <li>Shape: Rectángulo (corchetes)</li>
             </ul>
        </td>
        <td><img src="/images/bpel_node.png"/></td>
    </tr>
    <tr>
        <td>obj_TarjetaTerminada>Tarjeta terminada]</td>
        <td>
            <ul>
                <li>Type: Nodo de objeto</li>
                <li>Id: TarjetaTerminada</li>
                <li>Label: Tarjeta terminada</li>
                <li>Shape: Nota (mayor que y corchete)</li>
             </ul>
        </td>
        <td><img src="/images/info_node.png"/></td>
    </tr>
    <tr>
        <td>fn_callbackClient((Fin))</td>
        <td>
            <ul>
                <li>Type: Nodo de fin</li>
                <li>Id: callbackClient</li>
                <li>Label: Fin</li>
                <li>Shape: Circulo (dobles paréntesis)</li>
             </ul>
        </td>
        <td><img src="/images/fn_node.png"/></td>
    </tr>
</table>

Link Creation
-------------

La creación de enlaces se puede hacer de dos formas; enlaces simples y enlaces con mensaje.

- Los enlaces simples se construyen solo uniendo dos identificadores de nodos utilizando “-->”.


Ejemplo:

<table>
    <tr>
        <td>ws_GenerarContrato --> ws_DatosLDAP</td>
        <td><img src="/images/simple_link.png"/></td>
    </tr>
</table>
<br/>
- Además se pueden utilizar enlaces con mensajes estáticos, para indicar alguna información adicional, tipo objetos condicionales para informar sobre la opción escogida. Los mensajes se definen entre pipes “|”


Ejemplo:

<table>
    <tr>
        <td>obj_SwitchContinuar-->|RECHAZAR|fn_callbackClientReject<br/>obj_ SwitchContinuar-->|ACEPTAR|ws_GenerarContrato</td>
        <td><img src="/images/cond_link.png"/></td>
    </tr>
</table>
<br/>
Node Style
----------
Aunque se puede modificar, se recomienda simplemente que se copie la sección de estilos desde la plantilla de ejemplo que está en la aplicación.

    classDef ht fill:#A8CB6A,stroke:#333;
    classDef ws fill:#6EABD0,stroke:#333;
    classDef obj fill:#D4D4D4,stroke:#333;
    classDef obj_info fill:#FFCE44,stroke:#333;
    classDef htError fill:#A8CB6A,stroke:#FF0000,stroke-width:3px;
    classDef wsError fill:#6EABD0,stroke:#FF0000,stroke-width:3px;

Link Style
----------
La última línea del fichero debe ser la del estilo de los enlaces. Se debe copiar esta línea desde la plantilla de ejemplo que está en la aplicación.

    linkStyle 0 stroke-width:2px,fill:none,stroke:green,stroke-dasharray: 5, 5;





