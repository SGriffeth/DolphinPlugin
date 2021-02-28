package name.griffeth.sebastian.DolphinPlugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.attribute.Attribute;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Dolphin;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import org.spigotmc.event.entity.EntityDismountEvent;

import name.griffeth.sebastian.DolphinPlugin.Files.DataManager;

public class DolphinMain extends JavaPlugin implements Listener { 
	
	protected final String NAME = ChatColor.DARK_BLUE + "[" + ChatColor.AQUA + getName() + ChatColor.DARK_BLUE + "]" + ChatColor.WHITE + " ";
	
	protected final static Map<UUID,List<String>> TAMED = new HashMap<UUID,List<String>>();
	
	protected DataManager data;
	
	@Override
	public void onEnable() {
		data = new DataManager(this);
		load();
		getServer().getPluginManager().registerEvents(this, this);
		Bukkit.getLogger().info(NAME + getName() + " has been enabled");
	}
	 
	@Override
	public void onDisable() {
		save();
	}
	
	public void save() {
		if(!TAMED.isEmpty()) {
			for(Map.Entry<UUID, List<String>> entry : TAMED.entrySet()) {
				if(entry != null) {
					if(entry.getKey() != null && entry.getValue() != null) {
						data.getConfig().set("data." + entry.getKey(), entry.getValue());
					}
				}
			}
		}
		data.saveConfig();
	}
	
	public void load() {
		if(data.getConfig().contains("data")) {
			data.getConfig().getConfigurationSection("data").getKeys(false).forEach(key -> {
				//try {
					List<String> value = (List<String>) data.getConfig().get("data." + key);
					TAMED.put(UUID.fromString(key), value);
				/*}catch(Exception e) {
					
				}*/
			});
		}
	}
	
	protected String enumerate(int i) {
		return ChatColor.WHITE + "" + i + ChatColor.DARK_BLUE + "." + ChatColor.WHITE + " ";
	}
	
	protected static void addTamed(Entity ent,Entity tamed) {
		UUID id = ent.getUniqueId();
		List<String> pets = TAMED.get(id);
		if(pets == null) {
			pets = new ArrayList<String>();
		}
		pets.add(tamed.getUniqueId().toString());
		TAMED.put(id, pets);
	}
	
	protected static List<String> getTamed(Entity ent) {
		if(TAMED.get(ent.getUniqueId()) == null) return new ArrayList<String>();
		return TAMED.get(ent.getUniqueId());
	}
	
	@Override
	public boolean onCommand(CommandSender sender,Command cmd,String label,String[] args) {
		if(!(sender instanceof Player)) return true;
		Player p = (Player) sender;
		switch(label) {
		case "pets":
			switch(args.length) {
			case 1:
				switch(args[0]) {
				case "find":
					Iterator<String> it = getTamed(p).iterator();
					int count = 0;
					while(it.hasNext()) {
						UUID next = UUID.fromString(it.next());
						Entity ent = Bukkit.getEntity(next);
						p.sendMessage("Here is a list of pets");
						if(ent != null) {
							count++;
							p.sendMessage(enumerate(count) + ent.getName() + ChatColor.LIGHT_PURPLE + " is at : " + ChatColor.WHITE + ent.getLocation().getBlockX()
							+ "" + ChatColor.AQUA + ", " + ChatColor.WHITE + ent.getLocation().getBlockY() + "" + ChatColor.AQUA + ", " + 
							ChatColor.WHITE + ent.getLocation().getBlockZ() + ChatColor.LIGHT_PURPLE + " in world " + ChatColor.WHITE + ent.getWorld().getName());
						}
					}
					break;
					default:
						p.sendMessage("Do " + ChatColor.LIGHT_PURPLE + "/pets find" + ChatColor.WHITE + " to find your pets!");
						break;
				}
				break;
			case 0:
				p.sendMessage("Do " + ChatColor.LIGHT_PURPLE + "/pets find" + ChatColor.WHITE + " to find your pets!");
				break;
				default:
					p.sendMessage("Do " + ChatColor.LIGHT_PURPLE + "/pets find" + ChatColor.WHITE + " to find your pets!");
					break;
			}
			return true;
		}
		return true;
	}
	
