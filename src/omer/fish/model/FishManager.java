package omer.fish.model;

import java.util.List;

import omer.fish.model.data.AnimatedEntity;

public interface FishManager {
	public List<AnimatedEntity> getAllFish();
	public void updateFishLocation(AnimatedEntity fish);
	public void updateAllFishLocation(List<AnimatedEntity> fish);
	public void saveFish(AnimatedEntity fish);
	public AnimatedEntity createRandomFish(int x, int y, String imgName);
	public void deleteFish(AnimatedEntity fish);
}
