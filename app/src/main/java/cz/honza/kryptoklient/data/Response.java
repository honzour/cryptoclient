package cz.honza.kryptoklient.data;


public class Response {
    public Throwable errorFromExchangeRequest;
    public long created;

    public Response(Throwable errorFromExchangeRequest) {
        this.errorFromExchangeRequest = errorFromExchangeRequest;
        created = System.currentTimeMillis();
    }

    public boolean isFresh() {
        return System.currentTimeMillis() - created < 150000;
    }

    public boolean isValid() {
        return errorFromExchangeRequest == null;
    }

    public String getError() {
        if (errorFromExchangeRequest != null) {
            return errorFromExchangeRequest.getMessage();
        } else {
            return "";
        }
    }
}