	@EventHandler
	public void onEntityInteractEvent(PlayerInteractEntityEvent e) {
		Player p = e.getPlayer();
		Entity ent = e.getRightClicked();
		if(ent instanceof Dolphin) {
			Dolphin d = (Dolphin) ent;
			//If the player does not have fish in his hand return
			Material type = p.getInventory().getItemInMainHand().getType();
			//The following 'if' statement is checking for fish in the players hand
			if(type != Material.COD && type != Material.SALMON &&
			type != Material.COOKED_COD && type != Material.COOKED_SALMON &&
			type != Material.TROPICAL_FISH && type != Material.COD_BUCKET &&
			type != Material.SALMON_BUCKET && type != Material.TROPICAL_FISH_BUCKET &&
			type != Material.PUFFERFISH_BUCKET && type != Material.PUFFERFISH) {
				if(getTamed(p).contains(ent.getUniqueId().toString())) {
					ent.addPassenger(p);
					new Scheduler(d).runTaskTimer(this, 0L, 10L);
				}
				return;
			}
			if(type != Material.PUFFERFISH_BUCKET && type != Material.PUFFERFISH) {
				//The dolphin regains health
				if(d.getHealth() + d.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue()/10 < d.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue()) {
					d.setHealth(d.getHealth() + d.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue()/10);
				}else {
					d.setHealth(d.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
				}
				
				p.sendMessage(d.getName() + " regained " + d.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue()/10 + " health " + 
				ChatColor.DARK_GREEN + "(" + ChatColor.RED + d.getHealth() + ChatColor.WHITE + "/" + ChatColor.RED
				+ d.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue() + ChatColor.DARK_GREEN + ")");
			}else {
				//The puffer fish poisoned the dolphin
				d.addPotionEffect(new PotionEffect(PotionEffectType.POISON,60*20,4));
				d.addPotionEffect(new PotionEffect(PotionEffectType.HUNGER,14*20,3));
				d.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION,14*20,1));
				return;
			}
			//If the player has enough cod he tames the dolphin
			if(!getTamed(p).contains(ent.getUniqueId().toString()))
			if(p.getInventory().containsAtLeast(new ItemStack(Material.COD), 3)) {
				int ran = new Random().nextInt(101);
				p.getInventory().removeItem(new ItemStack(Material.COD,3));
				if(ran >= 0 && ran <= 50) {
					d.addPassenger(p);
					addTamed(p,d);
					new Scheduler(d).runTaskTimer(this, 0L, 10L);
					p.sendMessage("You successfully tamed the dolphin!");
				}else {
					//Weren't
					p.sendMessage("You weren't able to tame the dolphin");
				}
			}else {
				p.sendMessage(ChatColor.AQUA + "You need at least 3 " + ChatColor.WHITE + "Raw Cod" + ChatColor.AQUA + " to tame a dolphin");
			}
		}
	}
	
	@EventHandler
	public void onEntityDismountEvent(EntityDismountEvent e) {
		Entity d = e.getEntity();
		Entity m = e.getDismounted();
		if(!(d instanceof Player)) return;
		List<String> pets = getTamed(d);
		if(pets.contains(m.getUniqueId().toString())) {
			if(!((Player) d).isSneaking()) {
				e.setCancelled(true);
			}
		}
	}
	@EventHandler
	public void onPlayerCommandPreprocessEvent(PlayerCommandPreprocessEvent e) {
		Player p = e.getPlayer();
		String cmd = e.getMessage().substring(1);
		String[] args = cmd.split(" ");
		String message = args[0];
		if(message.startsWith(getName().toLowerCase() + ":")) {
			String suffix = message.substring(getName().length() + 1);
			Bukkit.dispatchCommand(p, suffix);
		}
		switch(message) {
		case "save":
			save();
			p.sendMessage(NAME + "data save in " + getName() + "/" + data.YML_FILE);
			break;
		case "load":
			load();
			p.sendMessage(NAME + "data loaded from " + getName() + "/" + data.YML_FILE);
			break;
		}
	}
} 
 