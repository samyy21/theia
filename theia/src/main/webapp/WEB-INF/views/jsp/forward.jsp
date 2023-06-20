<%
	String html = (String)session.getAttribute("bankHTML");
	session.invalidate();
%>
<%=html%>