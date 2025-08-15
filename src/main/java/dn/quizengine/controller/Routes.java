package dn.quizengine.controller;

public interface Routes {
    // Placeholders
    String ID = "/{id:\\d+}";
    String API = "/api";

    // Delete Controller
    String DELETE = API + "/delete";

    // Get Controller
    String GET = API + "/get";
    String GET_ALL = "/all";
    String GET_COMPLETED = "/completed";

    // Registration controller
    String PING = "/ping";
    String REGISTER = "/register";

    // Post Controller
    String POST = API + "/post";
    String POST_NEW = "/new";
    String POST_SOLVE = ID + "/solve";

}
