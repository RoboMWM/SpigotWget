package tit79a.spigotwget;

import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {
	private static Main instance;

    public Main() {
        instance = this;
    }

	public void onEnable() {
		getDataFolder().mkdir();
		this.getCommand("wget").setExecutor(new WgetCommand());
	}

	public static Main getInstance() {
		return instance;
	}
}
