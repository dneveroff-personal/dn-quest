package dn.quest.gameengine.entity.enums;

/**
 * Роли в команде
 */
public enum TeamRole {
    CAPTAIN("Капитан"),
    MEMBER("Участник"),
    DEPUTY("Заместитель");

    private final String displayName;

    TeamRole(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isCaptain() {
        return this == CAPTAIN;
    }

    public boolean canManageTeam() {
        return this == CAPTAIN;
    }

    public boolean canInviteMembers() {
        return this == CAPTAIN;
    }

    public boolean canRemoveMembers() {
        return this == CAPTAIN;
    }
}