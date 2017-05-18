package event_based_server;

public class HttpInfo {
    private String httpUrl;
    private String httpMethod;
    private String httpProtocol;

    public HttpInfo(){
        httpUrl = "";
        httpMethod = "";
        httpProtocol = "";
    }

    public HttpInfo(String httpUrl, String httpMethod, String httpProtocol){
        this.httpUrl = httpUrl;
        this.httpMethod = httpMethod;
        this.httpProtocol = httpProtocol;
    }


//    public void setHttpUrl(String target){
//        httpUrl = target;
//    }
//
//    public void setHttpUrl(String target){
//        httpMethod = target;
//    }
//
//    public void setHttpUrl(String target){
//        httpProtocol = target;
//    }
}
