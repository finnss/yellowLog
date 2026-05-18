package com.yellowlog;

import com.google.common.collect.ImmutableSet;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.google.inject.Provides;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.EnumComposition;
import net.runelite.api.Player;
import net.runelite.api.ScriptID;
import net.runelite.api.StructComposition;
import net.runelite.api.events.ClientTick;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.ScriptPostFired;
import net.runelite.api.events.WidgetLoaded;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.gameval.ItemID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.config.RuneScapeProfileType;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.PluginManager;
import net.runelite.client.util.Text;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

@Slf4j
@PluginDescriptor(
	name = "YellowLog",
	description = "Marks collection log entries yellow when only the pet is missing",
	tags = {"collection", "log", "pet", "completion"}
)
public class YellowLogPlugin extends Plugin
{
	private static final String CONFIG_GROUP = "yellowlog";
	private static final String YELLOW_ENTRIES_CONFIG_PREFIX = "yellowEntries.";
	private static final int HEADER_TITLE_CHILD = 0;
	private static final String ALL_PETS_TITLE = "all pets";
	private static final int COLLECTION_LOG_TABS_ENUM_ID = 2102;
	private static final int COLLECTION_LOG_TAB_ENUM_PARAM_ID = 683;
	private static final int COLLECTION_LOG_PAGE_NAME_PARAM_ID = 689;
	private static final int COLLECTION_LOG_PAGE_ITEMS_ENUM_PARAM_ID = 690;
	private static final int COLLECTION_LOG_ITEM_REMAP_ENUM_ID = 3721;
	private static final String WIKISYNC_PLUGIN_CLASS_NAME = "com.andmcadams.wikisync.WikiSyncPlugin";

