package omer.fish.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.annotation.PostConstruct;

import omer.fish.model.data.AnimatedEntity;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Updates the fish location on a configurable timer. Updates the database also on a configurable timer.
 * @author Omer
 *
 */
@Service("autoFishMover")
public class AutoFishMoverImp implements AutoFishMover, Runnable {
	private static Logger log = Logger.getLogger(AutoSchoolFishMoverImp.class);

	// memory map for keeping current Fish info
	private Map<String, AnimatedEntity> fishMap = new HashMap<String, AnimatedEntity>();
    @Autowired
    @Qualifier("fishManager")
    private FishManager fishManager;
	// wire-in properties from fish.properties
	@Value("${fish.ms.update.interval}") 
	private String fishUpdateIntervalString;
	private int fishUpdateInterval;
	@Value("${auto.move}")
	private String autoMoveString;
	private boolean autoMove = false;
	@Value("${fish.tank.w}")
	private String tankWidthString;
	private int tankWidth = 1600;
	@Value("${fish.tank.h}")
	private String tankHeightString;
	private int tankHeight = 900;
	@Value("${fish.move.max.ms.delay}")
	private String delayString;
	private int delay = 2000;
	protected Thread packageThread;
    
    @PostConstruct
    public void init() {
    	// load fish from db
		for (AnimatedEntity fish: fishManager.getAllFish()) {
			if (fish.getType().equalsIgnoreCase("FISH") == true || fish.getType().equalsIgnoreCase("SCHOOL") == true) {
				this.fishMap.put(fish.getUniqueIdName(), fish);
			}
		}
		this.autoMove = true;
		try {
			this.fishUpdateInterval = Integer.parseInt(this.fishUpdateIntervalString);
		}
		catch (Exception e) {
			this.fishUpdateInterval = 5000;
		}
		try {
			this.tankWidth = Integer.parseInt(this.tankWidthString);
		}
		catch (Exception e) {
			this.tankWidth = 1600;
		}
		try {
			this.tankHeight = Integer.parseInt(this.tankHeightString);
		}
		catch (Exception e) {
			this.tankHeight = 900;
		}
		try {
			this.delay = Integer.parseInt(this.delayString);
		}
		catch (Exception e) {
			this.delay = 900;
		}
    	if (this.autoMoveString.equalsIgnoreCase("true")) {
			// start thread
			this.packageThread = new Thread(this);
			this.packageThread.start();
    	}
    	else {
    		this.autoMove = false;
    	}
    }
    
	public boolean isAutoMove() {
		return autoMove;
	}

	public void setAutoMove(boolean autoMove) {
		this.autoMove = autoMove;
	}

	public Map<String, AnimatedEntity> getFishMap() {
		return fishMap;
	}

	public List<AnimatedEntity> getFishList() {
		List<AnimatedEntity> temp = new ArrayList<AnimatedEntity>();
		temp.addAll(fishMap.values());
		return temp;
	}
	public void fishWasDeleted(AnimatedEntity fish) {
		this.fishMap.remove(fish.getUniqueIdName());
	}

	public void fishWasAdded(AnimatedEntity fish) {
		if (fish.getType().equalsIgnoreCase("FISH") == true || fish.getType().equalsIgnoreCase("SCHOOL") == true) {
			this.fishMap.put(fish.getUniqueIdName(), fish);
		}
	}

	public void updateFishLocation(AnimatedEntity fish) {
		// TODO: if (this.fishMap.get(fish.getName()) == null) throw new FishException();
		if (fish.getType().equalsIgnoreCase("FISH") == true || fish.getType().equalsIgnoreCase("SCHOOL") == true) {
			this.fishMap.get(fish.getUniqueIdName()).setX(fish.getX());
			this.fishMap.get(fish.getUniqueIdName()).setY(fish.getY());
			this.fishMap.get(fish.getUniqueIdName()).setMoveDelay(0);
		}
	}

	private void setPosition(AnimatedEntity fish) {
		// fish is ready to be moved. set its new position
		int x = Math.abs(new Random().nextInt() % this.tankWidth);
		if ((x + fish.getW()) > this.tankWidth) x = this.tankWidth - fish.getW();
		int y = Math.abs(new Random().nextInt() % this.tankHeight);
		if ((y + fish.getH()) > this.tankHeight) y = this.tankHeight - fish.getH();
		log.debug(fish.getUniqueIdName() + ": " + x + ", " + y);
		fish.setX(x);
		fish.setY(y);
		// get a random delay
		int delay = new Random().nextInt() % this.delay;
		delay = delay < 0 ? -delay : delay;
		fish.setMoveDelay(delay);
	}

	public void run() {
		log.debug("run() is called");
		while (true) {
			try {
				// sleep first, then move the fish
				Thread.sleep(this.fishUpdateInterval);
				
				// get a random fish/school. if not busy (was moved by user and is trying to get to its destination), give it some new coords.
				// try to find a non-moving fish list-size times and not more. when found a non-moving one, move it and break out of the loop.
				// We move whole schools like they were a single fish. We don't move a shcool's fish here. This is done in
				// AutoSchoolFishMover
				for (int i = 0; i < this.fishMap.size(); i++) {
					int fishIndex = new Random().nextInt(this.fishMap.size());
					AnimatedEntity fish = (AnimatedEntity)this.fishMap.values().toArray()[fishIndex];
					if (fish.getMovingTill() < new Date().getTime()) {
						// omer: temp
						setPosition(fish);
					}
				}
			}
			catch (InterruptedException ie) {
			}
		}
	}
}
