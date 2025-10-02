package dn.quest.controllers;

public interface Routes {
    // COMMON
    String API = "/api";
    String ATTEMPT = "/attempt";
    String CODE = "/code";
    String CURRENT = "/current";
    String EMAIL = "/{email}";
    String HINTS = "/hints";
    String ID = "/{id:\\d+}";
    String LAST = "/last";
    String LEADERBOARD = "/leaderboard";
    String LEVELS = "/levels";
    String LEVEL = "/level";
    String LOGIN = "/login";
    String ME = "/me";
    String USER_ID = "/{userId}";
    String USER_NAME = "/{username}";
    String PUBLISHED = "/published";
    String REORDER = "/reorder";
    String ROLE = "/role";
    String SEARCH = "/search";
    String STATUS = "/status";
    String SUBMIT = "/submit";

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
    String SESSION_CURRENT = SESSION_ID + CURRENT;
    String SESSION_CODE = SESSION_ID + CODE;
    String SESSION_STATUS = "/sessions" + SESSION_ID + STATUS;
    String LAST_ATTEMPTS = SESSION_ID + "/last-attempts";

    // Team
    String TEAMS = API + "/teams";
    String TEAMS_ID = "/{teamId}";
    String TEAM_MEMBERS = ID + "/members";
    String TEAM_MEMBER_ADD = TEAM_MEMBERS + USER_ID;
    String TEAM_TRANSFER_CAPTAIN = ID + "/transfer-captain" + USER_ID;
    String TEAM_INVITE_USER = TEAMS_ID + "/invite" + USER_NAME;
    String TEAM_INVITE_RESPONSE = "/invitations" + ID + "/{action}";

    // Users
    String USERS = API + "/users";
    String USERS_BY_NAME = "/by-username" + USER_NAME;
    String BY_EMAIL = "/by-email" + EMAIL;
    String ROLE_BY_ID = ID + ROLE;
    String MY_INVITATIONS = ME + "/invitations";

    // Level
    String API_LEVELS = API + LEVELS;
    String LEVEL_ID = "/{levelId}";
    String LEVELS_BY_QUEST = "/by-quest" + QUEST_ID;
    String LEVELS_REORDER = "/reorder";

    // Level codes
    String LEVEL_CODES = API + "/codes";
    String CODES_BY_LEVEL = "/by-level" + LEVEL_ID;

    // Hints
    String API_HINTS = API + LEVELS + LEVEL_ID + HINTS;

    // Participations
    String PARTICIPATION = API + "/participation";
    String PARTICIPATION_STATUS = ID + STATUS;
    String PARTICIPATORS = "/by-quest" + QUEST_ID;

    // Attempts
    String ATTEMPTS = API + "/attempts";
    String ATTEMPTS_BY_LEVEL = LEVEL + LEVEL_ID;
    String LAST_ATTEMPTS_BY_LEVEL = LEVEL + LEVEL_ID + LAST;
}
