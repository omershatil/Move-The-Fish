package omer.fish.model.data;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

/**
 * Represents a fish or a school of fish, or whatever I may add later to be animated. Note that it's used across the MVC
 * and is sent to the front end (henced the Serializable interface).
 * @author Omer
 *
 */
@Entity
public class AnimatedEntity implements Serializable {
	private static final long serialVersionUID = -598666277228837861L;
	@Id
	@GeneratedValue
	private long id;
	// TODO: change to enum
	private String type;
	private String subType;
	private String extension;
	// Defaults to varchar(256). Set to text, so length is not limited. Could also make a varchar of up to 65535 
	@Column(columnDefinition="TEXT")
	private String path;
	// NOTE: x,y values are relative to the parent main div, not to doc!
	private int x;
	private int y;
	private int w;
	private int h;
	// gets set only if a user moved this fish. Used to determine when the fish is free to be moved automatically by server
	private long movingTill = 0;
	// within how much time should the fish move to its new location
	private int moveDuration;
	// within how much time should the fish turn around
	private int turnDuration;
	// how big of a turn should the fish make
	private int turnPixLength;
	// to create the turning effect we narrow down the fish and then widen it again once we flip the image. This factor
	// determines how thin should the fish become before flipping
	private int turnWFactor;
	// create a random delay, so that not all fish start moving at the exact same time. max delay is set in fish.move.max.ms.delay
	private int moveDelay = 0;
	
	public AnimatedEntity() {};
	
	public AnimatedEntity(String subType, String extension, String path, int x, int y,
			int w, int h, long movingTill, int moveDuration, int turnDuration,
			int turnPixLength, int turnWFactor, int moveDelay, String type) {
		super();
		this.subType = subType;
		this.extension = extension;
		this.path = path;
		this.x = x;
		this.y = y;
		this.w = w;
		this.h = h;
		this.movingTill = movingTill;
		this.moveDuration = moveDuration;
		this.turnDuration = turnDuration;
		this.turnPixLength = turnPixLength;
		this.turnWFactor = turnWFactor;
		this.moveDelay = moveDelay;
		this.type = type;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getUniqueIdName() {
		return new StringBuilder(this.subType).append("_").append(this.id).toString();
	}
	
	public String getSubType() {
		return subType;
	}

	public void setSubType(String subType) {
		this.subType = subType;
	}

	public String getExtension() {
		return extension;
	}

	public void setExtension(String extension) {
		this.extension = extension;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public int getX() {
		return x;
	}
	public void setX(int x) {
		this.x = x;
	}
	public int getY() {
		return y;
	}
	public void setY(int y) {
		this.y = y;
	}

	public int getW() {
		return w;
	}

	public void setW(int w) {
		this.w = w;
	}

	public int getH() {
		return h;
	}

	public void setH(int h) {
		this.h = h;
	}

	public long getMovingTill() {
		return movingTill;
	}

	public void setMovingTill(long movingTill) {
		this.movingTill = movingTill;
	}

	public int getMoveDuration() {
		return moveDuration;
	}

	public void setMoveDuration(int moveDuration) {
		this.moveDuration = moveDuration;
	}

	public int getTurnDuration() {
		return turnDuration;
	}

	public void setTurnDuration(int turnDuration) {
		this.turnDuration = turnDuration;
	}

	public int getTurnPixLength() {
		return turnPixLength;
	}

	public void setTurnPixLength(int turnPixLength) {
		this.turnPixLength = turnPixLength;
	}

	public int getTurnWFactor() {
		return turnWFactor;
	}

	public void setTurnWFactor(int turnWFactor) {
		this.turnWFactor = turnWFactor;
	}

	public int getMoveDelay() {
		return moveDelay;
	}

	public void setMoveDelay(int moveDelay) {
		this.moveDelay = moveDelay;
	}

	public String getType() {
		return type;
	}

	public void setType(String typeId) {
		this.type = typeId;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((extension == null) ? 0 : extension.hashCode());
		result = prime * result + h;
		result = prime * result + (int) (id ^ (id >>> 32));
		result = prime * result + ((subType == null) ? 0 : subType.hashCode());
		result = prime * result + moveDelay;
		result = prime * result + moveDuration;
		result = prime * result + (int) (movingTill ^ (movingTill >>> 32));
		result = prime * result + ((path == null) ? 0 : path.hashCode());
		result = prime * result + turnDuration;
		result = prime * result + turnPixLength;
		result = prime * result + turnWFactor;
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		result = prime * result + w;
		result = prime * result + x;
		result = prime * result + y;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AnimatedEntity other = (AnimatedEntity) obj;
		if (extension == null) {
			if (other.extension != null)
				return false;
		} else if (!extension.equals(other.extension))
			return false;
		if (h != other.h)
			return false;
		if (id != other.id)
			return false;
		if (subType == null) {
			if (other.subType != null)
				return false;
		} else if (!subType.equals(other.subType))
			return false;
		if (moveDelay != other.moveDelay)
			return false;
		if (moveDuration != other.moveDuration)
			return false;
		if (movingTill != other.movingTill)
			return false;
		if (path == null) {
			if (other.path != null)
				return false;
		} else if (!path.equals(other.path))
			return false;
		if (turnDuration != other.turnDuration)
			return false;
		if (turnPixLength != other.turnPixLength)
			return false;
		if (turnWFactor != other.turnWFactor)
			return false;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		if (w != other.w)
			return false;
		if (x != other.x)
			return false;
		if (y != other.y)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "AnimatedEntity [id=" + id + ", subType=" + subType
				+ ", extension=" + extension + ", path=" + path + ", x=" + x
				+ ", y=" + y + ", w=" + w + ", h=" + h + ", movingTill="
				+ movingTill + ", moveDuration=" + moveDuration
				+ ", turnDuration=" + turnDuration + ", turnPixLength="
				+ turnPixLength + ", turnWFactor=" + turnWFactor
				+ ", moveDelay=" + moveDelay + ", type=" + type + "]";
	}

}