	private static final Set<Integer> KNOWN_PET_ITEM_IDS = ImmutableSet.of(
		ItemID.CHAOSELEPET,
		ItemID.SUPREMEPET,
		ItemID.PRIMEPET,
		ItemID.REXPET,
		ItemID.MOLEPET,
		ItemID.KQPET_WALKING,
		ItemID.KQPET_FLYING,
		ItemID.SMOKEPET,
		ItemID.ARMADYLPET,
		ItemID.BANDOSPET,
		ItemID.SARADOMINPET,
		ItemID.ZAMORAKPET,
		ItemID.KBDPET,
		ItemID.KRAKENPET,
		ItemID.PENANCEPET,
		ItemID.COREPET,
		ItemID.SNAKEPET,
		ItemID.SNAKEPET_ORANGE,
		ItemID.SNAKEPET_BLUE,
		ItemID.CHOMPYBIRD_PET,
		ItemID.VENENATIS_PET,
		ItemID.CALLISTO_PET,
		ItemID.VETION_PET,
		ItemID.VETION_PET2,
		ItemID.SCORPIA_PET,
		ItemID.JAD_PET,
		ItemID.HELL_PET,
		ItemID.ABYSSALSIRE_PET,
		ItemID.BLOODHOUND_PET,
		ItemID.SKILLPETFISH,
		ItemID.SKILLPETMINING,
		ItemID.SKILLPETWC,
		ItemID.SKILLPETHUNTER_RED,
		ItemID.SKILLPETHUNTER_GREY,
		ItemID.SKILLPETHUNTER_BLACK,
		ItemID.SKILLPETHUNTER_GOLD,
		ItemID.SKILLPETAGILITY,
		ItemID.SKILLPETFARMING,
		ItemID.SKILLPETTHIEVING,
		ItemID.SKILLPETRUNECRAFTING_FIRE,
		ItemID.SKILLPETRUNECRAFTING_AIR,
		ItemID.SKILLPETRUNECRAFTING_MIND,
		ItemID.SKILLPETRUNECRAFTING_WATER,
		ItemID.SKILLPETRUNECRAFTING_EARTH,
		ItemID.SKILLPETRUNECRAFTING_BODY,
		ItemID.SKILLPETRUNECRAFTING_COSMIC,
		ItemID.SKILLPETRUNECRAFTING_CHAOS,
		ItemID.SKILLPETRUNECRAFTING_NATURE,
		ItemID.SKILLPETRUNECRAFTING_LAW,
		ItemID.SKILLPETRUNECRAFTING_DEATH,
		ItemID.SKILLPETRUNECRAFTING_SOUL,
		ItemID.SKILLPETRUNECRAFTING_ASTRAL,
		ItemID.SKILLPETRUNECRAFTING_BLOOD,
		ItemID.SKILLPETRUNECRAFTING_WRATH,
		ItemID.SKILLPETRUNECRAFTING_GOTR,
		ItemID.PHOENIXPET,
		ItemID.PHOENIXPET_GREEN,
		ItemID.PHOENIXPET_BLUE,
		ItemID.PHOENIXPET_WHITE,
		ItemID.PHOENIXPET_PURPLE,
		ItemID.OLMPET,
		ItemID.SKOTIZOPET,
		ItemID.INFERNOPET,
		ItemID.INFERNOPET_ZUK,
		ItemID.HERBIBOARPET,
		ItemID.DAWNPET,
		ItemID.DUSKPET,
		ItemID.VORKATHPET,
		ItemID.CORPPET,
		ItemID.DOGADILEPET,
		ItemID.TEKTONPET,
		ItemID.TEKTONENRAGEDPET,
		ItemID.VANGUARDPET,
		ItemID.VASAPET,
		ItemID.VESPULAPET,
		ItemID.VESPULAFLYINGPET,
		ItemID.VERZIKPET,
		ItemID.HYDRAPET,
		ItemID.HYDRAPET_ELECTRIC,
		ItemID.HYDRAPET_FIRE,
		ItemID.HYDRAPET_EXTINGUISHED,
		ItemID.SARACHNISPET,
		ItemID.SARACHNISPET_ORANGE,
		ItemID.SARACHNISPET_BLUE,
		ItemID.GAUNTLETPET,
		ItemID.GAUNTLETPET_CORRUPT,
		ItemID.ZALCANOPET,
		ItemID.NIGHTMAREPET,
		ItemID.NIGHTMAREPET_PARASITE,
		ItemID.SOULWARSPET_BLUE,
		ItemID.SOULWARSPET_RED,
		ItemID.JAD_PET_INFERNO,
		ItemID.SKILLPETFISH_TEMPOROSS,
		ItemID.TEMPOROSSPET,
		ItemID.NEXPET,
		ItemID.ABYSSALPET,
		ItemID.WARDENPET_TUMEKEN,
		ItemID.MUSPAHPET,
		ItemID.WHISPERERPET,
		ItemID.VARDORVISPET,
		ItemID.DUKESUCELLUSPET,
		ItemID.LEVIATHANPET,
		ItemID.SCURRIUSPET,
		ItemID.SOLHEREDITPET,
		ItemID.QUETZALPET,
		ItemID.ARAXXORPET,
		ItemID.ARAXXORPET_CUTE,
		ItemID.HUEYPET,
		ItemID.AMOXLIATLPET,
		ItemID.RTBRANDAPET,
		ItemID.RTELDRICPET,
		ItemID.YAMAPET,
		ItemID.DOMPET,
		ItemID.GRYPHONBOSSPET,
		ItemID.GRYPHONBOSSPET_ADULT,
		ItemID.COWBOSSPET
	);

	private static final int[] COLLECTION_LIST_CONTAINERS = {
		InterfaceID.Collection.BOSS_CONTAINER,
		InterfaceID.Collection.RAID_CONTAINER,
		InterfaceID.Collection.CLUE_CONTAINER,
		InterfaceID.Collection.MINIGAME_CONTAINER,
		InterfaceID.Collection.OTHER_CONTAINER,
		InterfaceID.Collection.SEARCH_RESULTS
	};

	private static final int[] COLLECTION_LIST_TEXT_WIDGETS = {
		InterfaceID.Collection.BOSS_TEXT,
		InterfaceID.Collection.RAID_TEXT,
		InterfaceID.Collection.CLUE_TEXT,
		InterfaceID.Collection.MINIGAME_TEXT,
		InterfaceID.Collection.OTHER_TEXT,
		InterfaceID.Collection.SEARCH_RESULTS
	};

	@Inject
	private Client client;

	@Inject
	private YellowLogConfig config;

	@Inject
	private ConfigManager configManager;

	@Inject
	private PluginManager pluginManager;

	@Inject
	private ClientThread clientThread;

	@Inject
	private OkHttpClient okHttpClient;

	@Inject
	private Gson gson;

	private final Set<Integer> petItemIds = new HashSet<>(KNOWN_PET_ITEM_IDS);
	private final Set<String> yellowEntryTitles = new HashSet<>();
	private final Map<String, List<Integer>> pageItemIdsByTitle = new HashMap<>();
	private String cacheProfile;
	private String wikiSyncFetchProfile;
	private boolean wikiSyncFetchInFlight;

