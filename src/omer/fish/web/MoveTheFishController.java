package omer.fish.web;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import omer.fish.model.AutoFishMover;
import omer.fish.model.FishManager;
import omer.fish.model.data.AnimatedEntity;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class MoveTheFishController {
	// private static Logger log = Logger.getLogger(MoveTheFishController.class);
	// TODO: autofishmover should probably be accessed via the fishmanager... 
    @Autowired
    @Qualifier("autoFishMover")
    private AutoFishMover autoFishMover;
    @Autowired
    @Qualifier("autoSchoolFishMover")
    private AutoFishMover autoSchoolFishMover;
    @Autowired
    @Qualifier("fishManager")
    private FishManager fishManager;
    
    @PostConstruct
    public void init() {
    }
    
    /**
     * Get Move The Fish page.
     * @param username
     * @param model
     * @return
     */
    @RequestMapping("movethefish.htm*")
    public String setupForm(
    		// RequestParam is an http request param. We are asking Spring to pass it in to this function
            @RequestParam(required = false, value = "username") String username,
            ModelMap model) {
    	model.addAttribute("fishes", this.fishManager.getAllFish());
        return "movethefish";
    }

    /**
     * POST fish location data. Note the body type is List<String>, these are JSON objects in a list
     * @param username
     */
    @RequestMapping(value = "fishLocation", method = RequestMethod.POST, headers ="Content-Type=application/json")
    public @ResponseBody void setFishLocation(@RequestBody AnimatedEntity movedFish, Model model) {
    	// only allow non-school fish to be moved. if a school fish, let it go back to its school...
    	AnimatedEntity fish = this.autoFishMover.getFishMap().get(movedFish.getUniqueIdName());
    	if (fish != null) {
    		fish.setX(movedFish.getX());
    		fish.setY(movedFish.getY());
    		// no delay, as it was a user moving the fish
    		fish.setMoveDelay(0);
   			this.autoFishMover.updateFishLocation(fish);
    	}
    }

    /**
     * Delete a fish.
     * @param fish
     * @param model
     */
    @RequestMapping(value = "deletefish", method = RequestMethod.POST, headers ="Content-Type=application/json")
    public @ResponseBody void deleteFish(@RequestBody AnimatedEntity fish, Model model) {
    	this.fishManager.deleteFish(fish);
    }

    /**
     * create a new fish.
     * @param fish
     * @param model
     * @return
     */
    @RequestMapping(value = "createfish", method = RequestMethod.POST, headers ="Content-Type=application/json")
    public @ResponseBody AnimatedEntity createFish(@RequestBody AnimatedEntity fish, Model model) {
    	return this.fishManager.createRandomFish(fish.getX(), fish.getY(), fish.getSubType());
    }

    /**
     * Get all fish location data.
     * @param username
     */
    @RequestMapping(value = "allFishLocation", method = RequestMethod.GET, consumes="application/json")
    public @ResponseBody Collection<AnimatedEntity> getAllFishLocation(ModelMap model) {
    	// log.debug(this.autoFishMover.getFishList());
    	// TODO: wrong. it should take it from memory, from all auto fish movers
    	// List<AnimatedEntity> fishes =  this.fishManager.getAllFish();
    	// TODO: for now, just update whatever fishes you have
    	// Map<String, AnimatedEntity> tempMap = new HashMap<String, AnimatedEntity>();
    	List<AnimatedEntity> fishes = new ArrayList<AnimatedEntity>();
    	for (AnimatedEntity fish: this.autoFishMover.getFishList()) {
    		// tempMap.put(fish.getUniqueIdName(), fish);
    		fishes.add(fish);
    	}
    	// override so that position will be updated. 
    	for (AnimatedEntity fish: this.autoSchoolFishMover.getFishList()) {
    		// tempMap.put(fish.getUniqueIdName(), fish);
    		fishes.add(fish);
    	}
    	return fishes;
    }

}