package omer.fish.dao.fish;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import omer.fish.dao.BaseDaoImp;
import omer.fish.model.data.AnimatedEntity;

/**
 * The Fish DAO layer. Saves Fish data in the DB.
 * @author Omer
 *
 */
@Repository("fishDao")
@Transactional
public class FishDaoImp extends BaseDaoImp implements FishDao {
	// private static Logger log = Logger.getLogger(FishDaoImp.class);

	/**
	 * Saves lists but first checks which of its members have been deleted from the db and ignores those

	 */
	@Transactional
	public List<AnimatedEntity> saveIfNotDeleted(List<AnimatedEntity> fishes) {
		List<AnimatedEntity> nonDeletedFishes = new ArrayList<AnimatedEntity>();
		for (AnimatedEntity fish: fishes) {
			// once did a find() on the fish, got to use that object b/c it's associated w/the session. can't use the passed-in fish...
			AnimatedEntity temp = find(AnimatedEntity.class, fish.getId());
			if (temp != null) {
				temp.setMoveDelay(fish.getMoveDelay());
				temp.setExtension(fish.getExtension());
				temp.setH(fish.getH());
				temp.setSubType(fish.getSubType());
				temp.setMovingTill(fish.getMovingTill());
				temp.setPath(fish.getPath());
				temp.setW(fish.getW());
				temp.setX(fish.getX());
				temp.setY(fish.getY());
				temp.setMoveDuration(fish.getMoveDuration());
				temp.setTurnDuration(fish.getTurnDuration());
				temp.setTurnPixLength(fish.getTurnPixLength());
				temp.setTurnWFactor(fish.getTurnWFactor());
				temp.setType(fish.getType());
				// add the new fish, not the one found in the db
				nonDeletedFishes.add(temp);
			}
		}
		return save(nonDeletedFishes);
	}
}
