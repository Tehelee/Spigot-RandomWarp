package com.tehelee.randomWarp;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CmdRandomWarp implements CommandExecutor
{
	private enum ValidCommand
	{
		silent,
		warp,
		force,
		range,
		min,
		max,
		toggle,
		duration,
		edit,
		color,
		colors,
		permissions,
		reload;
		
		public String toString()
		{
	        switch(this)
	        {
		        case silent:		return "silent";
		        case warp:			return "warp";
		        case force:			return "force";
		        case range:			return "range";
		        case min:			return "min";
		        case max:			return "max";
		        case toggle:		return "toggle";
		        case duration:		return "duration";
		        case edit:			return "edit";
		        case color:			return "color";
		        case colors:		return "colors";
		        case permissions:	return "permissions";
		        case reload:		return "reload";
	        }
	        return null;
	    }

		public static ValidCommand getValue(String value)
	    {
	    	String input = value.toLowerCase();
	    	
	    	ValidCommand[] commands = ValidCommand.values();
	    	
	    	for (int i = 0; i < commands.length; i++)
	    	{
	    		if (input.equals(commands[i].toString())) return commands[i];
	    	}
	    	
            return null;
	    }
	};
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
	{	
		
		if ((args.length < 1) || isHelpText(args[0]))
		{
			DisplayAvailableCommands(sender);
		}
		else
		{
			ValidCommand command = ValidCommand.getValue(args[0]);
			if (command == null)
			{
				DisplayAvailableCommands(sender);
			}
			else
			{
				boolean isPlayer = (sender instanceof Player); 
				
				switch(command)
				{
					case silent:
						if (!isPlayer) return false;
						return silent((Player)sender, ((args.length > 1) && isHelpText(args[1])));
						
					case warp:
						if (!isPlayer) return false;
						return warp((Player)sender, ((args.length > 1) && isHelpText(args[1])));
						
					case force:
						return force(sender, args);
						
					case range:
					case min:
					case max:
						return range(sender, command, args);
						
					case toggle:
						return toggle(sender, args);

					case duration:
						return duration(sender, args);
						
					case edit:
						return edit(sender, args);
						
					case colors:
						Message(sender, "Available Colors:\n"+ValidColor.getList(isPlayer), true);
						break;

					case color:
						return color(sender, args);
						
					case permissions:
						return permissions(sender);
						
					case reload:
						return reload(sender);
				}
			}
		}
		
		return true;
	}
	
	////	////	////	////	////
	
	private boolean silent(Player player, boolean showHelp)
	{
		if (!player.hasPermission("permissions.randomwarp.silent")) return false;
		
		if (showHelp)
		{
			Message(player, HelpText.cmdSilent);
		}
		else
		{
			RandWarp.WarpPlayer(player,  true);
		}
		
		return true;
	}
	
	private boolean warp(Player player, boolean showHelp)
	{
		if (!player.hasPermission("permissions.randomwarp.remoteuse")) return false;
		
		if (showHelp)
		{
			Message(player, HelpText.cmdWarp);
		}
		else
		{
			RandWarp.WarpPlayer(player);
		}
		
		return true;
	}
	
	private boolean force(CommandSender sender, String[] args)
	{
		if (!sender.hasPermission("permissions.randomwarp.force")) return false;
		
		if ((args.length < 2) || isHelpText(args[1]))
		{
			Message(sender, HelpText.cmdForce);
		}
		else
		{
			String playerName = args[1];
			if (args.length > 2)
			{
				for (int i = 2; i < args.length; i++)
				{ 
					playerName = playerName + " " + args[i];
				}
			}
			
			Player player = Main.server.getPlayer(playerName);
			
			if (player != null)
			{
				if (!player.hasPermission("permissions.randomwarp.immunity"))
				{
					RandWarp.WarpPlayer(player);
					
					Message(sender, "You have forced "+playerName+" into a Random Warp!", true);
					
					Message(player, "You have been forced into a Random Warp!", true);
				}
				else
				{
					Message(sender, "You can't force "+playerName+"! They're immune!", true);
				}
			}
			else
			{
				Message(sender, "Could not find player: "+playerName, true);
			}
		}
		
		return true;
	}
	
	private boolean range(CommandSender sender, ValidCommand cmd, String[] args)
	{
		if (!sender.hasPermission("permissions.randomwarp.config")) return false;
		
		switch(cmd)
		{
			case range:
				double minRaw = Main.config.getDouble("minimumDistance");
				double maxRaw = Main.config.getDouble("maximumDistance");
				
				String minNeat = formatDouble(minRaw);
				String maxNeat = formatDouble(maxRaw);
				
				if (args.length >= 2)
				{
					if (isHelpText(args[1]) || (args.length != 3))
					{
						Message(sender, HelpText.cmdRange);
					}
					else
					{
						double minTarget = safeParse(args[1]);
						double maxTarget = safeParse(args[2]);
						
						if (Double.isNaN(minTarget))
						{
							Message(sender, "Invalid minimum range.", true);
						}
						else if (Double.isNaN(maxTarget))
						{
							Message(sender, "Invalid maximum range.", true);
						}
						else
						{
							Main.config.set("minimumDistance",  minTarget);
							Main.config.set("maximumDistance",  maxTarget);
							Main.writeConfig();
							
							Message(sender, "Range changed from ("+minNeat+", "+maxNeat+") to ("+formatDouble(minTarget)+", "+formatDouble(maxTarget)+").");
						}
					}
				}
				else
				{
					Message(sender, "Current range: " + minNeat + " to " + maxNeat, true);
					Message(sender, HelpText.cmdRange);
				}
				return true;
			case min:
				doMinMax(sender, "Min", "Minimum", "minimumDistance", args);
				return true;
			case max:
				doMinMax(sender, "Max", "Maximum", "maximumDistance", args);
				return true;
			default:
				return false;
		}
	}
	
	private void doMinMax(CommandSender sender, String nameShort, String nameLong, String config, String[] args)
	{
		double currentRaw = Main.config.getDouble(config);
		String current = formatDouble(currentRaw);
		
		if (args.length >= 2)
		{
			if (isHelpText(args[1]) || (args.length != 2))
			{
				Message(sender, "/randomwarp " + nameShort.toLowerCase() + " [distance]");
			}
			else
			{
				double target = safeParse(args[1]);
				
				if (!Double.isNaN(target))
				{
					Main.config.set(config, target);
					Main.writeConfig();
					
					Message(sender, nameLong + " distance changed from " + current + " to " + formatDouble(target), true);
				}
				else
				{
					Message(sender, "Invalid " + nameLong.toLowerCase() + " distance.", true);
				}
			}
		}
		else
		{
			Message(sender, "Current " + nameLong.toLowerCase() + ": " + current, true);
			Message(sender, "/randomwarp " + nameShort.toLowerCase() + " [distance]");
		}
	}
	
	private boolean toggle(CommandSender sender, String[] args)
	{
		if (!sender.hasPermission("permissions.randomwarp.config")) return false;
		
		if ((args.length < 2) || isHelpText(args[1]))
		{
			Message(sender, HelpText.cmdToggle);
		}
		else
		{
			boolean set = (args.length >= 3);
			
			boolean state = false;
			
			
			
			if (set)
			{
				int input = parseBool(args[2]);
				
				if (input == -1)
				{
					Message(sender, "\""+args[2]+"\" is not valid.", true);
					return false;
				}
				else if (input == -2)
				{
					Message(sender, HelpText.cmdToggle);
					return true;
				}
				else
				{
					state = (input == 1);
				}
			}
			
			if (args[1].equalsIgnoreCase("fire"))
			{
				if (set)
					Main.config.set("suppressIgnition", !state);
				
				Message(sender, "Fire " + (Main.config.getBoolean("suppressIgnition") ? "disabled." : "enabled."), true);
					
			}
			else if (args[1].equalsIgnoreCase("lightning"))
			{
				if (set)
					Main.config.set("hideLightning", !state);
					
				Message(sender, "Lightning " + (Main.config.getBoolean("hideLightning") ? "disabled." : "enabled."), true);
					
			}
			else if (args[1].equalsIgnoreCase("sound"))
			{
				if (set)
					Main.config.set("muteWarpSound", !state);
					
				Message(sender, "Sound " + (Main.config.getBoolean("muteWarpSound") ? "disabled." : "enabled."), true);
			}
			else
			{
				Message(sender, "\""+args[1]+"\" is not valid.", true);
				return false;
			}
			
			Main.writeConfig();
		}
		
		return true;
	}
	
	private boolean duration(CommandSender sender, String[] args)
	{
		if (!sender.hasPermission("permissions.randomwarp.config")) return false;
		
		if ((args.length < 2) || isHelpText(args[1]))
		{
			Message(sender, HelpText.cmdDuration);
		}
		else
		{	
			boolean set = ((args.length >= 3) && !isHelpText(args[2]));
			
			int value = 0;
			
			if (set)
			{
				value = (int)Math.floor(safeParse(args[2]));
			}
			
			if (args[1].equalsIgnoreCase("fire"))
			{
				if (!set)
					Message(sender, "Fire duration is " + Main.config.getInt("ignitionTicks") + " ticks.", true);
				else
				{
					Main.config.set("ignitionTicks", value);
					
					Message(sender, "Fire duration has been changed to " + value + " ticks.", true);
				}
			}
			else if (args[1].equalsIgnoreCase("safety"))
			{
				if (!set)
					Message(sender, "Safety check delay is " + Main.config.getInt("safetyCheckDelay") + " ticks.", true);
				else
				{
					Main.config.set("safetyCheckDelay", value);

					Message(sender, "Safety check delay has been changed to " + value + " ticks.", true);
				}
			}
			else if (args[1].equalsIgnoreCase("delay"))
			{
				if (!set)
					Message(sender, "Random warp delay is " + Main.config.getInt("warpDelay") + " seconds.", true);
				else
				{
					Main.config.set("warpDelay", value);

					Message(sender, "Random warp delay has been changed to " + value + " seconds.", true);
				}
			}
			else
			{
				Message(sender, "\""+args[1]+"\" is not valid.", true);
				return false;
			}
			
			if ((args.length >= 3) && isHelpText(args[2]))
			{
				Message(sender, HelpText.cmdDuration);
			}
			
			Main.writeConfig();
		}
		
		return true;
	}
	
	private boolean edit(CommandSender sender, String[] args)
	{
		if (!sender.hasPermission("permissions.randomwarp.config")) return false;
		
		if ((args.length < 2) || isHelpText(args[1]))
		{
			Message(sender, HelpText.cmdEdit);
		}
		else
		{
			String text = "";
			
			if (args.length >= 3)
			{
				text = args[2];
				for (int i = 3; i < args.length; i++)
				{ 
					text = text + " " + args[i];
				}
			}
			
			if (args[1].equalsIgnoreCase("title"))
			{
				if ((args.length < 3) || Main.config.getString("warpSignTitle").equals(text))
					Message(sender, "Title: \"" + Main.config.getString("warpSignTitle")+"\"", true);
				else
				{
					if (isHelpText(args[1]))
					{
						Message(sender, HelpText.cmdEdit);
					}
					else
					{
						Main.config.set("warpSignTitle", text);
						
						Message(sender, "Title changed to: \"" + Main.config.getString("warpSignTitle")+"\"", true);
					}
				}
			}
			else if (args[1].equalsIgnoreCase("description"))
			{
				if (isHelpText(args[1]))
				{
					Message(sender, HelpText.cmdEdit);
				}
				else
				{
					if (Main.config.getString("warpSignDescription").equals(text))
					{
						Message(sender, "Description: \"" + Main.config.getString("warpSignDescription")+"\"", true);
					}
					else
					{
						Main.config.set("warpSignDescription", text);
					
						Message(sender, "Description changed to: \"" + Main.config.getString("warpSignDescription")+"\"", true);
					}
				}
			}
			else
			{
				Message(sender, "\""+args[1]+"\" is not valid.", true);
				return false;
			}
			
			
			
			Main.writeConfig();
		}
		
		return true;
	}
	
	private boolean color(CommandSender sender, String[] args)
	{
		if (!sender.hasPermission("permissions.randomwarp.config")) return false;
		
		boolean isPlayer = (sender instanceof Player);
		
		if ((args.length < 2) || isHelpText(args[1]))
		{
			Message(sender, HelpText.cmdColor);
		}
		else
		{
			boolean set = (args.length >= 3); 
			
			ValidColor color = ValidColor.white;
			
			if (set)
			{
				color = ValidColor.getValue(args[2]);
			
				if (color == null)
				{
					Message(sender, "Invalid Color, available colors:\n"+ValidColor.getList(isPlayer), true);
					return true;
				}
			}
			
			String chatColor = ValidColor.getChatColor(color).name();
			
			if (args[1].equalsIgnoreCase("title"))
			{
				if (set)
				{
					Main.config.set("warpSignMainColor", chatColor);
				
					Message(sender, "Title color changed to: "+chatColor);
				}
				else
				{
					color = ValidColor.getValue(Main.config.getString("warpSignMainColor"));
					Message(sender, "Title color is currently: " + ValidColor.getChatColor(color) + color.toString());
				}
			}
			else if (args[1].equalsIgnoreCase("brackets"))
			{
				if (set)
				{
					Main.config.set("warpSignAltColor", chatColor);
					
					Message(sender, "Bracket color changed to: "+chatColor);
				}
				else
				{
					color = ValidColor.getValue(Main.config.getString("warpSignAltColor"));
					Message(sender, "Bracket color is currently: " + ValidColor.getChatColor(color) + color.toString());
				}
			}
			else if (args[1].equalsIgnoreCase("description"))
			{
				if (set)
				{
					Main.config.set("warpSignDescColor", chatColor);
					
					Message(sender, "Description color changed to: "+chatColor);
				}
				else
				{
					color = ValidColor.getValue(Main.config.getString("warpSignDescColor"));
					Message(sender, "Description color is currently: " + ValidColor.getChatColor(color) + color.toString());
				}
			}
			else if (args[1].equalsIgnoreCase("borderA"))
			{
				if (set)
				{
					Main.config.set("warpSignMainBorderColor", chatColor);
					
					Message(sender, "Main Border color changed to: "+chatColor);
				}
				else
				{
					color = ValidColor.getValue(Main.config.getString("warpSignMainBorderColor"));
					Message(sender, "Main Border color is currently: " + ValidColor.getChatColor(color) + color.toString());
				}
			}
			else if (args[1].equalsIgnoreCase("borderB"))
			{
				if (set)
				{
					Main.config.set("warpSignAltBorderColor", chatColor);
					
					Message(sender, "Secondary Border color changed to: "+chatColor);
				}
				else
				{
					color = ValidColor.getValue(Main.config.getString("warpSignAltBorderColor"));
					Message(sender, "Secondary Border color is currently: " + ValidColor.getChatColor(color) + color.toString());
				}
			}
			else
			{
				Message(sender, "\""+args[1]+"\" is not valid.", true);
				return false;
			}
			
			Main.writeConfig();
			
			if (!set)
			{
				Message(sender, "Available Colors:\n"+ValidColor.getList(isPlayer), true);
			}
		}
		
		return true;
	}
	
	private boolean permissions(CommandSender sender)
	{
		if (!sender.hasPermission("permissions.randomwarp.config")) return false;
		
		Message(sender, "permissions.randomwarp.create    - Create Random Warps");
		Message(sender, "permissions.randomwarp.silent    - /silent");
		Message(sender, "permissions.randomwarp.remoteuse - /warp");
		Message(sender, "permissions.randomwarp.force      - /force");
		Message(sender, "permissions.randomwarp.config    - /range /min /max /toggle /duration /edit /color");
		Message(sender, "permissions.randomwarp.reload    - /reload");
		
		return true;
	}
	
	private boolean reload(CommandSender sender)
	{
		if (!sender.hasPermission("permissions.randomwarp.reload")) return false;
		
		Main.reload();
		
		if (sender instanceof Player)
		{
			Message(sender, HelpText.logRestart, true);
		}
		
		return true;
	}
	
	////	////	////	////	////
	
	private boolean isHelpText(String value)
	{
		return (value.isEmpty() || value.equals("?") || value.equalsIgnoreCase("help"));
	}
	
	private void DisplayAvailableCommands(CommandSender sender)
	{
		Message(sender, "Alias: /rw /randwarp");
		
		if (sender instanceof Player)
		{
			if (sender.hasPermission("permissions.randomwarp.create"))
			{
				Message(sender, HelpText.cmdCreate);
			}
			if (sender.hasPermission("permissions.randomwarp.silent"))
			{
				Message(sender, HelpText.cmdSilent);
			}
			if (sender.hasPermission("permissions.randomwarp.remoteuse"))
			{
				Message(sender, HelpText.cmdWarp);
			}
		}
		if (sender.hasPermission("permissions.randomwarp.force"))
		{
			Message(sender, HelpText.cmdForce);
		}
		
		if (sender.hasPermission("permissions.randomwarp.config"))
		{
			Message(sender, HelpText.cmdMinMax);
			Message(sender, HelpText.cmdRange);
			Message(sender, HelpText.cmdToggle);
			Message(sender, HelpText.cmdDuration);
			Message(sender, HelpText.cmdEdit);
			Message(sender, HelpText.cmdColor);
			Message(sender, HelpText.cmdColors);
			Message(sender, HelpText.cmdPermissions);
		}
		
		if (sender.hasPermission("permissions.randomwarp.reload"))
		{
			Message(sender, HelpText.cmdReload);
		}
	}
	
	public static void Message(Player player, String message)
	{
		Message((CommandSender)player, message);
	}
	
	public static void Message(CommandSender sender, String message)
	{
		Message(sender, message, false);
	}
	
	public static void Message(CommandSender sender, String message, boolean prefix)
	{
		String fancyMessage;
		
		ChatColor title = ChatColor.valueOf(Main.config.getString("warpSignMainColor"));
		ChatColor bracket = ChatColor.valueOf(Main.config.getString("warpSignAltColor"));
		
		if (prefix)
			fancyMessage = bracket + "[" + title + "RandomWarp" + bracket + "]" + ChatColor.WHITE + ": " + message;
		else
			fancyMessage = ChatColor.WHITE + message;
			
		
		if ((null != sender) && (sender instanceof Player))
		{
			sender.sendMessage(fancyMessage);
		}
		else
		{
			Main.console.sendMessage(fancyMessage);
		}
	}
	
	private String formatDouble(double value)
	{
		if (Math.floor(value) == value)
		{
			return String.format("%1$.0f", value);
		}
		else
		{
			return Double.toString(value);
		}
	}
	
	private double safeParse(String value)
	{
		double result = Double.NaN;
		
		try
		{
			result = Double.parseDouble(value);
		}
		catch (NullPointerException e)
		{
			Message(null, "RandomWarp safeParse null exception: " + e.getMessage());
			
			return Double.NaN;
		}
		catch (NumberFormatException e)
		{
			return Double.NaN;
		}
		
		return result;
	}
	
	private int parseBool(String value)
	{
		if ((null != value) && !value.isEmpty())
		{
			if (isHelpText(value))
				return -2;
			else if (value.equalsIgnoreCase("on") || value.equalsIgnoreCase("true") || value.equalsIgnoreCase("t") || value.equalsIgnoreCase("1") || value.equalsIgnoreCase("yes") || value.equalsIgnoreCase("y") || value.equalsIgnoreCase("enable") || value.equalsIgnoreCase("enabled"))
				return 1;
			else if (value.equalsIgnoreCase("off") || value.equalsIgnoreCase("false") || value.equalsIgnoreCase("f") || value.equalsIgnoreCase("0") || value.equalsIgnoreCase("no") || value.equalsIgnoreCase("n") || value.equalsIgnoreCase("disable") || value.equalsIgnoreCase("disabled"))
				return 0;
		}
		
		return -1;
	}
}
