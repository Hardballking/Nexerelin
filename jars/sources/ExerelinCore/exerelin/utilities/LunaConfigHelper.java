package exerelin.utilities;

import com.fs.starfarer.api.Global;
import exerelin.ExerelinConstants;
import exerelin.plugins.ExerelinModPlugin;
import lombok.extern.log4j.Log4j;
import lunalib.lunaSettings.LunaSettings;
import lunalib.lunaSettings.LunaSettingsListener;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

@Log4j
public class LunaConfigHelper implements LunaSettingsListener {

    @Deprecated public static final List<String> DEFAULT_TAGS = new ArrayList<>();  // we don't use tags no more
    static {
        DEFAULT_TAGS.add("spacing:0.5");
    }

    // runcode exerelin.utilities.LunaConfigHelper.initLunaConfig()
    public static void initLunaConfig() {
        String mid = ExerelinConstants.MOD_ID;
        //List<String> tags = DEFAULT_TAGS;

        addSetting("nexDevMode", "boolean", ExerelinModPlugin.isNexDev);
        addSetting("ceasefireNotificationPopup", "boolean", NexConfig.ceasefireNotificationPopup);

        addHeader("invasions");
        addSetting("enableInvasions", "boolean", NexConfig.enableInvasions);
        addSetting("legacyInvasions", "boolean", NexConfig.legacyInvasions);
        addSetting("invasionsOnlyAfterPlayerColony", "boolean", NexConfig.invasionsOnlyAfterPlayerColony);
        addSetting("allowInvadeStoryCritical", "boolean", NexConfig.allowInvadeStoryCritical);
        addSetting("followersInvasions", "boolean", NexConfig.followersInvasions);
        addSetting("allowPirateInvasions", "boolean", NexConfig.allowPirateInvasions);
        addSetting("retakePirateMarkets", "boolean", NexConfig.retakePirateMarkets);

        addSetting("invasionGracePeriod", "int", Math.round(NexConfig.invasionGracePeriod), 0, 365*5);
        addSetting("pointsRequiredForInvasionFleet", "int", Math.round(NexConfig.pointsRequiredForInvasionFleet), 2000, 100000);
        addSetting("baseInvasionPointsPerFaction", "int", Math.round(NexConfig.baseInvasionPointsPerFaction), 0, 1000);
        addSetting("invasionPointsPerPlayerLevel", "int", Math.round(NexConfig.invasionPointsPerPlayerLevel), 0, 100);
        addSetting("invasionPointEconomyMult", "float", NexConfig.invasionPointEconomyMult, 0, 10);
        addSetting("invasionFleetSizeMult", "float", NexConfig.invasionFleetSizeMult, 0.1, 10);
        addSetting("fleetRequestCostPerFP", "int", Math.round(NexConfig.fleetRequestCostPerFP), 1, 10000);
        addSetting("creditLossOnColonyLossMult", "float", NexConfig.creditLossOnColonyLossMult, 0, 1);
        addSetting("groundBattleDamageMult", "float", NexConfig.groundBattleDamageMult, 0, 5);

        addHeader("insurance");
        addSetting("legacyInsurance", "boolean", NexConfig.legacyInsurance);
        addSetting("playerInsuranceMult", "float", NexConfig.playerInsuranceMult, 0, 10);

        addHeader("agents");
        addSetting("agentBaseSalary", "int", NexConfig.agentBaseSalary, 0, 100000);
        addSetting("agentSalaryPerLevel", "int", NexConfig.agentSalaryPerLevel, 0, 100000);
        addSetting("maxAgents", "int", NexConfig.maxAgents, 0, 100);
        addSetting("agentStealMarketShipsOnly", "boolean", !NexConfig.agentStealAllShips);
        addSetting("useAgentSpecializations", "boolean", NexConfig.useAgentSpecializations);
        addSetting("followersAgents", "boolean", NexConfig.followersAgents);

        addHeader("prisoners");
        addSetting("prisonerRepatriateRepValue", "float", NexConfig.prisonerRepatriateRepValue, 0, 1);
        addSetting("prisonerBaseRansomValue", "int", (int)NexConfig.prisonerBaseRansomValue, 0, 1000000);
        addSetting("prisonerRansomValueIncrementPerLevel", "int", (int)NexConfig.prisonerRansomValueIncrementPerLevel, 0, 1000000);
        addSetting("crewLootMult", "float", NexConfig.crewLootMult, 0, 100);

        addHeader("satbomb");
        addSetting("allowNPCSatBomb", "boolean", NexConfig.allowNPCSatBomb);
        addSetting("permaHateFromPlayerSatBomb", "float", NexConfig.permaHateFromPlayerSatBomb, 0, 1);

        addHeader("vengeance");
        addSetting("enableRevengeFleets", "int", NexConfig.enableRevengeFleets, 0, 2);
        addSetting("useNewVengeanceEncounters", "boolean", NexConfig.useNewVengeanceEncounters);
        addSetting("vengeanceFleetSizeMult", "float", NexConfig.vengeanceFleetSizeMult, 0.2, 5);

        addHeader("otherFleets");
        addSetting("colonyExpeditionInterval", "int", NexConfig.colonyExpeditionInterval, 15, 10000);
        addSetting("specialForcesPointMult", "float", NexConfig.specialForcesPointMult, 0, 10);
        addSetting("specialForcesSizeMult", "float", NexConfig.specialForcesSizeMult, 0.2, 5);

        addHeader("misc");
        addSetting("enableVictory", "boolean", NexConfig.enableVictory);

        addSetting("hardModeColonyGrowthMult", "float", NexConfig.hardModeColonyGrowthMult, 0.5f, 1f);
        addSetting("hardModeColonyIncomeMult", "float", NexConfig.hardModeColonyIncomeMult, 0.5f, 1f);
        addSetting("enablePunitiveExpeditions", "boolean", NexConfig.enablePunitiveExpeditions);
        //addSetting("prismNumBossShips", "int", NexConfig.prismNumBossShips, 0, 10);
        addSetting("officerDeaths", "boolean", NexConfig.officerDeaths);
        addSetting("rebellionMult", "float", NexConfig.rebellionMult, 0f, 10f);

        LunaSettings.SettingsCreator.refresh(mid);

        try {
            loadConfigFromLuna();
        } catch (NullPointerException npe) {
            // config not created yet I guess, do nothing
        }
    }

