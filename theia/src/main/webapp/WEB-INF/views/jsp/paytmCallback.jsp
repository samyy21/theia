<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<%@ page import="java.util.*,java.net.URLEncoder"%>
<%@ page session="false" %>
<%

        StringBuilder json = new StringBuilder();
        Enumeration<String> paramNames = request.getParameterNames();
        
        Map<String, String[]> mapData = request.getParameterMap();

        
        json.append("{"); 
        
        int i = 0;

        while(paramNames.hasMoreElements()) {
        
            String paramName = (String)paramNames.nextElement();

            if(paramName.equals("ORDER_ID")){
                continue;
            }else{
                if(i == 0){
                      json.append("\""+paramName.toString()+"\":\""+mapData.get(paramName)[0].toString()+"\""); 
                }else{
                      json.append(", \""+paramName.toString()+"\":\""+mapData.get(paramName)[0].toString()+"\"");
                }
            }
            i++;
        }
        json.append("}");

        StringBuilder outputHtml = new StringBuilder();
        outputHtml.append("<html>");
        outputHtml.append("<head>");
        outputHtml.append("<meta http-equiv='Content-Type' content='text/html;charset=ISO-8859-I'>");
        outputHtml.append("<title>Paytm</title>");
        outputHtml.append("<script type='text/javascript'>");
        outputHtml.append("function response(){");
        outputHtml.append("return document.getElementById('response').value;");
        outputHtml.append("}");
        outputHtml.append("</script>");
        outputHtml.append("</head>");
        outputHtml.append("<body>");
        outputHtml.append("Redirect back to the app<br>");
        outputHtml.append("<form name='frm' method='post'>");
        outputHtml.append("<input type='hidden' id='response' name='responseField' value='" + json.toString()  + "' />");
        outputHtml.append("</form>");
        outputHtml.append("</body>");
        outputHtml.append("</html>");
        out.println(outputHtml);
%>