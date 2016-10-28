package com.tehelee.randomWarp;

import java.util.Random;

import org.bukkit.Color;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class RandWarp
{	
	public static void WarpPlayer(final Player player)
	{
		WarpPlayer(player, false);
	}
	
	public static void WarpPlayer(final Player player, boolean silent)
	{
		Random rand = new Random();
		
		double min, max, range, radius, angle;
		
		Location destination, origin = player.getLocation();
		
		World world = origin.getWorld();
		
		int posX, posZ, maxHeight = world.getMaxHeight();
		
		min = Main.config.getDouble("minimumDistance");
		max = Main.config.getDouble("maximumDistance");
		
		long delay = Main.config.getLong("safetyCheckDelay");
		
		range = max - min;
		
		boolean success = false;

		do
		{
			radius = (rand.nextDouble() * range) + min;
			angle = (rand.nextDouble() * 360);
			
			posX = (int) Math.round(radius * Math.cos(angle));
			posZ = (int) Math.round(radius * Math.sin(angle));
			
			System.out.println("PosX: "+posX+" PosZ: "+posZ);
			
			destination = findSafeBlock(world, posX, posZ, maxHeight);
			
			if (null == destination) continue;
			
			player.setInvulnerable(true);
			
			player.teleport(destination, TeleportCause.COMMAND);
			
			final int x = posX;
			final int z = posZ;
			final int height = maxHeight;
			
			Main.DelayedTask(new Runnable()
			{
				@Override
				public void run()
				{	
					
					
					Location dest = findSafeBlock(world, x, z, height);
					
					if (null == dest) WarpPlayer(player);
					else player.teleport(dest, TeleportCause.COMMAND);
					
					if (!silent)
					{
						int ignitionTicks = Main.config.getInt("ignitionTicks");
						if ((Main.config.getBoolean("suppressIgnition") == false) || (ignitionTicks <= 5))
						{
							player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, ignitionTicks, 1, true, true, Color.BLACK));
							player.setFireTicks(ignitionTicks-5);
						}
						
						if (Main.config.getBoolean("hideLightning") == false)
						{
							world.spigot().strikeLightningEffect(dest, true);
							
							world.playSound(dest, Sound.ENTITY_LIGHTNING_IMPACT, 2, 0.5F);
							world.playSound(dest, Sound.ENTITY_LIGHTNING_THUNDER, 4, 0.5F);
						}
						
						if (Main.config.getBoolean("muteWarpSound") == false)
							world.playEffect(dest, Effect.PORTAL_TRAVEL, 96);
					}
					player.setInvulnerable(false);
					
					Main.logWarp(player, origin);
				}
			}, delay);
			success = true;
		}
		while (!success);
	}
	
	public static Location findSafeBlock(World world, int x, int z, int maxHeight)
	{
		Material mat;
		
		Block block;
		
		int y, checkA, checkB;
		
		for (checkA = maxHeight; checkA >= 0; checkA--)
		{
			block = world.getBlockAt(x, checkA, z);
			
			if (block.isEmpty()) continue;
			
			mat = block.getType();
			
			if (mat == Material.AIR) continue;
			
			switch(mat)
			{
			case STONE:
			case GRASS:
			case DIRT:
			case COBBLESTONE:
			case SAND:
			case GRAVEL:
			case LOG:
			case LEAVES:
			case SANDSTONE:
			case MOSSY_COBBLESTONE:
			case SNOW:
			case ICE:
			case SNOW_BLOCK:
			case CLAY:
			case PUMPKIN:
			case MYCEL:
			case STAINED_CLAY:
			case LEAVES_2:
			case LOG_2:
			case HARD_CLAY:
			case PACKED_ICE:
			case GRASS_PATH:
			case FROSTED_ICE:
				y = checkA+1;
				break;
			default:
				y = -1;
				break;
			}
			
			if (y >= 0)
			{
				for (checkB = 0; checkB <= 3; checkB++)
				{
					block = world.getBlockAt(x, y+checkB, z);
					
					mat = block.getType();
					
					if (mat.isSolid() || block.isLiquid())
					{
						return null;
					}
					else
					{
						if (!block.isEmpty())
						{
							block.breakNaturally();
						}
						else
						{
							block.setType(Material.AIR);
						}
					}
				}
				return new Location(world, x+0.5, y, z+0.5);
			}
		}
		
		return null;
	}
}
