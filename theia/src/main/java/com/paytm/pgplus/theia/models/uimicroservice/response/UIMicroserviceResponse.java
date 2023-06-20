package com.paytm.pgplus.theia.models.uimicroservice.response;

public class UIMicroserviceResponse {

    private String htmlPage;

    public UIMicroserviceResponse() {
    }

    public UIMicroserviceResponse(String htmlPage) {
        this.htmlPage = htmlPage;
    }

    public String getHtmlPage() {
        return htmlPage;
    }

    public void setHtmlPage(String htmlPage) {
        this.htmlPage = htmlPage;
    }

}
