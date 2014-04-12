package au.com.addstar.easteregghunt;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.WeakHashMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.ItemDespawnEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.SpawnEgg;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import com.pauldavdesign.mineauz.minigames.MinigamePlayer;
import com.pauldavdesign.mineauz.minigames.Minigames;
import com.pauldavdesign.mineauz.minigames.gametypes.MinigameType;
import com.pauldavdesign.mineauz.minigames.minigame.Minigame;
import com.pauldavdesign.mineauz.minigames.scoring.ScoreType;

public class HuntPlugin extends JavaPlugin implements Listener
{
	private static final EntityType[] mEggTypes = new EntityType[] {EntityType.CREEPER, EntityType.ZOMBIE, EntityType.BLAZE, EntityType.CAVE_SPIDER, EntityType.CHICKEN, EntityType.COW, EntityType.ENDERMAN, EntityType.GHAST, EntityType.HORSE, EntityType.MAGMA_CUBE, EntityType.MUSHROOM_COW, EntityType.OCELOT, EntityType.PIG, EntityType.PIG_ZOMBIE, EntityType.SHEEP, EntityType.SKELETON, EntityType.SLIME, EntityType.SPIDER, EntityType.SQUID, EntityType.VILLAGER, EntityType.WITCH, EntityType.WOLF};
	public static final String eggName = ChatColor.translateAlternateColorCodes('&', "&rEasterEgg&r");
	private static Random mRand = new Random();
	
	private WeakHashMap<Player, String> mWaitingEggs = new WeakHashMap<Player, String>();
	private WeakHashMap<Player, Boolean> mRemoveActive = new WeakHashMap<Player, Boolean>();
	
	@Override
	public void onEnable()
	{
		Bukkit.getPluginManager().registerEvents(this, this);
		DisplayManager.initialize(this);
		ScoreType.addScoreType(new EasterEggLogic(this));
	}
	
	@Override
	public boolean onCommand( CommandSender sender, Command command, String label, String[] args )
	{
		if(command.getName().equals("createeasteregg"))
		{
			if(args.length != 1)
				return false;
			
			if(!(sender instanceof Player))
			{
				sender.sendMessage(ChatColor.RED + "You must be in-game to do this");
				return true;
			}
			
			mWaitingEggs.put((Player)sender, args[0]);
			sender.sendMessage(ChatColor.GOLD + "Right click the ground to finish placing the egg. Left click to cancel");
			
			return true;
		}
		else if(command.getName().equals("removeeasteregg"))
		{
			if(args.length != 0)
				return false;
			
			if(!(sender instanceof Player))
			{
				sender.sendMessage(ChatColor.RED + "You must be in-game to do this");
				return true;
			}
			
			boolean active;
			
			if(mRemoveActive.containsKey(sender))
				active = mRemoveActive.get(sender);
			else
				active = false;
			
			active = !active;
			mRemoveActive.put((Player)sender, active);
			
			if(active)
			{
				sender.sendMessage(ChatColor.GOLD + "Easter Egg remove mode on");
				sender.sendMessage(ChatColor.GOLD + "Click the block the egg is in the remove it");
			}
			else
			{
				sender.sendMessage(ChatColor.GREEN + "Easter Egg remove mode off");
			}
			
			return true;
		}
		else if(command.getName().equals("removealleastereggs"))
		{
			if(args.length != 0 && args.length != 1)
				return false;
			
			if(!(sender instanceof Player))
			{
				sender.sendMessage(ChatColor.RED + "You must be in-game to do this");
				return true;
			}
			
			int radius = -1;
			
			if(args.length == 1)
			{
				try
				{
					radius = Integer.parseInt(args[0]);
					if(radius <= 0)
					{
						sender.sendMessage(ChatColor.RED + "Radius must be a positive number");
						return true;
					}
				}
				catch(NumberFormatException e)
				{
					sender.sendMessage(ChatColor.RED + "Radius must be a positive number");
					return true;
				}
			}
			
			if(radius == -1)
			{
				for(Item item : ((Player)sender).getWorld().getEntitiesByClass(Item.class))
				{
					if(isEasterEgg(item))
						item.remove();
				}
				
				sender.sendMessage(ChatColor.GREEN + "Removed all easter eggs from this world");
				sender.sendMessage(ChatColor.GRAY + "WARNING: Will not have removed eggs in unloaded chunks");
			}
			else
			{
				for(Entity ent : ((Player)sender).getNearbyEntities(radius, radius, radius))
				{
					if(ent instanceof Item)
					{
						if(isEasterEgg((Item)ent))
							ent.remove();
					}
				}
				
				sender.sendMessage(ChatColor.GREEN + "Removed all easter eggs within a " + radius + " block radius from you.");
				sender.sendMessage(ChatColor.GRAY + "WARNING: Will not have removed eggs in unloaded chunks");
			}
			
			return true;
		}
		
		return false;
	}
	
