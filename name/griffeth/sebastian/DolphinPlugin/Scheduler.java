package name.griffeth.sebastian.DolphinPlugin;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

public class Scheduler extends BukkitRunnable {
	
	private Entity ent;
	
	public Scheduler(Entity ent) {
		this.ent = ent;
	}
	
	@Override
	public void run() {
		if(ent.isDead()) {
			cancel();
			return;
		}
		Entity owner = null;
		Iterator<Entity> pas = ent.getPassengers().iterator();
		while(pas.hasNext()) {
			Entity next = pas.next();
			if(DolphinMain.TAMED.get(next.getUniqueId()).contains(ent.getUniqueId().toString())) {
				owner = next;
				break;
			}
		}
		if(owner == null) {
			cancel();
			return;
		}
		if(ent.isInWater()) {
			ent.setVelocity(owner.getLocation().getDirection().multiply(1));
		}
	}
	
}
