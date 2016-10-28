package com.tehelee.randomWarp;

import org.bukkit.ChatColor;

public enum ValidColor
{
	aqua,
	black,
	blue,
	dark_aqua,
	dark_blue,
	dark_gray,
	dark_grey,
	dark_green,
	dark_purple,
	dark_red,
	gold,
	gray,
	grey,
	green,
	light_purple,
	red,
	white,
	yellow;
	
	public String toString()
	{
		switch(this)
		{
			case aqua:			return "aqua";
			case black:			return "black";
			case blue:			return "blue";
			case dark_aqua:		return "dark_aqua";
			case dark_blue:		return "dark_blue";
			case dark_gray:		return "dark_gray";
			case dark_grey:		return "dark_grey";
			case dark_green:	return "dark_green";
			case dark_purple:	return "dark_purple";
			case dark_red:		return "dark_red";
			case gold:			return "gold";
			case gray:			return "gray";
			case grey:			return "grey";
			case green:			return "green";
			case light_purple:	return "light_purple";
			case red:			return "red";
			case white:			return "white";
			case yellow:		return "yellow";
		}			
		return null;
	}
	
	public static ValidColor getValue(String value)
	{
		String input = value.toLowerCase();
    	
		ValidColor[] colors = ValidColor.values();
    	
    	for (int i = 0; i < colors.length; i++)
    	{
    		if (input.equals(colors[i].toString())) return colors[i];
    	}
    	
        return null;
	}
	
	public static String getList(boolean flat)
	{
		String list = "";
		
		ValidColor[] colors = ValidColor.values();
		
		if (flat)
		{
			for (int i = 0; i < colors.length; i++)
	    	{
	    		list += ValidColor.getChatColor(colors[i]) + colors[i].toString() + ChatColor.WHITE + ((i < colors.length-1) ? ", " : "");
	    	}
		}
		else
		{
			for (int i = 0; i < colors.length; i++)
	    	{
	    		list += "\t" + ValidColor.getChatColor(colors[i]) + "[=] " + ChatColor.WHITE + colors[i].toString()+ ((i < colors.length-1) ? "\n" : "");
	    	}
		}
    
    	
    	return list;
	}
	
	public static ChatColor getChatColor(ValidColor color)
	{
		switch(color)
		{
			case aqua:			return ChatColor.AQUA;
			case black:			return ChatColor.BLACK;
			case blue:			return ChatColor.BLUE;
			case dark_aqua:		return ChatColor.DARK_AQUA;
			case dark_blue:		return ChatColor.DARK_BLUE;
			case dark_gray:		return ChatColor.DARK_GRAY;
			case dark_grey:		return ChatColor.DARK_GRAY;
			case dark_green:	return ChatColor.DARK_GREEN;
			case dark_purple:	return ChatColor.DARK_PURPLE;
			case dark_red:		return ChatColor.DARK_RED;
			case gold:			return ChatColor.GOLD;
			case gray:			return ChatColor.GRAY;
			case grey:			return ChatColor.GRAY;
			case green:			return ChatColor.GREEN;
			case light_purple:	return ChatColor.LIGHT_PURPLE;
			case red:			return ChatColor.RED;
			case white:			return ChatColor.WHITE;
			case yellow:		return ChatColor.YELLOW;
		}			
		return null;
	}
}