    public static void loadConfigFromLuna() {
        ExerelinModPlugin.isNexDev = (boolean)loadSetting("nexDevMode", "boolean");
        NexConfig.ceasefireNotificationPopup = (boolean)loadSetting("ceasefireNotificationPopup", "boolean");

        NexConfig.crewLootMult = (float)loadSetting("crewLootMult", "float");

        NexConfig.enableInvasions = (boolean)loadSetting("enableInvasions", "boolean");
        NexConfig.legacyInvasions = (boolean)loadSetting("legacyInvasions", "boolean");
        NexConfig.invasionsOnlyAfterPlayerColony = (boolean)loadSetting("invasionsOnlyAfterPlayerColony", "boolean");
        NexConfig.allowInvadeStoryCritical = (boolean)loadSetting("allowInvadeStoryCritical", "boolean");
        NexConfig.followersInvasions = (boolean)loadSetting("followersInvasions", "boolean");
        NexConfig.allowPirateInvasions = (boolean)loadSetting("allowPirateInvasions", "boolean");
        NexConfig.retakePirateMarkets = (boolean)loadSetting("retakePirateMarkets", "boolean");
        NexConfig.invasionGracePeriod = (int)loadSetting("invasionGracePeriod", "int");
        NexConfig.pointsRequiredForInvasionFleet = (int)loadSetting("pointsRequiredForInvasionFleet", "int");
        NexConfig.baseInvasionPointsPerFaction = (int)loadSetting("baseInvasionPointsPerFaction", "int");
        NexConfig.invasionPointsPerPlayerLevel = (int)loadSetting("invasionPointsPerPlayerLevel", "int");
        NexConfig.invasionPointEconomyMult = (float)loadSetting("invasionPointEconomyMult", "float");
        NexConfig.invasionFleetSizeMult = (float)loadSetting("invasionFleetSizeMult", "float");
        NexConfig.fleetRequestCostPerFP = (int)loadSetting("invasionFleetSizeMult", "int");
        NexConfig.creditLossOnColonyLossMult = (float)loadSetting("creditLossOnColonyLossMult", "float");
        NexConfig.groundBattleDamageMult = (float)loadSetting("groundBattleDamageMult", "float");

        NexConfig.legacyInsurance = (boolean)loadSetting("legacyInsurance", "boolean");
        NexConfig.playerInsuranceMult = (float)loadSetting("playerInsuranceMult", "float");

        NexConfig.agentBaseSalary = (int)loadSetting("agentBaseSalary", "int");
        NexConfig.agentSalaryPerLevel = (int)loadSetting("agentSalaryPerLevel", "int");
        NexConfig.maxAgents = (int)loadSetting("maxAgents", "int");
        NexConfig.agentStealAllShips = !(boolean)loadSetting("agentStealMarketShipsOnly", "boolean");
        NexConfig.useAgentSpecializations = (boolean)loadSetting("useAgentSpecializations", "boolean");
        NexConfig.followersAgents = (boolean)loadSetting("followersAgents", "boolean");

        NexConfig.prisonerRepatriateRepValue = (float)loadSetting("prisonerRepatriateRepValue", "float");
        NexConfig.prisonerBaseRansomValue = (float)loadSetting("prisonerBaseRansomValue", "float");
        NexConfig.prisonerRansomValueIncrementPerLevel = (float)loadSetting("prisonerRansomValueIncrementPerLevel", "float");

        NexConfig.allowNPCSatBomb = (boolean)loadSetting("allowNPCSatBomb", "boolean");
        NexConfig.permaHateFromPlayerSatBomb = (float)loadSetting("permaHateFromPlayerSatBomb", "float");

        NexConfig.enableRevengeFleets = (int)loadSetting("enableRevengeFleets", "int");
        NexConfig.useNewVengeanceEncounters = (boolean)loadSetting("useNewVengeanceEncounters", "boolean");
        NexConfig.vengeanceFleetSizeMult = (float)loadSetting("vengeanceFleetSizeMult", "float");

        NexConfig.colonyExpeditionInterval = (int)loadSetting("colonyExpeditionInterval", "int");
        NexConfig.specialForcesPointMult = (float)loadSetting("specialForcesPointMult", "float");
        NexConfig.specialForcesSizeMult = (float)loadSetting("specialForcesSizeMult", "float");

        NexConfig.enableVictory = (boolean)loadSetting("enableVictory", "boolean");
        NexConfig.hardModeColonyGrowthMult = (float)loadSetting("hardModeColonyGrowthMult", "float");
        NexConfig.hardModeColonyIncomeMult = (float)loadSetting("hardModeColonyIncomeMult", "float");
        NexConfig.enablePunitiveExpeditions = (boolean)loadSetting("enablePunitiveExpeditions", "boolean");
        NexConfig.officerDeaths = (boolean)loadSetting("officerDeaths", "boolean");
        NexConfig.rebellionMult = (float)loadSetting("rebellionMult", "float");
        //NexConfig.prismNumBossShips = (int)loadSetting("prismNumBossShips", "int");
    }