	@Override
	protected void startUp()
	{
		log.debug("YellowLog started");
	}

	@Override
	protected void shutDown()
	{
		yellowEntryTitles.clear();
		pageItemIdsByTitle.clear();
		cacheProfile = null;
		wikiSyncFetchProfile = null;
		wikiSyncFetchInFlight = false;
		log.debug("YellowLog stopped");
	}

	@Subscribe
	public void onWidgetLoaded(WidgetLoaded widgetLoaded)
	{
		if (widgetLoaded.getGroupId() == InterfaceID.COLLECTION)
		{
			updateCollectionLog();
		}
	}

	@Subscribe
	public void onScriptPostFired(ScriptPostFired scriptPostFired)
	{
		if (scriptPostFired.getScriptId() == ScriptID.COLLECTION_DRAW_LIST)
		{
			updateCollectionLog();
		}
	}

	@Subscribe
	public void onClientTick(ClientTick clientTick)
	{
		if (client.getWidget(InterfaceID.Collection.ITEMS_CONTENTS) != null)
		{
			loadCachedYellowEntries();
			fetchWikiSyncYellowEntries();
			if (!yellowEntryTitles.isEmpty())
			{
				paintVisibleListEntries();
			}
		}
	}

	@Subscribe
	public void onGameTick(GameTick gameTick)
	{
		loadCachedYellowEntries();
		fetchWikiSyncYellowEntries();

		if (client.getWidget(InterfaceID.Collection.ITEMS_CONTENTS) != null)
		{
			updateCollectionLog();
		}
	}

	private void updateCollectionLog()
	{
		loadCachedYellowEntries();
		fetchWikiSyncYellowEntries();

		Widget title = getEntryTitleWidget();
		if (title == null)
		{
			return;
		}

		String entryTitle = normalize(title.getText());
		if (entryTitle.isEmpty())
		{
			return;
		}

		Widget items = client.getWidget(InterfaceID.Collection.ITEMS_CONTENTS);
		if (items == null || items.getChildren() == null)
		{
			return;
		}

		if (ALL_PETS_TITLE.equals(entryTitle))
		{
			learnPetIds(items);
			return;
		}

		if (isOnlyMissingPet(items))
		{
			if (yellowEntryTitles.add(entryTitle))
			{
				saveCachedYellowEntries();
			}
		}
		else
		{
			if (yellowEntryTitles.remove(entryTitle))
			{
				saveCachedYellowEntries();
			}
		}

		paintVisibleListEntries();
	}

	private void fetchWikiSyncYellowEntries()
	{
		if (wikiSyncFetchInFlight || !isWikiSyncPluginActive())
		{
			return;
		}

		Player localPlayer = client.getLocalPlayer();
		if (localPlayer == null || localPlayer.getName() == null)
		{
			return;
		}

		String playerName = Text.removeTags(localPlayer.getName());
		String normalizedPlayerName = Text.toJagexName(playerName);
		if (normalizedPlayerName == null || normalizedPlayerName.isEmpty() || normalizedPlayerName.equals(wikiSyncFetchProfile))
		{
			return;
		}

		RuneScapeProfileType profileType = RuneScapeProfileType.getCurrent(client);
		HttpUrl url = new HttpUrl.Builder()
			.scheme("https")
			.host("sync.runescape.wiki")
			.addPathSegment("runelite")
			.addPathSegment("player")
			.addPathSegment(playerName)
			.addPathSegment(profileType.name())
			.build();

		wikiSyncFetchProfile = normalizedPlayerName;
		wikiSyncFetchInFlight = true;
		log.debug("Fetching WikiSync collection log data for {} ({})", playerName, profileType);
		okHttpClient.newCall(new Request.Builder().url(url).build()).enqueue(new Callback()
		{
			@Override
			public void onFailure(Call call, IOException e)
			{
				wikiSyncFetchInFlight = false;
				log.debug("Unable to fetch WikiSync collection log data", e);
			}

			@Override
			public void onResponse(Call call, Response response) throws IOException
			{
				try (Response ignored = response)
				{
					if (!response.isSuccessful())
					{
						log.debug("WikiSync collection log request failed: {}", response.code());
						return;
					}

					ResponseBody body = response.body();
					if (body == null)
					{
						return;
					}

					WikiSyncPlayerData playerData = gson.fromJson(body.string(), WikiSyncPlayerData.class);
					if (playerData == null || playerData.collectionLog == null || playerData.collectionLog.isEmpty())
					{
						return;
					}

					clientThread.invoke(() -> applyWikiSyncCollectionLog(playerData.collectionLog));
				}
				finally
				{
					wikiSyncFetchInFlight = false;
				}
			}
		});
	}

