package com.fs.starfarer.api.impl.campaign.rulecmd.newgame;

import com.fs.starfarer.api.Global;
import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.rules.MemKeys;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.CharacterCreationData;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.impl.campaign.rulecmd.PaginatedOptions;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Misc.Token;
import exerelin.campaign.PlayerFactionStore;
import exerelin.utilities.ExerelinConfig;
import exerelin.utilities.ExerelinFactionConfig;
import exerelin.utilities.StringHelper;
import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import org.lwjgl.input.Keyboard;


public class Nex_NGCStartFleetOptions extends PaginatedOptions {
	
	protected static final String[] FLEET_TYPES = {"SOLO", "COMBAT_SMALL", "TRADE_SMALL", 
		"CARRIER_SMALL", "COMBAT_LARGE", "TRADE_LARGE", "CARRIER_LARGE", "SUPER"};
	protected static final String[] DIALOG_ENTRIES = {"Solo", "CombatSmall", "TradeSmall", 
		"CarrierSmall", "CombatLarge", "TradeLarge", "CarrierLarge", "Super"};
	protected static final Map<String, String> OPTION_TEXTS = new HashMap<>();
	protected static final List<Misc.Token> EMPTY_PARAMS = new ArrayList<>();
	
	static {
		for (int i=0; i<DIALOG_ENTRIES.length; i++)
		{
			OPTION_TEXTS.put(FLEET_TYPES[i], StringHelper.getString("exerelin_ngc", 
					"fleet" + DIALOG_ENTRIES[i]));
		}
	}
	protected Map<String, String> tooltips = new HashMap<>();
	protected Map<String, List<String>> allHighlights = new HashMap<>();
	
	@Override
	public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
		optionsPerPage = 4;
		super.execute(ruleId, dialog, EMPTY_PARAMS, memoryMap);
		
		populate(true);
		return true;
	}
	
	@Override
	public void showOptions()
	{
		super.showOptions();
		
		// add the tooltips
		for (Map.Entry<String, String> tmp : tooltips.entrySet())
		{
			String option = tmp.getKey();
			String tooltip = tmp.getValue();
			List<String> highlights = allHighlights.get(option);
			
			dialog.getOptionPanel().setTooltip(option, tooltip);
			List<Color> colors = new ArrayList<>();
			for (String highlight : highlights) {
				colors.add(Misc.getHighlightColor());
			}
			dialog.getOptionPanel().setTooltipHighlights(option, highlights.toArray(new String[0]));
			dialog.getOptionPanel().setTooltipHighlightColors(option, colors.toArray(new Color[0]));
		}
	}
	
	@Override
	public void optionSelected(String optionText, Object optionData) {
		if ("nex_NGCFleetReroll".equals(optionData))
		{
			populate(false);
			return;
		}
		super.optionSelected(optionText, optionData);
	}
	
	protected void populate(boolean firstTime)
	{
		dialog.getOptionPanel().clearOptions();
		addShipOptions();
		
		if (firstTime)
		{
			if (memoryMap.get(MemKeys.LOCAL).getBoolean("$randomStartShips"))
				addOptionAllPages(Misc.ucFirst(StringHelper.getString("exerelin_ngc", 
						"fleetRandomReroll")), "nex_NGCFleetReroll");
			addOptionAllPages(Misc.ucFirst(StringHelper.getString("back")), "nex_NGCFleetBack");
		}
		
		showOptions();
	}
	
	protected void addShipOptions()
	{
		tooltips.clear();
		allHighlights.clear();
		
		ExerelinFactionConfig factionConf = ExerelinConfig.getExerelinFactionConfig(PlayerFactionStore.getPlayerFactionIdNGC());
		for (int i=0; i<FLEET_TYPES.length; i++)
		{
			String fleetTypeStr = FLEET_TYPES[i];
			String option = "nex_NGCFleet" + DIALOG_ENTRIES[i];
			List<String> startingVariants = factionConf.getStartShipsForType(fleetTypeStr, false);
			if (startingVariants == null) {
				//dialog.getOptionPanel().setEnabled(option, false);
				continue;
			}
			List<String> highlights = new ArrayList<>();
			
			String tooltip = "";
			for (int j=0; j < startingVariants.size(); j++)
			{
				String variantId = startingVariants.get(j);
				FleetMemberType type = FleetMemberType.SHIP;
				if (variantId.endsWith("_wing")) {
					type = FleetMemberType.FIGHTER_WING; 
				}

				FleetMemberAPI temp = Global.getFactory().createFleetMember(type, variantId);

				String className = temp.getHullSpec().getHullName();
				String variantName = temp.getVariant().getDisplayName().toLowerCase();
				String designation = temp.getVariant().getDesignation().toLowerCase();
				String tooltipLine;
				if (type == FleetMemberType.FIGHTER_WING)
					tooltipLine = StringHelper.getString("exerelin_ngc", "fighterWingString");
				else
					tooltipLine = StringHelper.getString("exerelin_ngc", "shipString");
				tooltipLine = StringHelper.substituteToken(tooltipLine, "$shipClass", className);
				tooltipLine = StringHelper.substituteToken(tooltipLine, "$variantName", variantName);
				tooltipLine = StringHelper.substituteToken(tooltipLine, "$designation", designation);
				
				tooltip += tooltipLine;
				if (j < startingVariants.size() - 1) tooltip += "\n";
				highlights.add(className);
			}
			addOption(OPTION_TEXTS.get(fleetTypeStr), option);
			tooltips.put(option, tooltip);
			allHighlights.put(option, highlights);
			
			memoryMap.get(MemKeys.LOCAL).set("$startShips_" + fleetTypeStr, startingVariants);
		}
	}
}