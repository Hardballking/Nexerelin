package exerelin.campaign.intel.groundbattle;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.econ.CommodityOnMarketAPI;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.ui.CustomPanelAPI;
import com.fs.starfarer.api.ui.IconRenderMode;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI.TooltipCreator;
import com.fs.starfarer.api.ui.TooltipMakerAPI.TooltipLocation;
import com.fs.starfarer.api.ui.UIComponentAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Pair;
import exerelin.campaign.intel.groundbattle.GroundUnit.ForceType;
import exerelin.campaign.intel.groundbattle.plugins.IndustryForBattlePlugin;
import exerelin.utilities.NexUtils;
import exerelin.utilities.StringHelper;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class IndustryForBattle {
	
	public static final int HEIGHT = 100;
	public static final int COLUMN_WIDTH_INDUSTRY = 400;
	//public static final int COLUMN_WIDTH_CONTROLLED_BY = 80;

	public static final int COLUMN_WIDTH_TROOP_ICON = 64;
	public static final int COLUMN_WIDTH_TROOP_NAME = 96;
	public static final int COLUMN_WIDTH_TROOP_COUNT = 48;
	public static final int COLUMN_WIDTH_TROOP_STR = 56;
	public static final int COLUMN_WIDTH_TROOP_BUTTON = 24;
	public static final int COLUMN_WIDTH_TROOP_TOTAL = COLUMN_WIDTH_TROOP_ICON + COLUMN_WIDTH_TROOP_NAME
			+ COLUMN_WIDTH_TROOP_COUNT + COLUMN_WIDTH_TROOP_STR + 2 * COLUMN_WIDTH_TROOP_BUTTON + 16;
	public static final int NUM_ICONS_PER_UNIT = 3;	// 3 icons == 1 platoon/company/etc.

	public static final Map<GroundUnit.ForceType, Integer[]> ICON_COUNTS = new HashMap<>();
	
	protected GroundBattleIntel intel;
	protected Industry ind;
	protected IndustryForBattlePlugin plugin;
	protected List<GroundUnit> units = new LinkedList<>();
	public boolean heldByAttacker = false;

	public IndustryForBattle(GroundBattleIntel intel, Industry ind) {
		this.intel = intel;
		this.ind = ind;
		plugin = IndustryForBattlePlugin.loadPlugin(ind.getId(), this);
	}
	
	public IndustryForBattlePlugin getPlugin() {
		return plugin;
	}
	
	public Industry getIndustry() {
		return ind;
	}
	
	public GroundBattleIntel getIntel() {
		return intel;
	}
		
	public GroundBattleSide getHoldingSide() {
		return intel.getSide(heldByAttacker);
	}
	
	public GroundBattleSide getNonHoldingSide() {
		return intel.getSide(!heldByAttacker);
	}

	public boolean containsEnemyOf(boolean attacker) {
		for (GroundUnit unit : intel.getSide(!attacker).units) {
			if (unit.location != this) continue;
			return true;
		}
		return false;
	}
	
	public boolean isContested() {
		boolean haveAttacker = false;
		boolean haveDefender = false;
		for (GroundUnit unit : units) {
			if (unit.isAttacker) haveAttacker = true;
			else haveDefender = true;
		}
		return haveAttacker && haveDefender;
	}
	
	/**
	 * Check whether this industry should change hands.
	 * @return The new owner (may be same as previous owner).
	 */
	public boolean updateOwner() {
		boolean nowHeldByAttacker = this.heldByAttacker;
		
		boolean haveAttacker = false;
		boolean haveDefender = false;
		boolean autocede = false;	// was this industry held by attackers who then moved on, witout being reoccupied by defenders?
		for (GroundUnit unit : units) {
			if (unit.isAttacker) haveAttacker = true;
			else haveDefender = true;
		}
		if (haveAttacker && !haveDefender) {
			nowHeldByAttacker = true;
		} else if (!haveAttacker) {
			nowHeldByAttacker = false;
			if (!haveDefender) autocede = true;
		}
		
		if (heldByAttacker != nowHeldByAttacker) {
			// TODO: post log message
			plugin.unapply();
			heldByAttacker = nowHeldByAttacker;
			plugin.apply();
			
			if (!autocede) {
				for (GroundUnit unit : intel.getSide(heldByAttacker).units) {
					unit.modifyMorale(GBConstants.CAPTURE_MORALE);
				}
				for (GroundUnit unit : intel.getSide(!heldByAttacker).units) {
					unit.modifyMorale(-GBConstants.CAPTURE_MORALE);
				}
			}
			GroundBattleLog lg = new GroundBattleLog(intel, GroundBattleLog.TYPE_INDUSTRY_CAPTURED, intel.turnNum);
			lg.params.put("industry", this);
			lg.params.put("heldByAttacker", heldByAttacker);
			if (!autocede)
				lg.params.put("morale", GBConstants.CAPTURE_MORALE);
			intel.addLogEvent(lg);		
		}
		return heldByAttacker;
	}
	
	public void addUnit(GroundUnit unit) {
		units.add(unit);
	}
	
	public void removeUnit(GroundUnit unit) {
		units.remove(unit);
	}
	
	public boolean isIndustryTrueDisrupted() {
		return ind.getDisruptedDays() > GBConstants.DISRUPT_WHEN_CAPTURED_TIME;
	}
	
	public String getIconTooltipPartial(ForceType type, float value) {
		String displayNum = String.format("%.1f", value);
		String str = GroundBattleIntel.getString("unitEquivalent");
		str = StringHelper.substituteToken(str, "$num", displayNum);
		str = StringHelper.substituteToken(str, "$type", type.getName());
		str = StringHelper.substituteToken(str, "$unit", intel.unitSize.getNamePlural());
		return str;
	}
	
	public boolean isMoraleKnown(boolean attacker) {
		return Global.getSettings().isDevMode() || intel.playerIsAttacker == attacker;	// TODO: agent intel
	}

	public TooltipMakerAPI renderForcePanel(CustomPanelAPI panel, float width, 
			boolean attacker, UIComponentAPI rightOf) 
	{
		float pad = 3;
		TooltipMakerAPI troops = panel.createUIElement(width, HEIGHT, false);
		MarketAPI market = ind.getMarket();
		final Color hl = Misc.getHighlightColor();
		Color hp = Misc.getPositiveHighlightColor();
		Color hn = Misc.getNegativeHighlightColor();
		
		final Map<ForceType, Float> strengths = new HashMap<>();
		
		// display units present here
		boolean any = false;
		for (GroundUnit unit : units) {
			if (unit.isAttacker != attacker) continue;
			any = true;
			NexUtils.modifyMapEntry(strengths, unit.type, unit.getNumUnitEquivalents());
		}
		if (!any) {	// nothing to display, quit now
			panel.addUIElement(troops).rightOfTop(rightOf, 0);
			return troops;
		}
		
		troops.beginIconGroup();
		List<ForceType> keys = new ArrayList<>(strengths.keySet());
		Collections.sort(keys);
		for (ForceType type : keys) {
			float val = strengths.get(type);
			int count = Math.round(val * NUM_ICONS_PER_UNIT);
			if (count <= 0 && val > 0) count = 1;
			final int countFinal = count;
			
			CommodityOnMarketAPI com = intel.market.getCommodityData(type.commodityId);
			troops.addIcons(com, count, IconRenderMode.NORMAL);
		}
		troops.addIconGroup(40, pad);
		
		troops.addTooltipToPrevious(new TooltipCreator() {
				public boolean isTooltipExpandable(Object tooltipParam) {
					return false;
				}
				public float getTooltipWidth(Object tooltipParam) {
					return 240;	// FIXME magic number
				}
				public void createTooltip(TooltipMakerAPI tooltip, boolean expanded, Object tooltipParam) {
					List<ForceType> keys = new ArrayList<>(strengths.keySet());
					Collections.sort(keys);
					for (ForceType type : keys) {
						float val = strengths.get(type);
						String tooltipStr = getIconTooltipPartial(type, val);
						String displayNum = String.format("%.1f", val);
						tooltip.addPara(tooltipStr, 0f, hl, displayNum);
					}
				}
			}, TooltipLocation.BELOW);
		
		// strength
		float strength = 0;
		for (GroundUnit unit : units) {
			if (unit.isAttacker != attacker) continue;
			strength += unit.getAttackStrength();
		}
		String strengthNum = Math.round(strength) + "";
		troops.addPara(GroundBattleIntel.getString("intelDesc_strength"), pad, hl, strengthNum);
		
		// morale
		if (isMoraleKnown(attacker)) {
			float totalUnits = 0;	// denominator
			float totalMorale = 0;	// numerator
			for (GroundUnit unit : units) {
				if (unit.isAttacker != attacker) continue;
				float unitSize = unit.getNumUnitEquivalents();
				totalUnits += unitSize;
				totalMorale += unit.morale * unitSize;
			}
			if (totalUnits > 0) {
				float avgMorale = totalMorale/totalUnits;
				Color h = avgMorale > 0.45f ? hl : hn;
				troops.addPara(GroundBattleIntel.getString("intelDesc_moraleAvg"), pad, 
					h, StringHelper.toPercent(avgMorale));
			}
		}
		
		panel.addUIElement(troops).rightOfTop(rightOf, 0);
		return troops;
	}

	public void renderPanel(CustomPanelAPI panel, TooltipMakerAPI tooltip, float width) {
		CustomPanelAPI row = panel.createCustomPanel(width, HEIGHT, null);
		float pad = 3;

		// Industry image and text
		TooltipMakerAPI ttIndustry = row.createUIElement(COLUMN_WIDTH_INDUSTRY, HEIGHT, false);
		TooltipMakerAPI sub = ttIndustry.beginImageWithText(ind.getCurrentImage(), 95);
		String str = ind.getCurrentName();
		
		sub.addPara(str, 0, Misc.getHighlightColor(), ind.getCurrentName());
		
		if (isIndustryTrueDisrupted()) {
			str = StringHelper.getString("disrupted", true);
			sub.addPara(str, Misc.getHighlightColor(), pad);
		}
		float strMult = plugin.getStrengthMult();
		if (strMult != 1) {
			str = StringHelper.getString("nex_invasion2", "industryPanel_header_defBonus") + ": %s";
			sub.addPara(str, pad, strMult > 1 ? Misc.getPositiveHighlightColor() : Misc.getNegativeHighlightColor(), 
					String.format("%.2f×", strMult));
		}
		
		String owner = StringHelper.getString(heldByAttacker ? "attacker" : "defender", true);
		// TODO: color-code based on relationship of attacker to player
		str = StringHelper.getString("nex_invasion2", "industryPanel_header_heldBy");
		sub.addPara(str + ": " + owner, pad, heldByAttacker ? Misc.getPositiveHighlightColor() 
				: Misc.getNegativeHighlightColor(), owner);
		
		if (heldByAttacker && intel.playerIsAttacker) {
			boolean haveLootables = ind.getAICoreId() != null || ind.getSpecialItem() != null;
			if (haveLootables) {
				sub.addButton(GroundBattleIntel.getString("btnLoot"), 
						new Pair<String, IndustryForBattle> ("loot", this),
						64, 24, pad);
			}
		}
		
		ttIndustry.addImageWithText(0);

		row.addUIElement(ttIndustry).inLMid(0);

		// Troops
		TooltipMakerAPI atkPanel = renderForcePanel(row, COLUMN_WIDTH_TROOP_TOTAL, true, ttIndustry);
		TooltipMakerAPI defPanel = renderForcePanel(row, COLUMN_WIDTH_TROOP_TOTAL, false, atkPanel);	

		tooltip.addCustom(row, 10);
	}
}
	
