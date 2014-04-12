package me.capit.Organizations;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;

import me.capit.Organizations.Organizations;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

public class ProtectionHandler implements Listener {
	public HashMap<String, String> protPlayers = new HashMap<String, String>();
		// PlayerName, Group
	public List<Player> overridePlayers = Arrays.asList();
	
	public HashMap<SerialLocation,String> blockData = new HashMap<SerialLocation, String>();;
		// PunchedBlock, FortificationString
	public HashMap<String,Integer> fs = new HashMap<String,Integer>();
		// FortificationKey, ForitifcationValue
	Plugin plugin;
	
	public ProtectionHandler(Plugin p){
		plugin=p;
		fs.put("STONE", 8);
		fs.put("LEATHER", 16);
		fs.put("OBSIDIAN", 32);
		fs.put("IRON_INGOT", 64);
		fs.put("DIAMOND", 128);
		fs.put("EMERALD", 256);
	}
	
	@EventHandler
	public void onPlayerClickEvent(PlayerInteractEvent e){
		loadBlockDataFromDisk();
		Player p = e.getPlayer();
		Block b = e.getClickedBlock();
		if (protPlayers.containsKey(p.getName())){
			boolean contains = false;
			String fortInfo = "";
			for (SerialLocation sloc : blockData.keySet()){
				if (sloc.getLocation(plugin).getX() == b.getLocation().getX() && 
						sloc.getLocation(plugin).getY() == b.getLocation().getY() &&
						sloc.getLocation(plugin).getZ() == b.getLocation().getZ() &&
						sloc.getLocation(plugin).getWorld().getName().equalsIgnoreCase(b.getLocation().getWorld().getName())){
					contains=true;
					fortInfo=blockData.get(sloc);
					int dashLoc = fortInfo.indexOf('-');
					int pipeLoc = fortInfo.indexOf('|');
					int colonLoc = fortInfo.indexOf(':');
					String group = fortInfo.substring(0, colonLoc);
					String orgname = fortInfo.substring(colonLoc+1, dashLoc);
					String mat = fortInfo.substring(dashLoc+1,pipeLoc);
					int fortNum = Integer.parseInt(fortInfo.substring(pipeLoc+1));
					p.sendMessage(ChatColor.translateAlternateColorCodes('&', Organizations.tag+"This block is already fortified with "+mat+"(x"+fortNum+") by "+group+":"+orgname+"."));
				}
			}
			if (!contains){
				Organization org = Organization.getOrganizationByPlayer(plugin.getConfig(), p.getUniqueId(), plugin);
				// Organization has already been checked to ensure it exists.
				String imStr = e.getItem().getType().toString();
				if (fs.containsKey(imStr)){
					int fortVal = fs.get(imStr);
					// Permissions and such are already checked.
					fortInfo = org.getTag()+":"+org.getName()+"-"+imStr+"|"+fortVal;
					SerialLocation nloc = new SerialLocation();
					nloc.setLocation(b.getLocation());
					blockData.put(nloc,fortInfo);
					if (e.getItem().getAmount()<=1){
						p.getInventory().setItemInHand(new ItemStack(Material.AIR));
					} else {
						e.getItem().setAmount(e.getItem().getAmount()-1);
					}
				} else {
					p.sendMessage(ChatColor.translateAlternateColorCodes('&', Organizations.tag+"The material "+e.getItem().getType().toString()+" is not valid for fortification."));
					protPlayers.remove(p);
				}
				e.setCancelled(true);
			}
			
			saveBlockDataToDisk();
		}
	}
	
	
	// TODO Fortification immunity from explosion/water/fire.
	@EventHandler
	public void onBlockBreak(BlockBreakEvent e){
		loadBlockDataFromDisk();
		Block b = e.getBlock();
		Player p = e.getPlayer();
		SerialLocation removalQue = null;
		for (SerialLocation sloc : blockData.keySet()){
			if (sloc.getLocation(plugin).getX() == b.getLocation().getX() && 
					sloc.getLocation(plugin).getY() == b.getLocation().getY() &&
					sloc.getLocation(plugin).getZ() == b.getLocation().getZ() &&
					sloc.getLocation(plugin).getWorld().getName().equalsIgnoreCase(b.getLocation().getWorld().getName())){
				String bData = blockData.get(sloc);
				int dashLoc = bData.indexOf('-');
				int pipeLoc = bData.indexOf('|');
				int colonLoc = bData.indexOf(':');
				if (overridePlayers.contains(p)){
					Organization org = Organization.getOrganizationByPlayer(plugin.getConfig(), p.getUniqueId(), plugin);
					String orgname = bData.substring(colonLoc+1, dashLoc-1);
					String mat = bData.substring(dashLoc+1,pipeLoc-1);
					if (org.getName().equalsIgnoreCase(orgname)){
						Block usb = (Block) b;
						usb.getWorld().dropItemNaturally(usb.getLocation(), new ItemStack(Material.getMaterial(mat), 1));
						return;
					}
				}
				int fortNum = Integer.parseInt(bData.substring(pipeLoc+1));
				fortNum=fortNum-1;
				if (fortNum>0){
					String nonum = bData.substring(0, pipeLoc+1);
					blockData.put(sloc, nonum+fortNum);
					e.setCancelled(true);
				} else {
					removalQue=sloc;
				}
			}
		}
		if (removalQue!=null){
			blockData.remove(removalQue);
		}
		saveBlockDataToDisk();
	}
	
	public void saveBlockDataToDisk(){
		try {
			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(plugin.getDataFolder()+File.separator+"blocks.bin"));
			oos.writeObject(blockData);
			oos.flush();
			oos.close();
		} catch (FileNotFoundException e1) {
			plugin.getLogger().log(Level.WARNING, "The blockdata file was missing. Attempting to create...");
			new File(plugin.getDataFolder()+File.separator+"blocks.bin");
			saveBlockDataToDisk();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@SuppressWarnings("unchecked")
	public void loadBlockDataFromDisk(){
		try {
			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(plugin.getDataFolder()+File.separator+"blocks.bin"));
			Object result = ois.readObject();
			ois.close();
			blockData = (HashMap<SerialLocation,String>)result;
		} catch (FileNotFoundException e1) { 
			plugin.getLogger().log(Level.WARNING, "The blockdata file was missing. Calling data save...");
			saveBlockDataToDisk();
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
}
