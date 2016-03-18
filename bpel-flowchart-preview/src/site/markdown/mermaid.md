Creaci&oacute;n de template Mermaid.js
==============================
 
Para asegurar una clasificación correcta se ha definido unas reglas adicionales en la creación de los grafos de mermaid.js
 
El fichero se debe estructurar en 4 partes:

1.	Definición de nodos
2.	Creación de enlaces
3.	Configuración de estilos de nodos
4.	Configuración de estilos de enlaces

Definici&oacute;n de Nodos
-------------------

Lo primero que se hace es definir todos sus nodos. Cada nodo tiene 4 propiedades; un tipo, un identificador de nodo, una forma geométrica y un mensaje.

- Se definen 6 tipos de nodos. \'ht\' (human task), \'ws\' (web service), \'bpel\' (proceso bpel), \'init\' (actividad de inicio), \'fn\' (actividad de fin), \'obj\' (cualquier otra actividad tipo condicional, flujo paralelo, o para notas informativas). Todos los identificadores de nodos deben comenzar su identificador con alguno de estos tipos.
- Los identificadores de nodo son una copia del nombre de la actividad en el proceso BPEL. Es de esta forma que se puede realizar una relación entre el grafo y el BPEL. 
- Hay varias formas geométricas que se definen utilizando caracteres diferentes en mermaid.js. Se pueden definir, círculos, rectángulos, rombos, rectángulos redondeados, y objetos tipo notas informativas.
- El mensaje es el label que se mostrará en el grafo. (En vez de mostrar el identificador del nodo)

La definición de un nodo se hace de la siguiente forma:


<b><font color="red">bpel</font>_<font color="green">Receive_CrearTarjeta</font>[<font color="blue">Crear Tarjeta</font>]</b>

-	<font color="red">bpel</font> : Tipo de objeto.
-	<font color="green">Receive_CrearTarjeta</font> : Identificador asociado al proceso BPEL
-	<font color="blue">Crear Tarjeta</font> : Label que aparecerá en el grafo
-	\[  \] (los corchetes): Representan la forma del nodo, en este caso un cuadrado.


<br/>

<table>
    <tr>
        <td>Identificador de Nodo</td>
        <td>Descripción</td>
        <td>Imagen</td>
    </tr>
    <tr>
        <td>init_receiveInput((Alta Agente))</td>
        <td>
            <ul>
                <li>Tipo: Nodo de inicio</li>
                <li>Id: receiveInput</li>
                <li>Label: Alta Agente</li>
                <li>Forma: Circulo (dobles paréntesis)</li>
            </ul>
        </td>
        <td><img src="/images/init_node.png"/></td>
    </tr>
    <tr>
        <td>ht_ValidacionDLF(Validacion DLF)</td>
        <td>
            <ul>
                <li>Tipo: Nodo tarea humana</li>
                <li>Id: ValidacionDLF</li>
                <li>Label: Validación DLF</li>
                <li>Forma: Rectángulo redondeado  (paréntesis)</li>
             </ul>
        </td>
        <td><img src="/images/ht_node.png"/></td>
    </tr>
    <tr>
        <td>obj_SwitchContinuar{Continuar?}</td>
        <td>
            <ul>
                <li>Tipo: Nodo de objeto</li>
                <li>Id: SwitchContinuar</li>
                <li>Label: Continuar?</li>
                <li>Forma: Rombo (Llaves)</li>
             </ul>
        </td>
        <td><img src="/images/cond_node.png"/></td>
    </tr>
    <tr>
        <td>ws_GenerarContrato[Generar Contrato]</td>
        <td>
            <ul>
                <li>Tipo: Nodo de web Service</li>
                <li>Id: GenerararContrato</li>
                <li>Label: Generar Contrato</li>
                <li>Forma: Rectángulo (corchetes)</li>
             </ul>
        </td>
        <td><img src="/images/ws_node.png"/></td>
    </tr>
    <tr>
        <td>bpel_InvokeCrearTarjeta[Crear Tarjeta]</td>
        <td>
            <ul>
                <li>Tipo: Nodo de BPEL</li>
                <li>Id: InvokeCrearTarjeta</li>
                <li>Label: Crear tarjeta</li>
                <li>Forma: Rectángulo (corchetes)</li>
             </ul>
        </td>
        <td><img src="/images/bpel_node.png"/></td>
    </tr>
    <tr>
        <td>obj_TarjetaTerminada>Tarjeta terminada]</td>
        <td>
            <ul>
                <li>Tipo: Nodo de objeto</li>
                <li>Id: TarjetaTerminada</li>
                <li>Label: Tarjeta terminada</li>
                <li>Forma: Nota (mayor que y corchete)</li>
             </ul>
        </td>
        <td><img src="/images/info_node.png"/></td>
    </tr>
    <tr>
        <td>fn_callbackClient((Fin))</td>
        <td>
            <ul>
                <li>Tipo: Nodo de fin</li>
                <li>Id: callbackClient</li>
                <li>Label: Fin</li>
                <li>Forma: Circulo (dobles paréntesis)</li>
             </ul>
        </td>
        <td><img src="/images/fn_node.png"/></td>
    </tr>
</table>

Creaci&oacute;n de enlaces
-------------------

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
Configuraci&oacute;n de estilos de nodos
---------------------------------
Aunque se puede modificar, se recomienda simplemente que se copie la sección de estilos desde la plantilla de ejemplo que está en la aplicación.

    classDef ht fill:#A8CB6A,stroke:#333;
    classDef ws fill:#6EABD0,stroke:#333;
    classDef obj fill:#D4D4D4,stroke:#333;
    classDef obj_info fill:#FFCE44,stroke:#333;
    classDef htError fill:#A8CB6A,stroke:#FF0000,stroke-width:3px;
    classDef wsError fill:#6EABD0,stroke:#FF0000,stroke-width:3px;

Configuración de estilos de enlaces
-----------------------------------
La última línea del fichero debe ser la del estilo de los enlaces. Se debe copiar esta línea desde la plantilla de ejemplo que está en la aplicación.

    linkStyle 0 stroke-width:2px,fill:none,stroke:green,stroke-dasharray: 5, 5;





