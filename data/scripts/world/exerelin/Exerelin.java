package data.scripts.world.exerelin;

import java.awt.*;
import java.util.List;
import org.apache.log4j.Logger;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import exerelin.commandQueue.CommandQueue;
import exerelin.*;
import exerelin.PlayerFactionStore;
import exerelin.events.EventPirateFleetSpawn;
import exerelin.utilities.ExerelinConfig;
import exerelin.utilities.ExerelinMessageManager;
import exerelin.utilities.ExerelinUtilsMessaging;

import java.util.Collections;

@SuppressWarnings("unchecked")
public class Exerelin //implements SectorGeneratorPlugin
{
	public static Logger log = Global.getLogger(Exerelin.class);
	private static String playerFaction = "independent";
	
	public void generate(SectorAPI sector)
	{
		System.out.println("Starting sector setup...");
	
		ExerelinSetupData.getInstance().resetAvailableFactions();
	
		// Set sector manager reference in persistent storage
		SectorManager sectorManager = new SectorManager();
		Global.getSector().getPersistentData().put("SectorManager", sectorManager);
	
		// Set starting conditions needed later for saving into the save file
		sectorManager.setPlayerFreeTransfer(ExerelinSetupData.getInstance().playerOwnedStationFreeTransfer);
		sectorManager.setRespawnFactions(ExerelinSetupData.getInstance().respawnFactions);
		sectorManager.setMaxFactions(ExerelinSetupData.getInstance().maxFactionsInExerelinAtOnce);
		sectorManager.setPlayerFactionId(ExerelinSetupData.getInstance().getPlayerFaction());
		sectorManager.setFactionsPossibleInSector(ExerelinSetupData.getInstance().getAvailableFactions(sector));
		sectorManager.setRespawnWaitDays(ExerelinSetupData.getInstance().respawnDelay);
		sectorManager.setBuildOmnifactory(ExerelinSetupData.getInstance().omniFacPresent);
		sectorManager.setMaxSystemSize(ExerelinSetupData.getInstance().maxSystemSize);
		sectorManager.setPlayerStartShipVariant(ExerelinSetupData.getInstance().getPlayerStartingShipVariant());
		sectorManager.setSectorPrePopulated(ExerelinSetupData.getInstance().isSectorPopulated);
		sectorManager.setSectorPartiallyPopulated(ExerelinSetupData.getInstance().isSectorPartiallyPopulated);
	
		// Setup the sector manager
		sectorManager.setupSectorManager(sector);
	
		// Build a message manager object and add to persistent storage
		ExerelinMessageManager exerelinMessageManager = new ExerelinMessageManager();
		Global.getSector().getPersistentData().put("ExerelinMessageManager", exerelinMessageManager);
	
		sectorManager.setupFactionDirectors();
	
		// Build and add a time mangager
		TimeManager timeManger = new TimeManager();
		Global.getSector().addScript(timeManger);
	
		// Add a EveryFrameScript command queue to handle synchronous-only events
		CommandQueue commandQueue = new CommandQueue();
		Global.getSector().addScript(commandQueue);
		sectorManager.setCommandQueue(commandQueue);
	
		// Set abandoned as enemy of every faction
		ExerelinConfig.loadSettings();
		this.initFactionRelationships(sector);
	
		// Populate sector
		//this.populateSector(Global.getSector(), sectorManager);
	
		// Add trader spawns
		//this.initTraderSpawns(sector);
	
		// Remove any data stored in ExerelinSetupData
		ExerelinSetupData.resetInstance();
	
		System.out.println("Finished sector setup...");
	}

