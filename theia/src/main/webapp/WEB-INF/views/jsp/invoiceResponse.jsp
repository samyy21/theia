<%@ page import="java.util.*" %>
<%@ page import="org.slf4j.Logger" %>
<%@ page import="org.slf4j.LoggerFactory" %>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8" session="false"%>
    <%@ taglib prefix="ptm" uri="PaytmCustomTags" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Paytm Payments</title>
<style type="text/css">
        table.tablesorter1 {
    background-color: #CDCDCD;
    border: 1px solid #C3C3C3;
    font-family: 'Open Sans',Arial,sans-serif;
    font-size: 8pt;
    margin: 10px 0 15px;
    min-width: 500px;
}
table {
    border-collapse: separate;
    border-spacing: 0;
    color: inherit;
    font: inherit;
}


table.tablesorter1 thead tr th {
    background-color: #00C0F2;
    border: 1px solid #FFFFFF;
    color: #FFFFFF;
    font-size: 8pt;
    padding: 4px;
}


html, body, div, span, applet, object, iframe, h1, h2, h3, h4, h5, h6, p, blockquote, pre, a, acronym, address, big, cite, code, del, dfn, em, font, img, ins, kbd, q, s, samp, small, strike, strong, sub, sup, tt, var, dl, dt, dd, ol, ul, li, fieldset, form, label, legend, table, caption, tbody, tfoot, thead, tr, th, td {
    border: 0 none;
    font-family: 'Open Sans',Arial,sans-serif;
    font-size: 100%;
    font-style: inherit;
    font-weight: inherit;
    margin: 0;
    outline: 0 none;
    padding: 0;
    vertical-align: middle;
}


table.tablesorter tbody td, table.tablesorter1 tbody td {
    background-color: #FFFFFF;
    color: #3D3D3D;
    padding: 4px;
    vertical-align: top;
    word-break: break-all;
}

</style>
</head>
<body style="margin-top: 10px">
        <div id="logo" style="float:left; margin-left: 20px">
                <a href="http://www.paytm.com" target="_blank">
                        <img src="${ptm:stResPath()}images/web/paytm/paytm-logo.png" alt="" />
                </a>
        </div>
        <br /><br />
        <c:set var="orderId" value='<%=request.getParameter("ORDERID") %>' />
        <c:set var="status" value='<%=request.getParameter("STATUS") %>' />

        <c:choose>
           <c:when test="${!empty orderId && !empty status && 'TXN_SUCCESS' eq status}">
        <h3>Your payment has been processed successfully. Please note the order id: ${orderId} for your reference.</h3>      
           </c:when>
           <c:otherwise>
           <h3>Your transaction failed due to some reason. Please try after some time. Please note the order id: ${orderId} for your reference.</h3>
           </c:otherwise>
        </c:choose>
        
        <div style="float: left; width: 100%;">
                <center><h3>Response</h3></center>
                <center>
                        <table border="1px; text-align: center;" class="tablesorter1"">
                        <thead>
    
     <tr>
                                <th>Parameter Name</th>
                                <th>Parameter Value</th>
                        </tr>
                        </thead>
                        <%
                                //Enumeration<String> parameters = request.getParameterNames();
                                //TreeSet<String> paramSet = (TreeSet<String>)request.getAttribute("paramSet");
                                try {
                                Boolean validCheckSum = (Boolean)request.getAttribute("validCheckSum");
                                for (Map.Entry<String, String[]> entry: request.getParameterMap().entrySet()) {
                                	if(!"CHECKSUMHASH".equals(entry.getKey())){
                                        String paramValue = request.getParameter(entry.getKey());
                        %>      
                                        <tr>
                                                <td><%=entry.getKey()%></td>
                                                <td><%=paramValue %></td>
                                        </tr>
        
                        <% }     }%>
                        </table>
                </center>
        </div>
        <script>
                <%
                        String status = request.getParameter("STATUS");
                %>
                if(window.top !== window){
                        
                        if(window.top.Paytm)
                                window.top.Paytm.payment_response = "<% System.out.print(status + "lllll"); } catch(Exception e){Logger LOGGER = LoggerFactory.getLogger("InvoiceResponse.jsp"); LOGGER.error("Exception occurred : {} ",e);} %>";
                }
        </script>;
</body>
</html>
    