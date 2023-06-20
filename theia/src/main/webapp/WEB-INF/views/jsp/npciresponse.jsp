<%@ page session="false" language="java" contentType="text/html; charset=ISO-8859-1"
  pageEncoding="ISO-8859-1"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!DOCTYPE html>
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
        <div id="logo" style="float:right; margin-right: 20px">
            <img src="resources/images/paytm_logo.png" alt="" />
        </div>
        <div style="float: left; width: 100%;">
                <center><h3>Response</h3></center>
                <center>
            <table border="1px; text-align: center;" class="tablesorter1">
                <thead>
                    <tr>
                        <th>Parameter Name</th>
                        <th>Parameter Value</th>
                    </tr>
                </thead>

                <c:if test="${not empty processedResponse.subsId}">
                <tr>
                    <td>SUBS_ID</td>
                    <td>${processedResponse.subsId}</td>
                </tr>
                </c:if>

                <c:if test="${(not empty processedResponse.isAccepted)}">
                <tr>
                   <td>IS_ACCEPTED</td>
                   <td>${processedResponse.isAccepted}</td>
                </tr>
                </c:if>

                <c:if test="${(not empty processedResponse.isAccepted) && (processedResponse.isAccepted == 'true')}">
                <tr>
                   <td>ACCEPTED_REF_NO</td>
                   <td>${processedResponse.acceptedRefNo}</td>
                </tr>
                </c:if>

                <c:if test="${(not empty processedResponse.isAccepted) && (processedResponse.isAccepted == 'false')}">
                <tr>
                   <td>REJECTED_BY</td>
                   <td>${processedResponse.rejectedBy}</td>
                </tr>
                </c:if>

                <c:if test="${(not empty processedResponse.transactionStatus) && (processedResponse.transactionStatus == 'SUCCESS')}">
                <tr>
                   <td>STATUS</td>
                   <td>${processedResponse.transactionStatus}</td>
                </tr>
                </c:if>


                <c:if test="${(not empty processedResponse.transactionStatus) && (processedResponse.transactionStatus == 'FAILURE')}">
                <tr>
                   <td>STATUS</td>
                   <td>${processedResponse.transactionStatus}</td>
                </tr>

                <tr>
                   <td>RESPCODE</td>
                   <td>${processedResponse.responseCode}</td>
                </tr>

                <tr>
                   <td>RESPMSG</td>
                   <td>${processedResponse.responseMsg}</td>
                </tr>
                </c:if>

                <c:if test="${not empty processedResponse.merchantCustId}">
                <tr>
                    <td>MERCHANT_CUST_ID</td>
                    <td>${processedResponse.merchantCustId}</td>
                </tr>
                </c:if>

                <c:if test="${not empty processedResponse.orderId}">
                <tr>
                   <td>ORDERID</td>
                   <td>${processedResponse.orderId}</td>
                </tr>
                </c:if>

                <c:if test="${not empty processedResponse.mid}">
                <tr>
                   <td>MID</td>
                   <td>${processedResponse.mid}</td>
                </tr>
                </c:if>

                <c:if test="${not empty processedResponse.txnDate}">
                <tr>
                   <td>TXNDATE</td>
                   <td>${processedResponse.txnDate}</td>
                </tr>
                </c:if>

                <c:if test="${not empty processedResponse.paymentMode}">
                <tr>
                   <td>PAYMENTMODE</td>
                   <td>${processedResponse.paymentMode}</td>
                </tr>
                </c:if>

                <c:if test="${not empty processedResponse.mandateType}">
                <tr>
                   <td>MANDATE_TYPE</td>
                   <td>${processedResponse.mandateType}</td>
                </tr>
                </c:if>

            </table>
    </body>
</html>