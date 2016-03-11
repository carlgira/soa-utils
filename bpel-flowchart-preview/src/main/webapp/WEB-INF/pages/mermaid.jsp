<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<html>
<head>
    <!-- <link href="<c:url value="/resources/css/mermaid.css" />" rel="stylesheet"> -->
    <link href="/bpel-flowchart-preview/resources/css/mermaid.css" rel="stylesheet">
    <style>
    	body {
    		background-color: linen;
    	}

    	h1 {
    		color: maroon;
    		margin-left: 40px;
    	}
    </style>
    <!-- <script src="<c:url value="/resources/js/mermaid.min.js" />"></script> -->
    <script src="/bpel-flowchart-preview/resources/js/mermaid.min.js"></script>
    <script>
    		var config = {
                startOnLoad:true,
                flowchart:{
                        useMaxWidth:false,
                        htmlLabels:false
                }
            };
            mermaid.initialize(config);
    </script>
</head>
<body>
	<h1>${process}</h1>
    <b>Estado: </b>${state}
	 <div class="mermaid">
        ${graph}
     </div>
</body>
</html>