package vswe.stevesfactory.components;


import gigabit101.AdvancedSystemManager2.components.Setting;

public class ConditionSettingChecker {
    private gigabit101.AdvancedSystemManager2.components.Setting setting;
    private int amount;

    public ConditionSettingChecker(Setting setting) {
        this.setting = setting;
        amount = 0;
    }

    public void addCount(int n) {
        amount += n;
    }

    public boolean isTrue() {
        return !setting.isLimitedByAmount() || amount >= setting.getAmount();
    }
}
