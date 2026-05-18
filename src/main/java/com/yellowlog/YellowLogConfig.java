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
}
