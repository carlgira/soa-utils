<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<html>
<head>
</head>
<body>
	<h1>${process}</h1>
    <b>Estado: </b>${state}
	 <img alt="Proceso" src="data:image/png;base64,${graph}" />
</body>
</html>