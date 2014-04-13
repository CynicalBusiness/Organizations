package me.capit.Organizations;

import java.util.logging.Logger;

import org.bukkit.plugin.java.JavaPlugin;

public class Organizations extends JavaPlugin {
	public static final String head = "&8&l||==============|[&eOrganizations&8&l]|==============||&r";
	public static final String tag = "&8&l[&eOrgs&8&l]&7 ";
	public static String version = "";
	public static Logger logger;
	
	public ProtectionHandler protEventHandler = null;
	
	@Override
	public void onEnable(){
		protEventHandler = new ProtectionHandler(this);
		
		CommandHandler cmdh = new CommandHandler(this, protEventHandler);
		getServer().getPluginManager().registerEvents(protEventHandler, this);
		getCommand("organization").setExecutor(cmdh);
		
		version = this.getDescription().getVersion();
		saveDefaultConfig();
		protEventHandler.loadBlockDataFromDisk();
		
		this.getConfig().addDefaults(this.getConfig());
		
		logger = getLogger();
		getLogger().info("Enabled WoC.Organizations.");
	}
	
	@Override
	public void onDisable(){
		protEventHandler.saveBlockDataToDisk();
		saveConfig();
		getLogger().info("Disabled WoC.Organizations");
	}
	
	public ProtectionHandler getProtHandler(){
		return protEventHandler;
	}
}
