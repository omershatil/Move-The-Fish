package omer.fish.dao.fish;

import java.util.ArrayList;
import java.util.List;

import omer.fish.model.FishManager;
import omer.fish.model.data.AnimatedEntity;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import junit.framework.TestCase;

public class TestFishDaoImp extends TestCase {
	ApplicationContext applicationContext = null;
    @Autowired
    @Qualifier("fishManager")
    FishManager fishManager;
    private List<AnimatedEntity> fishes = new ArrayList<AnimatedEntity>();
    
	public void setUp() {
		applicationContext = new ClassPathXmlApplicationContext("application-context.xml");
		this.fishManager = (FishManager)applicationContext.getBean("fishManager");
		System.out.println("test" + applicationContext.getBean("fishDao"));
    	if (fishManager.getAllFish().size() == 0) {
	    	AnimatedEntity fish2 = new AnimatedEntity("discus_l", "gif", "images", 200, 100, 124, 136, 0, 8000, 2000, 100, 10, 1000, "SCHOOL_FISH");
	    	AnimatedEntity fish3 = new AnimatedEntity("bass_r", "gif", "images", 200, 300, 137, 40, 0, 8000, 2000, 100, 8, 1000, "FISH");
	    	AnimatedEntity fish4 = new AnimatedEntity("green_l", "gif", "images", 300, 200, 80, 52, 0, 8000, 2000, 100, 6, 1000, "FISH");
	    	AnimatedEntity fish5 = new AnimatedEntity("shark_l", "gif", "images", 300, 200, 200, 160, 0, 8000, 2000, 100, 4, 1000, "FISH");
	    	AnimatedEntity fish7 = new AnimatedEntity("trout_l", "gif", "images", 300, 200, 195, 91, 0, 8000, 2000, 100, 8, 1000, "FISH");
	    	AnimatedEntity school1 = new AnimatedEntity("discus_l_school", "", "", 350, 230, 240, 200, 0, 8000, 2000, 100, 8, 1000, "SCHOOL");
	    	
	    	this.fishes.add(fish2);
	    	this.fishes.add(fish3);
	    	this.fishes.add(fish4);
	    	this.fishes.add(fish5);
	    	this.fishes.add(fish7);
	    	this.fishes.add(school1);
	    	
	    	fishManager.saveFish(fish2);
	    	fishManager.saveFish(fish3);
	    	fishManager.saveFish(fish4);
	    	fishManager.saveFish(fish5);
	    	fishManager.saveFish(fish7);
	    	fishManager.saveFish(school1);
    	}
    	else {
    		fishes = fishManager.getAllFish();
    	}
	}
	public void testDao() {
		
	}
}
