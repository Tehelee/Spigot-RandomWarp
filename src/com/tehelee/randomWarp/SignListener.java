package com.tehelee.randomWarp;

import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class SignListener implements Listener
{
	@EventHandler
	public void onSignChange(SignChangeEvent e)
	{
		Main.console.sendMessage("SignChangeEvent: "+e.getLine(0));
		if (e.getPlayer().hasPermission("permissions.randomwarp.create"))
		{
			boolean warpSign = false;
			for (int i = 0; i < 4; i++)
			{
				String line = e.getLine(i);
				if (line != null && !line.isEmpty() && ((line.compareToIgnoreCase("[RandomWarp]") == 0) || (line.compareToIgnoreCase("[RandWarp]") == 0)))
				{
					warpSign = true;
					break;
				}
			}
			if (warpSign)
			{
				ChatColor main = ChatColor.valueOf(Main.config.getString("warpSignMainColor"));
				ChatColor alt = ChatColor.valueOf(Main.config.getString("warpSignAltColor"));
				ChatColor desc = ChatColor.valueOf(Main.config.getString("warpSignDescColor"));
				String border = ChatColor.BOLD + "" + ChatColor.MAGIC + "" + ChatColor.DARK_PURPLE + "" + ChatColor.MAGIC + " - " + ChatColor.BLACK + "" + ChatColor.MAGIC + " - " + ChatColor.DARK_PURPLE + "" + ChatColor.MAGIC + " - " + ChatColor.BLACK + "" + ChatColor.MAGIC + " - " + ChatColor.DARK_PURPLE + "" + ChatColor.MAGIC + " - ";  
				e.setLine(0, border);
				e.setLine(1, ChatColor.BOLD + "" + alt + "[" + main + Main.config.getString("warpSignTitle") + alt + "]");
				e.setLine(2, ChatColor.ITALIC + "" + desc + Main.config.getString("warpSignDescription"));
				e.setLine(3, border);
				
				Block block = e.getBlock();
				World world	= block.getWorld();
				Location location = block.getLocation();
				location = new Location(world, location.getX() + 0.5, location.getY() + 0.5, location.getZ() + 0.5);
				world.playEffect(location, Effect.DRAGON_BREATH, 48);
				world.playSound(location, Sound.ENTITY_IRONGOLEM_HURT, 2, 0.5F);
			}
		}
	}
	
	@EventHandler
	public void onSignBreak(BlockBreakEvent e)
	{
		Block block = e.getBlock();
		Material mat = block.getType();
		if ((mat == Material.SIGN_POST) || (mat == Material.WALL_SIGN))
		{
			Sign sign = (Sign)block.getState();
			if (ChatColor.stripColor(sign.getLine(1)).equalsIgnoreCase("["+Main.config.getString("warpSignTitle")+"]"))
			{
				if (!e.getPlayer().hasPermission("permissions.randomwarp.create"))
				{
					e.setCancelled(true);
				}
			}
		}
	}
	
	@EventHandler
	public void onSignClick(PlayerInteractEvent e)
	{
		if (e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
		
		Player player = e.getPlayer();
		if (player.hasPermission("permissions.randomwarp.use"))
		{
			Block block = e.getClickedBlock();
			Material mat = block.getType();
			if ((mat == Material.SIGN_POST) || (mat == Material.WALL_SIGN))
			{
				Sign sign = (Sign)block.getState();
				if (ChatColor.stripColor(sign.getLine(0)).equals(" -  -  -  -  - ") && ChatColor.stripColor(sign.getLine(1)).equalsIgnoreCase("["+Main.config.getString("warpSignTitle")+"]") && ChatColor.stripColor(sign.getLine(3)).equals(" -  -  -  -  - "))
				{
					long waitTime = Main.getLastWarp(player);
					
					if (waitTime < 0)
					{
						waitTime *= -1;
						
						if (waitTime < 60)
						{
							CmdRandomWarp.Message(player, ChatColor.GOLD + "You must wait another " + ChatColor.RED + waitTime + ChatColor.GOLD + " seconds.", true);
						}
						else
						{
							CmdRandomWarp.Message(player, ChatColor.GOLD + "You must wait another " + ChatColor.RED + (waitTime / 60) + ChatColor.GOLD + " minutes and " + ChatColor.RED + (waitTime % 60) + ChatColor.GOLD + " seconds.", true);
						}
					}
					else
					{
						RandWarp.WarpPlayer(player);
					}
					
					e.setCancelled(true);
				}
			}
		}
	}
}