	public static SpawnEgg randomType()
	{
		SpawnEgg egg = new SpawnEgg();
		egg.setSpawnedType(mEggTypes[mRand.nextInt(mEggTypes.length)]);
		return egg;
	}
	
	@SuppressWarnings( "deprecation" )
	public static ItemStack newEasterEgg(String name)
	{
		ItemStack item = new ItemStack(Material.MONSTER_EGG);
		item.setDurability(randomType().getData());
		
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(eggName);
		meta.setLore(Arrays.asList(name));
		item.setItemMeta(meta);
		
		return item;
	}
	
	public static boolean isEasterEgg(Item item)
	{
		ItemStack stack = item.getItemStack();
		if(!stack.hasItemMeta())
			return false;
		
		ItemMeta meta = stack.getItemMeta();
		if(!eggName.equals(meta.getDisplayName()))
			return false;
		
		return true;
	}
	
	@EventHandler(priority=EventPriority.LOWEST, ignoreCancelled=false)
	private void onInteract(PlayerInteractEvent event)
	{
		if(event.getAction() != Action.RIGHT_CLICK_BLOCK && event.getAction() != Action.LEFT_CLICK_BLOCK)
			return;
		
		if(mRemoveActive.containsKey(event.getPlayer()) && mRemoveActive.get(event.getPlayer()))
		{
			Location loc = event.getClickedBlock().getRelative(event.getBlockFace()).getLocation();
			loc.add(0.5, 0, 0.5);
			
			List<Entity> entities = event.getPlayer().getNearbyEntities(7, 7, 7);
			
			boolean removed = false;
			for(Entity ent : entities)
			{
				if(ent instanceof Item && ent.getLocation().distance(loc) < 1)
				{
					ent.remove();
					removed = true;
				}
			}
			
			if(removed)
				event.getPlayer().sendMessage(ChatColor.GOLD + "Easter Egg removed");
			
			event.setCancelled(true);
		}
		else
		{
			String eggName = mWaitingEggs.remove(event.getPlayer());
			if(eggName != null)
			{
				if(event.getAction() == Action.RIGHT_CLICK_BLOCK)
				{
					Location loc = event.getClickedBlock().getRelative(event.getBlockFace()).getLocation();
					loc.add(0.5, 0.5, 0.5);
					
					Item item = event.getPlayer().getWorld().dropItem(loc, newEasterEgg(eggName));
					item.setPickupDelay(0);
					item.setVelocity(new Vector());
					
					event.getPlayer().sendMessage(ChatColor.GREEN + "Easter egg '" + eggName + "' has been placed.");
				}
				else if(event.getAction() == Action.LEFT_CLICK_BLOCK)
					event.getPlayer().sendMessage(ChatColor.GOLD + "Easter egg placement cancelled.");
				
				event.setCancelled(true);
			}
		}
	}
	
	@EventHandler(priority=EventPriority.LOWEST, ignoreCancelled=true)
	private void onItemPickup(PlayerPickupItemEvent event)
	{
		if(!isEasterEgg(event.getItem()))
			return;
	
		event.setCancelled(true);
		
		ItemStack item = event.getItem().getItemStack();
	
		ItemMeta meta = item.getItemMeta();
		if(!meta.hasLore())
		{
			getLogger().warning("Easter egg did not have egg name?!");
			return;
		}
		
		String name = meta.getLore().get(0);
		
		MinigamePlayer player = Minigames.plugin.pdata.getMinigamePlayer(event.getPlayer());
		if(player == null)
			return;
		
		if(!player.isInMinigame())
			return;
		
		Minigame game = player.getMinigame();
		
		if(!game.hasFlags() || player.getFlags().contains(name) || !game.getFlags().contains(name) || (game.getType() != MinigameType.SINGLEPLAYER && !game.hasStarted()))
			return;
		
		
		player.addFlag(name);
		
		event.getPlayer().playSound(event.getPlayer().getLocation(), Sound.ITEM_PICKUP, 1, 1);
		
		Bukkit.getPluginManager().callEvent(new FlagGrabEvent(player, name, game, event.getItem().getLocation()));
	}
	
	@EventHandler(priority=EventPriority.LOWEST, ignoreCancelled=true)
	private void onItemDespawn(ItemDespawnEvent event)
	{
		if(isEasterEgg(event.getEntity()))
			event.setCancelled(true);
	}
}
