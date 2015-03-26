package omer.fish.model;

import javax.annotation.PostConstruct;

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
@Service("autoDBUpdate")
public class AutoDBUpdateImp implements AutoDBUpdate, Runnable {
	private static Logger log = Logger.getLogger(AutoDBUpdate.class);

    @Autowired
    @Qualifier("autoFishMover")
    private AutoFishMover autoFishMover;
    @Autowired
    @Qualifier("autoSchoolFishMover")
    private AutoFishMover autoSchoolFishMover;
    @Autowired
    @Qualifier("fishManager")
    private FishManager fishManager;
	// wire-in properties from fish.properties
	@Value("${db.ms.update.interval}")
	private String dbUpdateIntervalString;
	private int dbUpdateInterval;
	@Value("${auto.move}")
	private String autoMoveString;
	private boolean autoMove = false;
	protected Thread packageThread;
    
    @PostConstruct
    public void init() {
    	if (this.autoMoveString.equalsIgnoreCase("true")) {
    		this.autoMove = true;
			try {
				this.dbUpdateInterval = Integer.parseInt(this.dbUpdateIntervalString);
			}
			catch (Exception e) {
				this.dbUpdateInterval = 5000;
			}
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

	public void run() {
		log.debug("run() is called");
		while (true) {
			try {
				// sleep first. then do the update
				Thread.sleep(this.dbUpdateInterval);
				this.fishManager.updateAllFishLocation(this.autoFishMover.getFishList());
				this.fishManager.updateAllFishLocation(this.autoSchoolFishMover.getFishList());
			}
			catch (InterruptedException ie) {
			}
		}
	}
}
