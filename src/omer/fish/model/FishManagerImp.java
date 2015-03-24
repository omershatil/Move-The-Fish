package omer.fish.model;

import java.util.List;

import omer.fish.dao.fish.FishDao;
import omer.fish.model.data.AnimatedEntity;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

/**
 * @author Omer
 *
 */
@Service("fishManager")
public class FishManagerImp implements FishManager {
	private static Logger log = Logger.getLogger(FishManagerImp.class);
	
	@Autowired
	@Qualifier("fishDao")
	private FishDao fishDao;
    @Autowired
    @Qualifier("autoFishMover")
    private AutoFishMover autoFishMover;
    @Autowired
    @Qualifier("autoSchoolFishMover")
    private AutoFishMover autoSchoolFishMover;
	
	public List<AnimatedEntity> getAllFish() {
		return fishDao.findAll(AnimatedEntity.class);
	}
	public void updateFishLocation(AnimatedEntity fish) {
		this.fishDao.save(fish);
	}
	public void updateAllFishLocation(List<AnimatedEntity> fishes) {
		// call saveIfNotDeleted() in case the user has already deleted this fish and the AutoFishMover doesn't yet know about it.
		// Thus we can avoid race conditions and need to synchronize deletion w/update.
		this.fishDao.saveIfNotDeleted(fishes);
	}
	public void saveFish(AnimatedEntity fish) {
		this.fishDao.save(fish);
	}
	public AnimatedEntity createRandomFish(int x, int y, String imgName) {
		// TODO: should create a table in db that has 'templates' of fish and create the new fish out of those.
		String type = "FISH";
		// TODO: for the mean time, if it's a discus, make it a school fish.
		if (imgName.equalsIgnoreCase("discus_l")) {
			type = "SCHOOL_FISH";
		}
		int width = 120;
		int height = 90;
		if (type == "SCHOOL_FISH") {
			width = 30;
			height = 20;
		}
		AnimatedEntity fish = new AnimatedEntity(imgName, "gif", "images", x,  y, width, height, 0, 2000, 2000, 100, 4, 1000, type);
		this.fishDao.save(fish);
		// now inform AutoFishMover
		if (type == "SCHOOL_FISH") {		
			this.autoSchoolFishMover.fishWasAdded(fish);
			// TODO: cleanup. souldn't have to do this...
			// return a copy b/c fishWasAdded will change the position of 'fish' and that would effect the position that the jsp puts it
			long id = fish.getId();
			fish = new AnimatedEntity(imgName, "gif", "images", x,  y, width, height, 0, 2000, 2000, 100, 4, 1000, type);
			fish.setId(id);
		}
		else {
			this.autoFishMover.fishWasAdded(fish);
		}
		return fish;
	}
	public void deleteFish(AnimatedEntity fish) {
		try {
			this.fishDao.delete(fish);
		}
		// use the spring's hibernate catch-all exception
		catch (DataAccessException e) {
			log.warn("Failed to delete. Most likely has already been previously deleted..");
			log.debug(e + e.getMessage());
		}
		String type = "FISH";
		// TODO: for the mean time, if it's a discus, make it a school fish.
		if (fish.getSubType().equalsIgnoreCase("discus_l")) {
			type = "SCHOOL_FISH";
		}
		// now inform the AutoFishMover. Note that thanks to  saveIfNotDeleted() we don't need to worry about synchronization
		if (type == "SCHOOL_FISH") {		
			this.autoSchoolFishMover.fishWasDeleted(fish);
		}
		else {
			this.autoFishMover.fishWasDeleted(fish);
		}
	}
}
