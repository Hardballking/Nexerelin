package exerelin.campaign.intel.rebellion;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.RepLevel;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.events.CampaignEventPlugin;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import exerelin.campaign.DiplomacyManager;
import exerelin.campaign.PlayerFactionStore;
import exerelin.campaign.SectorManager;
import exerelin.utilities.ExerelinConfig;
import exerelin.utilities.ExerelinUtils;
import exerelin.utilities.ExerelinUtilsFaction;
import exerelin.utilities.ExerelinUtilsMarket;
import java.util.List;
import org.lazywizard.lazylib.MathUtils;

/**
 * Periodically starts rebellions on suitably restive planets.
 */
public class RebellionCreator implements EveryFrameScript {
	
	public static final String MEMORY_KEY_REBELLION_POINTS = "$nex_rebellionPoints";
	public static final String PERSISTENT_DATA_KEY = "nex_rebellionCreator";
	public static final float REBELLION_POINT_MULT = 0.2f;
	public static final float HARD_MODE_MULT = 1.25f;
	public static final float HARD_MODE_STABILITY_MODIFIER = -2;
	public static final float MAX_ONGOING = 4;
	
	protected IntervalUtil interval = new IntervalUtil(1,1);
	protected transient int numOngoing;
	
	public static RebellionCreator generate() {
		RebellionCreator creator = new RebellionCreator();
		Global.getSector().getPersistentData().put(PERSISTENT_DATA_KEY, creator);
		Global.getSector().addScript(creator);
		return creator;
	}
	
	public static RebellionCreator getInstance() {
		return (RebellionCreator)Global.getSector().getPersistentData().get(PERSISTENT_DATA_KEY);
	}
	
	public RebellionIntel createRebellion(MarketAPI market, String factionId, boolean instant)
	{
		if (RebellionIntel.isOngoing(market))
			return null;
		
		float prepTime = market.getSize() * 2 * MathUtils.getRandomNumberInRange(0.8f, 1.2f);
		if (RebellionIntel.DEBUG_MODE) prepTime = 1;
		
		FactionAPI rebel = Global.getSector().getFaction(factionId);
		RebellionIntel intel = new RebellionIntel(market, rebel, prepTime);
		intel.init(instant);
		
		return intel;
	}
	
	protected static void addToListIfNotPresent(List<String> list, String toAdd)
	{
		if (list.contains(toAdd)) return;
		list.add(toAdd);
	}
	
	public RebellionIntel createRebellion(MarketAPI market, boolean instant)
	{
		if (RebellionIntel.isOngoing(market))
			return null;
		FactionAPI faction = market.getFaction();
		boolean allowPirates = ExerelinConfig.retakePirateMarkets;		
		
		WeightedRandomPicker<String> enemyPicker = new WeightedRandomPicker<>();
		List<String> enemies = DiplomacyManager.getFactionsOfAtBestRepWithFaction(market.getFaction(), 
				RepLevel.INHOSPITABLE, allowPirates, false, false);
		
		if (allowPirates && faction.isHostileTo(Factions.INDEPENDENT))
		{
			addToListIfNotPresent(enemies, Factions.INDEPENDENT);
		}
		
		String originalOwner = ExerelinUtilsMarket.getOriginalOwner(market);
		for (String candidate : enemies)
		{
			float weight = 1;
			if (faction.isAtBest(candidate, RepLevel.VENGEFUL))
				weight += 2;
			//if (ExerelinUtilsFaction.isPirateFaction(candidate))
			//	weight += 1;
			if (candidate.equals(Factions.INDEPENDENT))
				weight += 2;
			if (candidate.equals(originalOwner))
				weight += 10;
			if (market.hasCondition(Conditions.LUDDIC_MAJORITY))
			{
				if (candidate.equals(Factions.LUDDIC_PATH))
					weight += 5;
				else if (candidate.equals(Factions.LUDDIC_CHURCH))
					weight += 3;
			}
			
			enemyPicker.add(candidate, weight);
		}
		
		String enemyId = enemyPicker.pick();
		if (enemyId == null)
			return null;
		return createRebellion(market, enemyId, instant);
	}
	
	/**
	 * How many rebellion points should this market get today?
	 * @param market
	 * @return
	 */
	protected float getRebellionIncrement(MarketAPI market)
	{
		int effectiveStability = (int)market.getStabilityValue();
		int size = market.getSize();
		String factionId = market.getFactionId();
		boolean hardModePenalty = SectorManager.getManager().isHardMode()
				&& (factionId.equals(PlayerFactionStore.getPlayerFactionId()) || factionId.equals(Factions.PLAYER));
		
		if (market.hasCondition(Conditions.DISSIDENT))
			effectiveStability -= 1;
		if (ExerelinUtilsMarket.isWithOriginalOwner(market))
			effectiveStability += 1;
		else
			effectiveStability -= 2;		
		if (hardModePenalty)
			effectiveStability -= HARD_MODE_STABILITY_MODIFIER;
		
		int requiredThreshold = 4;
		if (requiredThreshold < 0) requiredThreshold = 0;
		
		float points = (requiredThreshold - effectiveStability)/2;
		if (points > 2) points = 2;
		else if (points < -2) points = -2;
		points *= REBELLION_POINT_MULT * ExerelinConfig.rebellionMult;
		if (hardModePenalty && points > 0) points *= HARD_MODE_MULT;
		return points;		
	}
	
	public float getRebellionPoints(MarketAPI market)
	{
		Float points = market.getMemoryWithoutUpdate().getFloat(MEMORY_KEY_REBELLION_POINTS);
		return points;
	}
	
	protected void incrementRebellionPoints(MarketAPI market, float points)
	{
		float currPoints = getRebellionPoints(market);
		if (currPoints == 0 && points <= 0) return;
		
		currPoints += points;
		if (currPoints <= 0)
		{
			market.getMemoryWithoutUpdate().unset(MEMORY_KEY_REBELLION_POINTS);
			return;
		}
		if (currPoints >= 100)
		{
			createRebellion(market, false);
			currPoints = 0;
		}
		market.getMemoryWithoutUpdate().set(MEMORY_KEY_REBELLION_POINTS, currPoints);
	}
	
	protected void processMarket(MarketAPI market, float days)
	{
		if (!ExerelinUtilsMarket.canBeInvaded(market, false))
			return;
		if (market.getFactionId().equals("templars"))
			return;
		if (market.getFactionId().equals(Factions.INDEPENDENT))
			return;
		if (ExerelinUtilsFaction.isPirateFaction(market.getFactionId()))
			return;
		if (RebellionIntel.isOngoing(market))
			return;
		
		float points = getRebellionIncrement(market) * days;
		if (points > 0)
		{
			float ongoingMult = 1 - (numOngoing/MAX_ONGOING);
			points *= ongoingMult;
		}
		if (points != 0) return;
		
		incrementRebellionPoints(market, points);
	}
	
	@Override
	public void advance(float amount) {
		ExerelinUtils.advanceIntervalDays(interval, amount);
		if (!interval.intervalElapsed()) return;
		
		numOngoing = Global.getSector().getIntelManager().getIntelCount(RebellionIntel.class, true);
		for (MarketAPI market : Global.getSector().getEconomy().getMarketsCopy())
		{
			processMarket(market, interval.getElapsed());
		}
	}
	
	@Override
	public boolean isDone() {
		return false;
	}

	@Override
	public boolean runWhilePaused() {
		return false;
	}
}
