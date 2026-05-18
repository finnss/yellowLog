package com.yellowlog;

import java.awt.Color;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("yellowlog")
public interface YellowLogConfig extends Config
{
	@ConfigItem(
		position = 0,
		keyName = "highlightColor",
		name = "Highlight color",
		description = "Color used for collection log entries where only the pet is missing."
	)
	default Color highlightColor()
	{
		return Color.YELLOW;
	}

	@ConfigItem(
		position = 1,
		keyName = "useWikiSync",
		name = "Use WikiSync data",
		description = "Use public WikiSync collection log data to mark entries before they are opened."
	)
	default boolean useWikiSync()
	{
		return true;
	}
}