	private boolean isWikiSyncPluginActive()
	{
		for (Plugin plugin : pluginManager.getPlugins())
		{
			if (WIKISYNC_PLUGIN_CLASS_NAME.equals(plugin.getClass().getName()))
			{
				return pluginManager.isPluginActive(plugin);
			}
		}

		return false;
	}

	private void applyWikiSyncCollectionLog(List<Integer> collectionLog)
	{
		loadCachedYellowEntries();
		loadPageItemDefinitions();
		if (pageItemIdsByTitle.isEmpty())
		{
			return;
		}

		Set<Integer> obtainedItems = new HashSet<>(collectionLog);
		Set<String> refreshedYellowEntries = new HashSet<>();
		for (Map.Entry<String, List<Integer>> entry : pageItemIdsByTitle.entrySet())
		{
			String entryTitle = entry.getKey();
			if (!ALL_PETS_TITLE.equals(entryTitle) && isOnlyMissingPet(entry.getValue(), obtainedItems))
			{
				refreshedYellowEntries.add(entryTitle);
			}
		}

		if (!yellowEntryTitles.equals(refreshedYellowEntries))
		{
			yellowEntryTitles.clear();
			yellowEntryTitles.addAll(refreshedYellowEntries);
			saveCachedYellowEntries();
			paintVisibleListEntries();
			log.debug("Loaded {} yellow collection log entries from WikiSync", refreshedYellowEntries.size());
		}
	}

	private void loadPageItemDefinitions()
	{
		if (!pageItemIdsByTitle.isEmpty())
		{
			return;
		}

		EnumComposition itemRemaps = client.getEnum(COLLECTION_LOG_ITEM_REMAP_ENUM_ID);
		for (int tabStructId : client.getEnum(COLLECTION_LOG_TABS_ENUM_ID).getIntVals())
		{
			StructComposition tabStruct = client.getStructComposition(tabStructId);
			int tabEnumId = tabStruct.getIntValue(COLLECTION_LOG_TAB_ENUM_PARAM_ID);
			EnumComposition tabEnum = client.getEnum(tabEnumId);
			for (int pageStructId : tabEnum.getIntVals())
			{
				StructComposition pageStruct = client.getStructComposition(pageStructId);
				String pageTitle = normalize(pageStruct.getStringValue(COLLECTION_LOG_PAGE_NAME_PARAM_ID));
				int pageItemsEnumId = pageStruct.getIntValue(COLLECTION_LOG_PAGE_ITEMS_ENUM_PARAM_ID);
				EnumComposition pageItemsEnum = client.getEnum(pageItemsEnumId);

				List<Integer> itemIds = new ArrayList<>();
				for (int itemId : pageItemsEnum.getIntVals())
				{
					int remappedItemId = itemRemaps.getIntValue(itemId);
					itemIds.add(remappedItemId > 0 ? remappedItemId : itemId);
				}

				if (!pageTitle.isEmpty() && !itemIds.isEmpty())
				{
					pageItemIdsByTitle.put(pageTitle, itemIds);
				}
			}
		}
	}

	private void loadCachedYellowEntries()
	{
		Player localPlayer = client.getLocalPlayer();
		if (localPlayer == null || localPlayer.getName() == null)
		{
			return;
		}

		String playerName = Text.toJagexName(localPlayer.getName());
		if (playerName.equals(cacheProfile))
		{
			return;
		}

		cacheProfile = playerName;
		yellowEntryTitles.clear();

		String cachedEntries = configManager.getConfiguration(CONFIG_GROUP, YELLOW_ENTRIES_CONFIG_PREFIX + playerName);
		if (cachedEntries != null && !cachedEntries.isEmpty())
		{
			for (String cachedEntry : Text.fromCSV(cachedEntries))
			{
				String entryTitle = normalize(cachedEntry);
				if (!entryTitle.isEmpty())
				{
					yellowEntryTitles.add(entryTitle);
				}
			}
		}
	}