    public static Object loadSetting(String var, String type) {
        String mid = ExerelinConstants.MOD_ID;
        switch (type) {
            case "bool":
            case "boolean":
                return LunaSettings.getBoolean(mid, var);
            case "int":
            case "integer":
                return LunaSettings.getInt(mid, var);
            case "float":
                return (float)(double)LunaSettings.getDouble(mid, var);
            case "double":
                return LunaSettings.getDouble(mid, var);
            default:
                log.error(String.format("Setting %s has invalid type %s", var, type));
        }
        return null;
    }

    public static void addSetting(String var, String type, Object defaultVal) {
        addSetting(var, type, defaultVal, Integer.MIN_VALUE, Integer.MAX_VALUE);
    }

    public static void addSetting(String var, String type, Object defaultVal, double min, double max) {
        String tooltip = Global.getSettings().getString("nex_lunaSettings", "tooltip_" + var);
        if (tooltip.startsWith("Missing string:")) {
            tooltip = "";
        }
        String mid = ExerelinConstants.MOD_ID;

        switch (type) {
            case "boolean":
                LunaSettings.SettingsCreator.addBoolean(mid, var, getString("name_" + var), tooltip, (boolean)defaultVal);
                break;
            case "int":
            case "integer":
                if (defaultVal instanceof Float) {
                    defaultVal = Math.round((float)defaultVal);
                }
                LunaSettings.SettingsCreator.addInt(mid, var, getString("name_" + var), tooltip,
                        (int)defaultVal, (int)Math.round(min), (int)Math.round(max));
                break;
            case "float":
                // fix float -> double conversion causing an unround number
                String floatStr = ((Float)defaultVal).toString();
                LunaSettings.SettingsCreator.addDouble(mid, var, getString("name_" + var), tooltip,
                        Double.parseDouble(floatStr), min, max);
                break;
            case "double":
                LunaSettings.SettingsCreator.addDouble(mid, var, getString("name_" + var), tooltip,
                        (double)defaultVal, min, max);
                break;
            default:
                log.error(String.format("Setting %s has invalid type %s", var, type));
        }
    }

    public static void addHeader(String id) {
        LunaSettings.SettingsCreator.addHeader(ExerelinConstants.MOD_ID, id, getString("header_" + id));
    }

    public static void addHeader(String id, String title) {
        LunaSettings.SettingsCreator.addHeader(ExerelinConstants.MOD_ID, id, title);
    }

    public static LunaConfigHelper createListener() {
        LunaConfigHelper helper = new LunaConfigHelper();
        Global.getSector().getListenerManager().addListener(helper, true);
        return helper;
    }

    @Override
    public void settingsChanged(String modId) {
        if (ExerelinConstants.MOD_ID.equals(modId)) {
            loadConfigFromLuna();
        }
    }

    public static String getString(String id) {
        return StringHelper.getString("nex_lunaSettings", id);
    }
}
