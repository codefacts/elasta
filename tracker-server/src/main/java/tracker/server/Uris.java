package tracker.server;

/**
 * Created by sohan on 7/1/2017.
 */
public interface Uris {
    String loginUri = "/login";
    String logoutUri = "/logout";
    String userUri = "/users";
    String deviceUri = "/devices";
    String positionUri = "/positions";

    static String api(String uriPart) {
        return "/api" + uriPart;
    }

    static String singularUri(String uri) {
        return uri + "/:" + PathParams.id;
    }
}