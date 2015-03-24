package omer.fish.dao.fish;

import java.util.List;

import omer.fish.dao.BaseDao;
import omer.fish.model.data.AnimatedEntity;

public interface FishDao extends BaseDao {
	public List<AnimatedEntity> saveIfNotDeleted(List<AnimatedEntity> fishes);
}
