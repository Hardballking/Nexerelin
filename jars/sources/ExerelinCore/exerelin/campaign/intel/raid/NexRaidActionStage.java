package exerelin.campaign.intel.raid;

import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.impl.campaign.intel.raid.PirateRaidActionStage;
import com.fs.starfarer.api.impl.campaign.intel.raid.RaidIntel;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.Nex_MarketCMD;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import exerelin.campaign.econ.RaidCondition;
import java.util.List;

public class NexRaidActionStage extends PirateRaidActionStage {
	
	public NexRaidActionStage(RaidIntel raid, StarSystemAPI system) {
		super(raid, system);
	}
	
	@Override
	public void performRaid(CampaignFleetAPI fleet, MarketAPI market) {
		float raidStr = intel.getRaidFP() / intel.getNumFleets() * Misc.FP_TO_GROUND_RAID_STR_APPROX_MULT;
		if (fleet != null) {
			raidStr = Nex_MarketCMD.getRaidStr(fleet);
		}
		
		float maxPenalty = 3f;
		
		Nex_MarketCMD cmd = new Nex_MarketCMD(market.getPrimaryEntity());
		Industry toDisrupt = pickIndustryToDisrupt(market);
		if (toDisrupt != null)
			cmd.doIndustryRaid(intel.getFaction(), raidStr, toDisrupt, 0.4f);
		else
			cmd.doGenericRaid(intel.getFaction(), raidStr, maxPenalty);
	}
	
	protected Industry pickIndustryToDisrupt(MarketAPI market) {
		WeightedRandomPicker<Industry> picker = new WeightedRandomPicker<>();
		for (Industry industry : market.getIndustries()) {
			if (!industry.canBeDisrupted()) continue;
			if (industry.getSpec().hasTag(Industries.TAG_UNRAIDABLE)) continue;
			float weight = 1;
			if (industry.getDisruptedDays() > 0)
				weight *= 0.5f;
			picker.add(industry, weight);
		}
		return picker.pick();
	}
	
	protected int getNumBombardables(MarketAPI market) {
		int count = 0;
		float dur = Nex_MarketCMD.getBombardDisruptDuration();
		for (Industry ind : market.getIndustries()) {
			if (ind.getSpec().hasTag(Industries.TAG_TACTICAL_BOMBARDMENT)) {
				if (ind.getDisruptedDays() >= dur * 0.8f) continue;
				count++;
			}
		}
		return count;
	}
	
	@Override
	public void giveReturnOrdersToStragglers(List<RouteManager.RouteData> stragglers) {
		super.giveReturnOrdersToStragglers(stragglers);
		RaidCondition.removeRaidFromConditions(system, intel);
	}

	@Override
	public void notifyStarted() {
		super.notifyStarted();
		
		RaidCondition.addOrUpdateConditionsForMarkets(system, intel);
	}
}