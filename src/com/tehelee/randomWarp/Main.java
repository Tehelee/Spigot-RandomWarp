package com.tehelee.randomWarp;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.User;

public class Main extends JavaPlugin
{
	public static FileConfiguration config;
	public static ConsoleCommandSender console;
	public static Server server;
	public static Essentials ess;
	
	private static Main instance;
	private static BukkitScheduler scheduler;
	private static PluginManager pluginManager;
	private static Logger logger;
	
	private static Map<UUID, String> playerLog;
	
	public Main()
	{
		Main.instance = this;
	}
	
	@Override
	public void onEnable()
	{
		initializeConfig();
		
		Main.server = getServer();
		
		Main.console = Main.server.getConsoleSender();
		
		Main.scheduler = Main.server.getScheduler();
		
		Main.pluginManager = Main.server.getPluginManager();
		
		Main.ess = (Essentials)pluginManager.getPlugin("Essentials");
		
		Main.logger = Main.server.getLogger();
		
		Main.playerLog = new HashMap<UUID, String>();
		
		Main.pluginManager.registerEvents(new SignListener(), this);
		
		this.getCommand("RandomWarp").setExecutor(new CmdRandomWarp());
		
		CmdRandomWarp.Message(null, HelpText.logStart, true);
	}
	
	@Override
	public void onDisable()
	{	
		CmdRandomWarp.Message(null, HelpText.logStop, true);
	}
	
	private void initializeConfig()
	{
		Main.config = this.getConfig();
		Main.config.options().header("General Options\n  minimumDistance\n    - Minimum radius for warp\n  maximumDistance\n    - Maximum radius for warp\n  safetyCheckDelay\n    - Tick delay before rechecking if destination is valid\n    - This gives time for the chunks to populate correctly, trees and villages seem to lag behind\n  warpDelay\n    - the amount of seconds until a player can use a warp sign\n\nWarp Effects\n  suppressIgnition\n    - toggle this on to disable igniting players on warp\n  hideLightning\n    - toggle this to supress the lightining strike at the warp destination\n  muteWarpSound\n    - toggle this to disable the warp sounds\n  ignitionTicks\n    - the amount of ticks to ignite a player, must be greater than 5\n\nSign Text\n  warpSignTitle\n    - The text to display on the second line as -> \"[\" + title + \"]\"\n    - Example \"Random Warp\" will be displayed as \"[Random Warp]\"\n  warpSignDescription\n    - This is the optional description text on the third line, displayed in italics\n\nSign Colors (Must be in all CAPS)\n  warpSignMainColor\n    - The color of the title text\n  warpSignAltColor\n    - The color of the title's brackets\n  warpSignDescription\n    - The color of the description");
		Main.config.addDefault("minimumDistance", 1000);
		Main.config.addDefault("maximumDistance", 100000);
		Main.config.addDefault("safetyCheckDelay", 5L);
		Main.config.addDefault("warpDelay", 300);
		Main.config.addDefault("suppressIgnition", false);
		Main.config.addDefault("hideLightning", true);
		Main.config.addDefault("muteWarpSound", false);
		Main.config.addDefault("ignitionTicks", 80);
		Main.config.addDefault("warpSignTitle", "Random Warp");
		Main.config.addDefault("warpSignDescription", "");
		Main.config.addDefault("warpSignMainColor", ChatColor.AQUA.name());
		Main.config.addDefault("warpSignAltColor", ChatColor.BLUE.name());
		Main.config.addDefault("warpSignDescColor", ChatColor.DARK_BLUE.name());
		Main.config.addDefault("warpSignMainBorderColor", ChatColor.DARK_PURPLE.name());
		Main.config.addDefault("warpSignAltBorderColor", ChatColor.BLACK.name());
		
		Main.config.options().copyDefaults(true);
		
		String title = Main.config.getString("warpSignTitle");
		if (title.isEmpty())
		{
			Main.config.set("warpSignTitle", "[Random Warp]");
		}
		
		writeConfig();
	}
	
	public static void reload()
	{
		final Plugin plugin = (Plugin)instance;
		
		plugin.reloadConfig();
		
		Main.config = plugin.getConfig();
		
		CmdRandomWarp.Message(null, HelpText.logRestart, true);
	}
	
	public static void writeConfig()
	{
		if (null != instance)
		{
			instance.saveConfig();
			
			instance.reloadConfig();
			
			Main.config = instance.getConfig();
		}
	}
	
	public static void logWarp(Player player, Location origin)
	{
		logWarp(player, origin, false);
	}
	
	public static void logWarp(Player player, Location origin, boolean forced)
	{
		if (ess != null)
		{
			User user = ess.getUser(player);
			
			user.setLastLocation(origin);
		}
		
		if (forced)
		{
			Main.logger.info(player.getName() + " was forced into a random warp.");
		}
		else
		{
			Main.logger.info(player.getName() + " has used the random warp.");
			
			if (playerLog.containsKey(player.getUniqueId()))
			{
				long past = Long.parseLong(playerLog.get(player.getUniqueId()));
				long now = System.currentTimeMillis() / 1000;
				long difference = now - past;
				long minutes = difference / 60;
				long seconds = difference % 60;
				Main.logger.info("Their last random warp was "+minutes+" minutes and "+seconds+" seconds ago.");
			}
			else
			{
				playerLog.put(player.getUniqueId(), String.valueOf(System.currentTimeMillis() / 1000));
			}
		}
	}
	
	public static long getLastWarp(Player player)
	{
		if (playerLog.containsKey(player.getUniqueId()))
		{
			String playerTime = playerLog.get(player.getUniqueId());
			
			long past = Long.parseLong(playerTime);
			long now = System.currentTimeMillis() / 1000;
			return (now - past) - Main.config.getLong("warpDelay");
		}
		
		return 0;
	}
	
	public static void DelayedTask(Runnable runner, long tickDelay)
	{
		scheduler.scheduleSyncDelayedTask(instance, runner, tickDelay);
	}
}
