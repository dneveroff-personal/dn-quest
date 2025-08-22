package dn.quest.controllers;

public interface Routes {
    // COMMON
    String ID = "/{id:\\d+}";
    String API = "/api";
    String ATTEMPT = "/attempt";
    String LEVEL = "/level";
    String USER_ID = "/{userId}";

    // Delete Controller
    String DELETE = API + "/delete";

    // Get Controller
    String GET = API + "/get";
    String ALL = "/all";
    String GET_COMPLETED = "/completed";

    // Registration controller
    String PING = "/ping";
    String REGISTER = "/register";

    // Post Controller
    String POST = API + "/post";
    String POST_NEW = "/new";
    String POST_SOLVE = ID + "/solve";

    // Session
    String SESSIONS = API + "/sessions";
    String SESSION_ID = "/{sessionId}";
    String SESSION_ATTEMPT = SESSION_ID + ATTEMPT;
    String SESSION_LEVEL = SESSION_ID + LEVEL;

    // Quest
    String QUESTS = API + "/quests";
    String QUEST_ID = "/{questId}";
    String START = "/start";
    String QUEST_START = QUEST_ID + START;

    // Team
    String TEAMS = API + "/teams";
    String TEAM_MEMBERS = ID + "/members";
    String TEAM_MEMBER_ADD = TEAM_MEMBERS + USER_ID;
    String TEAM_TRANSFER_CAPTAIN = ID + "/transfer-captain" + USER_ID;

}