	private void initFactionRelationships(SectorAPI sector)
	{
		/*String[] factions = ExerelinSetupData.getInstance().getAvailableFactions(sector);
		for(int i = 0; i < factions.length; i = i + 1)
		{
			sector.getFaction(factions[i]).setRelationship("abandoned", -1);
			sector.getFaction(factions[i]).setRelationship("rebel", -1);
			sector.getFaction(factions[i]).setRelationship("independent", 0);
			sector.getFaction(factions[i]).setRelationship("pirates", -1);

			String customRebelFactionId = ExerelinConfig.getExerelinFactionConfig(factions[i]).customRebelFaction;
			if(!customRebelFactionId.equalsIgnoreCase(""))
			{
				for(int j = 0; j < factions.length; j = j + 1)
				{
					sector.getFaction(factions[j]).setRelationship(customRebelFactionId, -1);
				}
			}
		}

		// Set independent and rebels to hate each other
		FactionAPI rebel = sector.getFaction("rebel");
		FactionAPI independent = sector.getFaction("independent");
		rebel.setRelationship(independent.getId(), -1);
		independent.setRelationship(rebel.getId(), -1);
	
		// Set player faction starting  diplomacy
		sector.getFaction("player").setRelationship("abandoned", -1);
		sector.getFaction("player").setRelationship("rebel", -1);
		sector.getFaction("player").setRelationship("independent", 0);
		sector.getFaction("player").setRelationship("pirates", -1);
	
		for(int i = 0; i < factions.length; i = i + 1)
		{
		String customRebelFactionId = ExerelinConfig.getExerelinFactionConfig(factions[i]).customRebelFaction;
		sector.getFaction("player").setRelationship(customRebelFactionId, -1);
		SectorManager.getCurrentSectorManager().getDiplomacyManager().getRecordForFaction(factions[i]).setPlayerInfluence(15);
		}
	
		if(!SectorManager.getCurrentSectorManager().isPlayerInPlayerFaction())
		{
		// Player is aligned with a faction so set initial influence
		SectorManager.getCurrentSectorManager().getDiplomacyManager().playerRecord.setPlayerInfluence(50);
		}*/
	
		FactionAPI player = sector.getFaction("player");
		String selectedFactionKey = PlayerFactionStore.getPlayerFaction();
		FactionAPI selectedFaction = sector.getFaction(selectedFactionKey);
		log.info("Selected faction is " + selectedFaction + " | " + selectedFactionKey);
		
		FactionAPI pirates = sector.getFaction("pirates");
		FactionAPI sindrian_diktat = sector.getFaction("sindrian_diktat");
		FactionAPI tritachyon = sector.getFaction("tritachyon");
		FactionAPI luddic_church = sector.getFaction("luddic_church");
		FactionAPI hegemony = sector.getFaction("hegemony");
		FactionAPI independent = sector.getFaction("independent");
	
		pirates.setRelationship("sindrian_diktat", RepLevel.HOSTILE);
		pirates.setRelationship("tritachyon", RepLevel.HOSTILE);
		pirates.setRelationship("luddic_church", RepLevel.HOSTILE);
		pirates.setRelationship("hegemony", RepLevel.HOSTILE);
		pirates.setRelationship("independent", RepLevel.HOSTILE);
	
		sindrian_diktat.setRelationship("pirates", RepLevel.HOSTILE);
		sindrian_diktat.setRelationship("luddic_church", RepLevel.HOSTILE);
		sindrian_diktat.setRelationship("hegemony", RepLevel.SUSPICIOUS);
		sindrian_diktat.setRelationship("tritachyon", RepLevel.FRIENDLY);
	
		tritachyon.setRelationship("pirates", RepLevel.HOSTILE);
		tritachyon.setRelationship("hegemony", RepLevel.HOSTILE);
		tritachyon.setRelationship("luddic_church", RepLevel.SUSPICIOUS);
		tritachyon.setRelationship("sindrian_diktat", RepLevel.COOPERATIVE);
	
		luddic_church.setRelationship("pirates", RepLevel.HOSTILE);
		luddic_church.setRelationship("sindrian_diktat", RepLevel.HOSTILE);
		luddic_church.setRelationship("tritachyon", RepLevel.SUSPICIOUS);
		luddic_church.setRelationship("hegemony", RepLevel.FRIENDLY);
	
		hegemony.setRelationship("pirates", RepLevel.HOSTILE);
		hegemony.setRelationship("tritachyon", RepLevel.HOSTILE);
		hegemony.setRelationship("sindrian_diktat", RepLevel.SUSPICIOUS);
		hegemony.setRelationship("hegemony", RepLevel.COOPERATIVE);
	
		independent.setRelationship("pirates", RepLevel.HOSTILE);
	
		// set player relations based on selected faction
		List factions = sector.getAllFactions();
		
		for (int i=1; i<factions.size(); i++)
		{
			FactionAPI faction = (FactionAPI)(factions.get(i));
			if (faction != player && faction != selectedFaction)
			{
				float relationship = selectedFaction.getRelationship(faction.getId());
				player.setRelationship(faction.getId(), relationship);
				faction.setRelationship("player", relationship);
			}
		}
		
		player.setRelationship(selectedFactionKey, RepLevel.FRIENDLY);
		selectedFaction.setRelationship("player", RepLevel.FRIENDLY);
	}

