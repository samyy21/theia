<c:if test="${'1' eq debug}">
	<div class="debug">
		<a href="#" class="pull-down">&darr;</a>
		<pre class="console">
Server info = <%= application.getServerInfo() %>
Servlet engine version = <%=  application.getMajorVersion() %>.<%= application.getMinorVersion() %>
Java version = <%= System.getProperty("java.vm.version") %>

Request params
<c:forEach var='parameter' items='${paramValues}'><c:set var="paramValue"><c:forEach var='value' items='${parameter.value}'>${value} </c:forEach></c:set><c:out value="${parameter.key} = ${paramValue}" />
</c:forEach>
Request errors
<c:forEach var="error" items="${requestScope.validationErrors}">
	${error}
</c:forEach>
Headers   
<c:forEach var="hdr" items="${header}">                           
${hdr.key} => ${hdr.value}
</c:forEach>
Session:
<c:forEach var="name" items="${pageContext.session.attributeNames}">
	Name:  ${name}
	Value: ${sessionScope[name]}
</c:forEach>

Configured channels: ${requestScope.configuredChannels}
Filtered channels: ${requestScope.filteredConfiguredChannels}
		</pre>
	</div>
	<script type="text/javascript">
	// Debug
	$(document).ready(function() {
		$('.debug .pull-down').live('click', function() {
			console.log('Clicked: ' + $('.debug .console'));
			$('.debug .console').slideToggle();
			return false;
		});
	});
	</script>
</c:if>
