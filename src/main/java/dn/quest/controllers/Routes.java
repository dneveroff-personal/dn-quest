package dn.quest.controllers;

public interface Routes {
    // COMMON
    String API = "/api";
    String ATTEMPT = "/attempt";
    String EMAIL = "/{email}";
    String ID = "/{id:\\d+}";
    String LEADERBOARD = "/leaderboard";
    String LEVEL = "/level";
    String LOGIN = "/login";
    String ME = "/me";
    String USER_ID = "/{userId}";
    String USER_NAME = "/{username}";
    String PUBLISHED = "/published";
    String ROLE = "/role";
    String STATUS = "/status";
    String SUBMIT = "/submit";

    // Attempts
    String ATTEMPTS = API + "/attempts";

    // Quest
    String QUESTS = API + "/quests";
    String QUEST_ID = "/{questId}";
    String START = "/start";
    String QUEST_START = QUEST_ID + START;
    String QUEST_LEADERBOARD = QUEST_ID + LEADERBOARD;

    // Registration
    String PING = "/ping";
    String REGISTER = "/register";

    // Session
    String SESSIONS = API + "/sessions";
    String SESSION_ID = "/{sessionId}";
    String SESSION_ATTEMPT = SESSION_ID + ATTEMPT;
    String SESSION_LEVEL = SESSION_ID + LEVEL;
    String SESSION_STATUS = "/sessions" + SESSION_ID + STATUS;

    // Team
    String TEAMS = API + "/teams";
    String TEAM_MEMBERS = ID + "/members";
    String TEAM_MEMBER_ADD = TEAM_MEMBERS + USER_ID;
    String TEAM_TRANSFER_CAPTAIN = ID + "/transfer-captain" + USER_ID;

    // Users
    String USERS = API + "/users";
    String USERS_BY_NAME = "/by-username" + USER_NAME;
    String BY_EMAIL = "/by-email" + EMAIL;
    String ROLE_BY_ID = ID + ROLE;

    // Level
    String LEVELS = API + "/levels";
    String LEVEL_ID = "/{levelId}";
    String LEVELS_BY_QUEST = "/by-quest" + QUEST_ID;
    String LEVELS_REORDER = "/reorder";

    // Level codes
    String LEVEL_CODES = "/level-codes";
    String CODES_BY_LEVEL = "/by-level" + LEVEL_ID;

    // Participations
    String PARTICIPATION = API + "/participation";
    String PARTICIPATION_STATUS = ID + STATUS;
    String PARTICIPATORS = "/by-quest" + QUEST_ID;

}
