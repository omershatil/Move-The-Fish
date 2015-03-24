package omer.fish.model;

import java.util.List;
import java.util.Map;

import omer.fish.model.data.AnimatedEntity;

public interface AutoFishMover {
	public Map<String, AnimatedEntity> getFishMap();
	public List<AnimatedEntity> getFishList();
	public void updateFishLocation(AnimatedEntity fish);
	public void fishWasDeleted(AnimatedEntity fish);
	public void fishWasAdded(AnimatedEntity fish);
}
