package ninja.oscaz.autowiki;

import ninja.oscaz.autowiki.command.WikiCommand;
import org.bukkit.plugin.java.JavaPlugin;

public class AutoWiki extends JavaPlugin {

    @Override
    public void onEnable() {
        this.getCommand("wiki").setExecutor(new WikiCommand());
    }

}
