package exerelin.campaign.intel.groundbattle;

import java.util.ArrayList;
import java.util.List;

public class GroundBattleAI {
	
	public static float MIN_MORALE_TO_REDEPLOY = 0.35f;
	
	protected GroundBattleIntel intel;
	protected GroundBattleSide side;
	protected boolean isAttacker;
	protected boolean isPlayer;
	
	/*
		AI concepts:
		- industries that need shoring up
		- available assets to move, sorted
			- strength of each
			- not too low morale
		- industries that are lost (not enough assets to reinforce)
	*/
	
	protected List<IndustryForBattle> getIndustriesWithEnemyPresence() {
		List<IndustryForBattle> results = new ArrayList<>();
		for (IndustryForBattle ifb : intel.getIndustries()) {
			if (ifb.containsEnemyOf(isAttacker))
				results.add(ifb);
		}
		return results;
	}
	
	protected List<IndustryForBattle> getThreatenedIndustries(List<IndustryForBattle> toCheck) {
		List<IndustryForBattle> results = new ArrayList<>();
		for (IndustryForBattle ifb : toCheck) {
			// TODO
		}
		return results;
	}
	
	protected List<GroundUnit> getAvailableUnits() {
		List<GroundUnit> results = new ArrayList<>();
		for (GroundUnit unit : side.getUnits()) {
			if (unit.getMorale() < MIN_MORALE_TO_REDEPLOY) continue;
			if (unit.isReorganizing() || unit.isAttackPrevented()) continue;
			results.add(unit);
		}
		return results;
	}
	
	
	
	public void giveOrders() {
		List<GroundUnit> available = getAvailableUnits();
		List<IndustryForBattle> contested = getIndustriesWithEnemyPresence();
		
		//for ()
	}
			
	protected boolean canUnleashMilitia() {
		return intel.turnNum > 15 + intel.getMarket().getSize() * 4;
	}
	
	public static class IFBStrengthRecord {
		public IndustryForBattle industry;
		public float ourStr;
		public float theirStr;
		
		public IFBStrengthRecord(IndustryForBattle industry, float ourStr, float theirStr) {
			
		}
		
	}
}