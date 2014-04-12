package me.capit.Organizations;

import java.io.Serializable;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;

// I had to create this class because Location is not serializable and thus cannot be written to disk.
public class SerialLocation implements Serializable {
	private static final long serialVersionUID = 4200472796408840628L;
	private double x, y, z;
	private String world;
	
	public SerialLocation(Location loc){
		setLocation(loc);
	}
	
	public SerialLocation(){
		setLocation(null);
	}
	
	public Location getLocation(Plugin plugin) {
		World nworld = plugin.getServer().getWorld(world);
		return new Location(nworld, x, y, z);
	}

	public void setLocation(Location loc) {
		if (loc!=null){
			x = loc.getX();
			y = loc.getY();
			z = loc.getZ();
			world = loc.getWorld().getName();
		}
	}

	public double getX() {
		return x;
	}

	public void setX(double x) {
		this.x = x;
	}

	public double getY() {
		return y;
	}

	public void setY(double y) {
		this.y = y;
	}

	public double getZ() {
		return z;
	}

	public void setZ(double z) {
		this.z = z;
	}

	public String getWorld() {
		return world;
	}

	public void setWorld(String world) {
		this.world = world;
	}
	
	public boolean equals(SerialLocation loc){
		if (loc.getWorld().equalsIgnoreCase(world)){
			if (loc.getX()==x && loc.getY()==y && loc.getZ()==z){
				return true;
			}
		}
		return false;
	}

}