	private void saveCachedYellowEntries()
	{
		if (cacheProfile == null)
		{
			return;
		}

		configManager.setConfiguration(CONFIG_GROUP, YELLOW_ENTRIES_CONFIG_PREFIX + cacheProfile, Text.toCSV(yellowEntryTitles));
	}

	private Widget getEntryTitleWidget()
	{
		Widget header = client.getWidget(InterfaceID.Collection.HEADER_TEXT);
		if (header == null || header.getChildren() == null)
		{
			return null;
		}

		return header.getChild(HEADER_TITLE_CHILD);
	}

	private void learnPetIds(Widget items)
	{
		for (Widget child : items.getChildren())
		{
			if (isItemCell(child))
			{
				petItemIds.add(child.getItemId());
			}
		}
	}

	private boolean isOnlyMissingPet(Widget items)
	{
		int totalItems = 0;
		int missingItems = 0;
		int missingPetItems = 0;

		for (Widget child : items.getChildren())
		{
			if (!isItemCell(child))
			{
				continue;
			}

			totalItems++;
			if (child.getOpacity() != 0)
			{
				missingItems++;
				if (petItemIds.contains(child.getItemId()))
				{
					missingPetItems++;
				}
			}
		}

		return totalItems > 1 && missingItems == 1 && missingPetItems == 1;
	}

	private boolean isOnlyMissingPet(List<Integer> itemIds, Set<Integer> obtainedItems)
	{
		int missingItems = 0;
		int missingPetItems = 0;

		for (int itemId : itemIds)
		{
			if (obtainedItems.contains(itemId))
			{
				continue;
			}

			missingItems++;
			if (petItemIds.contains(itemId))
			{
				missingPetItems++;
			}
		}

		return itemIds.size() > 1 && missingItems == 1 && missingPetItems == 1;
	}

	private boolean isItemCell(Widget widget)
	{
		return widget != null && !widget.isHidden() && widget.getItemId() > 0;
	}

	private void paintVisibleListEntries()
	{
		int color = config.highlightColor().getRGB() & 0xFFFFFF;
		String colorTag = String.format("%06x", color);
		for (int widgetId : COLLECTION_LIST_TEXT_WIDGETS)
		{
			paintListTextWidget(client.getWidget(widgetId), colorTag);
		}

		for (int containerId : COLLECTION_LIST_CONTAINERS)
		{
			paintVisibleListEntries(client.getWidget(containerId), color);
		}
	}

	private void paintListTextWidget(Widget widget, String colorTag)
	{
		if (widget == null || widget.isHidden())
		{
			return;
		}

		String text = widget.getText();
		if (text == null || text.isEmpty())
		{
			return;
		}

		String[] lines = text.split("(?i)<br>", -1);
		boolean changed = false;
		for (int i = 0; i < lines.length; i++)
		{
			String lineTitle = normalize(lines[i]);
			if (!lineTitle.isEmpty() && yellowEntryTitles.contains(lineTitle))
			{
				String yellowLine = "<col=" + colorTag + ">" + Text.removeTags(lines[i]) + "</col>";
				if (!yellowLine.equals(lines[i]))
				{
					lines[i] = yellowLine;
					changed = true;
				}
			}
		}

		if (changed)
		{
			widget.setText(String.join("<br>", lines));
		}
	}

	private void paintVisibleListEntries(Widget widget, int color)
	{
		if (widget == null || widget.isHidden())
		{
			return;
		}

		String text = normalize(widget.getText());
		if (!text.isEmpty() && yellowEntryTitles.contains(text))
		{
			widget.setTextColor(color);
		}

		paintVisibleListEntryChildren(widget.getChildren(), color);
		paintVisibleListEntryChildren(widget.getDynamicChildren(), color);
		paintVisibleListEntryChildren(widget.getStaticChildren(), color);
		paintVisibleListEntryChildren(widget.getNestedChildren(), color);
	}

	private void paintVisibleListEntryChildren(Widget[] children, int color)
	{
		if (children == null)
		{
			return;
		}

		for (Widget child : children)
		{
			paintVisibleListEntries(child, color);
		}
	}

	private String normalize(String text)
	{
		if (text == null)
		{
			return "";
		}

		return Text.standardize(text);
	}

	private static class WikiSyncPlayerData
	{
		@SerializedName("collection_log")
		private List<Integer> collectionLog;
	}

	@Provides
	YellowLogConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(YellowLogConfig.class);
	}
}
