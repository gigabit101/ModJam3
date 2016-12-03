package vswe.stevesfactory.proxy;

import vswe.stevesfactory.init.ModelHelper;
import vswe.stevesfactory.settings.Settings;

public class ClientProxy extends CommonProxy
{
    @Override
    public void preInit()
    {
        Settings.load();
        //Load models
        ModelHelper.init();
    }
}