	private void initTraderSpawns(SectorAPI sector)
	{
		for(int j = 0; j < SectorManager.getCurrentSectorManager().getSystemManagers().length; j++)
		{
			StarSystemAPI systemAPI = SectorManager.getCurrentSectorManager().getSystemManagers()[j].getStarSystemAPI();
			for(int i = 0; i < Math.max(1, systemAPI.getOrbitalStations().size()/5); i++)
			{
				IndependantTraderSpawnPoint tgtsp = new IndependantTraderSpawnPoint(sector,  systemAPI,  ExerelinUtils.getRandomInRange(8,12), 1, systemAPI.createToken(0,0));
				systemAPI.addSpawnPoint(tgtsp);
			}
		}
	}

	private void populateSector(SectorAPI sector, SectorManager sectorManager)
	{
		boolean finishedPopulating = false;
		int populated = 0;
	
		// Popuate a single station for each starting faction
		String[] factions = sectorManager.getFactionsPossibleInSector();
		int numFactionsInitialStart = Math.min(factions.length - 1, ExerelinSetupData.getInstance().numStartFactions);
	
		// If player is starting unaligned add one more starting faction
		if(SectorManager.getCurrentSectorManager().isPlayerInPlayerFaction())
		numFactionsInitialStart++;
	
		for(int i = 0; i < numFactionsInitialStart; i++)
		{
			String factionId = factions[i];
			if(factionId.equalsIgnoreCase(sectorManager.getPlayerFactionId()))
			{
				numFactionsInitialStart = numFactionsInitialStart + 1;
				continue;
			}
		
			// Find an available station
			List systems = sector.getStarSystems();
			Collections.shuffle(systems);
			systemLoop: for(int j = 0; j < systems.size(); j++)
			{
				StarSystemAPI systemAPI = (StarSystemAPI)systems.get(j);
				List stations = systemAPI.getOrbitalStations();
				for(int k = 0; k < stations.size(); k++)
				{
					SectorEntityToken station = (SectorEntityToken)stations.get(k);
					if(station.getFaction().getId().equalsIgnoreCase("abandoned"))
					{
						SystemStationManager systemStationManager = SystemManager.getSystemManagerForAPI(systemAPI).getSystemStationManager();
						StationRecord stationRecord = systemStationManager.getStationRecordForToken(station);
						stationRecord.setOwner(factionId, false, false);
						System.out.println("Setting start station in " + systemAPI.getName() + " for: " + factionId);
						FactionDirector.getFactionDirectorForFactionId(factionId).setHomeSystem(systemAPI);
						populated++;
						break systemLoop;
					}
				}
			}
		}
	
		// Add players start station
		if(!sectorManager.getPlayerFactionId().equalsIgnoreCase("player"))
		{
			List systems = sector.getStarSystems();
			Collections.shuffle(systems);
			systemLoop: for(int j = 0; j < systems.size(); j++)
			{
				StarSystemAPI systemAPI = (StarSystemAPI)systems.get(j);
				List stations = systemAPI.getOrbitalStations();
				for(int k = 0; k < stations.size(); k++)
				{
					SectorEntityToken station = (SectorEntityToken)stations.get(k);
					if(station.getFaction().getId().equalsIgnoreCase("abandoned"))
					{
						SystemStationManager systemStationManager = SystemManager.getSystemManagerForAPI(systemAPI).getSystemStationManager();
						StationRecord stationRecord = systemStationManager.getStationRecordForToken(station);
						stationRecord.setOwner(sectorManager.getPlayerFactionId(), false, false);
						System.out.println("Setting start station in " + systemAPI.getName() + " for: " + sectorManager.getPlayerFactionId());
						FactionDirector.getFactionDirectorForFactionId(sectorManager.getPlayerFactionId()).setHomeSystem(systemAPI);
						populated++;
						break systemLoop;
					}
				}
			}
		}
	
		// Populate rest of sector half or full
	
		String[] factionsInSector = sectorManager.getFactionsInSector();
	
		// If empty sector, only leave one station
		if(!ExerelinSetupData.getInstance().isSectorPopulated)
		finishedPopulating = true;
	
		while(!finishedPopulating)
		{
		for(int i = 0; i < factionsInSector.length; i++)
		{
			String factionId = factionsInSector[i];
	
			// Check home system for available stations
			StarSystemAPI homeSystem = FactionDirector.getFactionDirectorForFactionId(factionId).getHomeSystem();
			StarSystemAPI system = homeSystem;
			SectorEntityToken station = ExerelinUtils.getClosestEnemyStation(factionId, homeSystem, ExerelinUtils.getRandomStationInSystemForFaction(factionId, homeSystem));
	
			if(station == null)
			{
			// Couldn't find station in home system so find closest available system
			system = ExerelinUtils.getClosestSystemForFaction(homeSystem, factionId, -1f, -0.0001f);
			if(system != null)
				station = ExerelinUtils.getClosestEntityToSystemEntrance(system, factionId, -1f, -0.0001f);
			}
	
			if(station == null)
			continue; // Move to next faction
	
			SystemStationManager systemStationManager = SystemManager.getSystemManagerForAPI(system).getSystemStationManager();
			StationRecord stationRecord = systemStationManager.getStationRecordForToken(station);
			stationRecord.setOwner(factionId, false, false);
			System.out.println("Setting station in " + system.getName() + " for: " + factionId);
			populated++;
	
			if((sectorManager.isSectorPartiallyPopulated() && populated > (((StarSystemAPI)Global.getSector().getStarSystems().get(0)).getOrbitalStations().size()*Global.getSector().getStarSystems().size())/2)
				|| populated >= ((StarSystemAPI)Global.getSector().getStarSystems().get(0)).getOrbitalStations().size()*Global.getSector().getStarSystems().size())
			{
				finishedPopulating = true;
				break;
			}
		}
	
	
	
	
		}
	
		// Setup some initial pirate spawns
		try {
			EventPirateFleetSpawn pirateFleetSpawn = new EventPirateFleetSpawn();
			List systems = sector.getStarSystems();
			systemLoop:
			for (int j = 0; j < systems.size(); j++) {
				StarSystemAPI systemAPI = (StarSystemAPI) systems.get(j);
				pirateFleetSpawn.spawnPirateFleet(systemAPI, true);
				pirateFleetSpawn.spawnPirateFleet(systemAPI, true);
				pirateFleetSpawn.spawnPirateFleet(systemAPI, true);
				pirateFleetSpawn.spawnPirateFleet(systemAPI, true);
				pirateFleetSpawn.spawnPirateFleet(systemAPI, true);
			}
		}
		catch (Exception e)
		{
			ExerelinUtilsMessaging.addMessage("ERROR: Exerelin mod out of sync with another mod. See log for details.", Color.white.ORANGE);
			System.out.println(e.getMessage());
		}
	}
}
