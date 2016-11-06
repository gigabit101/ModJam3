package vswe.stevesfactory.blocks;


import java.util.UUID;

public class UserPermission {
    private UUID userId;
    private String name;
    private boolean op;
    private boolean active;

    public UserPermission(UUID userId, String name) {
        this.userId = userId;
        if (name == null) {
            this.name = "Unknown";
        } else{
            this.name = name;
        }
    }

    public UUID getUserId() {
        return userId;
    }

    public String getUserName() {
        return name;
    }

    public boolean isOp() {
        return op;
    }

    public void setOp(boolean op) {
        this.op = op;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public gigabit101.AdvancedSystemManager2.blocks.UserPermission copy() {
        gigabit101.AdvancedSystemManager2.blocks.UserPermission temp = new gigabit101.AdvancedSystemManager2.blocks.UserPermission(getUserId(), getUserName());
        temp.setOp(isOp());
        temp.setActive(isActive());
        return temp;
    }
}
