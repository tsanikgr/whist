package com.tsanikgr.whist_multiplayer.models;

public class AccomplishmentsModel extends JsonSerialisable<AccomplishmentsModel> {

	public static final transient int PLAYA = 0;
	public static final transient int FISH = 1;
	public static final transient int LARGE = 2;
	public static final transient int HUGE = 3;
	public static final transient int PRO = 4;
	public static final transient int NOOB = 5;
	public static final transient int APPRENTICE = 6;

	//whenever the cache is updated, the outbox is updated as well
	private final Accomplishments cache;
	private final Accomplishments outbox;

	public AccomplishmentsModel() {
		super(AccomplishmentsModel.class);
		cache = new Accomplishments();
		outbox = new Accomplishments();
	}

	public boolean isOutboxEmpty(){
		if (outbox.playa ||
				outbox.fish ||
				outbox.large ||
				outbox.huge ||
				outbox.pro ||
				outbox.noob ||
				outbox.apprentice) return false;
		if (outbox.highScore != -1 ||
				outbox.eloScore != -1) return false;

		return true;
	}

	//update cache + outbox
	public boolean unlockAchievement(int achievementId) {
		switch (achievementId) {
			case PLAYA:
				if (cache.playa) return false;
				cache.playa = true;
				outbox.playa = true;
				break;
			case FISH:
				if (cache.fish) return false;
				cache.fish = true;
				outbox.fish = true;
				break;
			case LARGE:
				if (cache.large) return false;
				cache.large = true;
				outbox.large = true;
				break;
			case HUGE:
				if (cache.huge) return false;
				cache.huge = true;
				outbox.huge = true;
				break;
			case PRO:
				if (cache.pro) return false;
				cache.pro = true;
				outbox.pro = true;
				break;
			case NOOB:
				if (cache.noob) return false;
				cache.noob = true;
				outbox.noob = true;
				break;
			case APPRENTICE:
				if (cache.apprentice) return false;
				cache.apprentice = true;
				outbox.apprentice = true;
				break;
 		}
		return true;
	}

	//update cache + outbox
	public boolean newHighScore(long maximumScore) {
		if (cache.highScore >= maximumScore) return false;
		cache.highScore = maximumScore;
		outbox.highScore = maximumScore;
		return true;
	}

	//update cache + outbox
	public boolean newHighELO(int maximumELO) {
		if (cache.eloScore >= maximumELO) return false;
		cache.eloScore = maximumELO;
		outbox.eloScore = maximumELO;
		return true;
	}

	//update only outbox
	public long getMaxScoreAndClearFromOutbox(){
		long hs = outbox.highScore;
		outbox.highScore = -1;
		return hs;
	}

	//update only outbox
	public long getMaxELOAndClearFromOutbox(){
		long es = outbox.eloScore;
		outbox.eloScore = -1;
		return es;
	}

	//update only outbox
	public String getNextAchievementAndClearFromOutbox() {
		if (outbox.playa) {
			outbox.playa = false;
			return "playa";
		}
		if (outbox.fish) {
			outbox.fish = false;
			return "fish";
		}
		if (outbox.large) {
			outbox.large = false;
			return "large";
		}
		if (outbox.huge) {
			outbox.huge = false;
			return "huge";
		}
		if (outbox.pro) {
			outbox.pro = false;
			return "pro";
		}
		if (outbox.noob) {
			outbox.noob = false;
			return "noob";
		}
		if (outbox.apprentice) {
			outbox.apprentice = false;
			return "apprentice";
		}
		return null;
	}

	private static class Accomplishments {
		private long highScore, eloScore;
		private boolean playa, fish, large, huge, pro, noob, apprentice;

		private Accomplishments() {
			clear();
		}

		private void clear() {
			highScore = -1;
			eloScore = -1;
			playa = false;
			fish = false;
			large = false;
			huge = false;
			pro = false;
			noob = false;
			apprentice = false;
		}
	}
}
