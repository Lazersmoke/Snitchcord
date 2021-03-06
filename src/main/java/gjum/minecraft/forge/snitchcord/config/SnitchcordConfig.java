package gjum.minecraft.forge.snitchcord.config;


import gjum.minecraft.forge.snitchcord.SnitchcordMod;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

import net.minecraft.util.text.TextComponentString;
import net.minecraft.client.Minecraft;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class SnitchcordConfig {
    public static final String CATEGORY_MAIN = "Main";

    public static final SnitchcordConfig instance = new SnitchcordConfig();

    public Configuration config;

    public boolean enabled;
    public String webhookUrl;
    public String alertFormat;
    private Property propEnabled, propWebhookUrl, propAlertFormat;

    public Pattern alertTrackFilter, alertIgnoreFilter;
    private Property propAlertTrackFilter, propAlertIgnoreFilter;

    public boolean ignorelistOn;
    public boolean tracklistOn;
    public HashSet<String> ignorelist;
    public HashSet<String> tracklist;
    private Property propIgnorelistOn, propTracklistOn, propIgnorelist, propTracklist;

    public boolean ignoredGroupsOn;
    public boolean trackedGroupsOn;
    public HashSet<String> ignoredGroups;
    public HashSet<String> trackedGroups;
    private Property propignoredGroupsOn, proptrackedGroupsOn, propignoredGroups, proptrackedGroups;

    private SnitchcordConfig() {
    }

    public void load(File configFile) {
        config = new Configuration(configFile, SnitchcordMod.VERSION);
        syncProperties();
        config.load();
        syncProperties();
        syncValues();
    }

    public void afterGuiSave() {
        syncProperties();
        syncValues();
    }

    public void setEnabled(boolean enabled) {
        syncProperties();
        propEnabled.set(enabled);
        syncValues();
    }

    /**
     * no idea why this has to be called so often, ideally the prop* would stay the same,
     * but it looks like they get disassociated from the config sometimes and setting them no longer has any effect
     */
    private void syncProperties() {
        propEnabled = config.get(CATEGORY_MAIN, "enabled", true, "Enable/disable snitch sending");
        propAlertFormat = config.get(CATEGORY_MAIN, "alert format", "{\"content\":\"`<time>` **<player>**< :nonEnter:> at <snitch>< :nonWorld:> ~[<roundedCoords>]\"}", "");
        propWebhookUrl = config.get(CATEGORY_MAIN, "webhook url", "", "Get this from the discord channel settings");

        propAlertIgnoreFilter = config.get(CATEGORY_MAIN, "alert filter: ignore", "", "Regular expression, if an alert matches it will not be sent");
        propAlertTrackFilter = config.get(CATEGORY_MAIN, "alert filter: track", "", "Regular expression, if it's set, only matching alerts  will be sent");

        propIgnorelistOn = config.get(CATEGORY_MAIN, "enable ignorelist",
                true, "Ignore players in ignore list." +
                        "\nThis applies to snitch/logout overlays and the proximity ping sound.");
        propTracklistOn = config.get(CATEGORY_MAIN, "enable tracklist",
                false, "Only show players in track list." +
                        "\nThis applies to snitch/logout overlays and the proximity ping sound.");
        propIgnorelist = config.get(CATEGORY_MAIN, "ignored players",
                new String[]{}, "If enabled, these players will NOT show up, even if they're also in the track list" +
                        "\nThis applies to snitch/logout overlays and the proximity ping sound.");
        propTracklist = config.get(CATEGORY_MAIN, "tracked players",
                new String[]{}, "If enabled, ONLY these players will show up (unless they're also in the ignore list)" +
                        "\nThis applies to snitch/logout overlays and the proximity ping sound.");

        propignoredGroupsOn = config.get(CATEGORY_MAIN, "enable ignored groups", false, "Ignore snitches on listed groups.");
        proptrackedGroupsOn = config.get(CATEGORY_MAIN, "enable tracked groups", false, "Only show snitches on listed groups");
        propignoredGroups = config.get(CATEGORY_MAIN, "ignored groups",
                new String[]{}, "If enabled, snitches on these groups will NOT show up, even if they're also in the ignored groups list.");
        proptrackedGroups = config.get(CATEGORY_MAIN, "tracked groups",
                new String[]{}, "If enabled, ONLY snitches on these groups will show up (unless they're also in the ignored groups list).");

        List<String> menuItems = new ArrayList<>(Arrays.asList("webhook url", "enabled",
                "alert format",
                "alert filter: track", "alert filter: ignore",
                "enable tracked groups", "tracked groups",
                "enable ignored groups", "ignored groups",
                "enable tracklist", "tracked players",
                "enable ignorelist", "ignored players"));
        config.setCategoryPropertyOrder(CATEGORY_MAIN, menuItems);
    }

    /**
     * called every time a prop is changed, to apply the new values to the fields and to save the values to the config file
     */
    private void syncValues() {
        enabled = propEnabled.getBoolean();
        alertFormat = propAlertFormat.getString();
        webhookUrl = propWebhookUrl.getString();

        Minecraft mc = Minecraft.getMinecraft();

        try {
            alertIgnoreFilter = null;
            if (propAlertIgnoreFilter.getString().length() > 0)
                alertIgnoreFilter = Pattern.compile(propAlertIgnoreFilter.getString());
        } catch (PatternSyntaxException e) {
            String msg = String.format("[SnitchCord] Error: incorrect regex in filter for ignored alerts: %s", e.getMessage());
            if (mc != null && mc.ingameGUI != null && mc.ingameGUI.getChatGUI() != null) {
                mc.ingameGUI.getChatGUI().printChatMessage(new TextComponentString(msg));
            }
            SnitchcordMod.logger.error(msg);
        }

        try {
            alertTrackFilter = null;
            if (propAlertTrackFilter.getString().length() > 0)
                alertTrackFilter = Pattern.compile(propAlertTrackFilter.getString());
        } catch (PatternSyntaxException e) {
            String msg = String.format("[SnitchCord] Error: incorrect regex in filter for tracked alerts: %s", e.getMessage());
            if (mc != null && mc.ingameGUI != null && mc.ingameGUI.getChatGUI() != null) {
                mc.ingameGUI.getChatGUI().printChatMessage(new TextComponentString(msg));
            }
            SnitchcordMod.logger.error(msg);
        }

        ignorelistOn = propIgnorelistOn.getBoolean();
        tracklistOn = propTracklistOn.getBoolean();

        String[] ignorelistArr = propIgnorelist.getStringList();
        ignorelist = new HashSet<>(ignorelistArr.length);
        for (String s : ignorelistArr) {
            ignorelist.add(s.toLowerCase());
        }

        String[] tracklistArr = propTracklist.getStringList();
        tracklist = new HashSet<>(tracklistArr.length);
        for (String s : tracklistArr) {
            tracklist.add(s.toLowerCase());
        }

        ignoredGroupsOn = propignoredGroupsOn.getBoolean();
        trackedGroupsOn = proptrackedGroupsOn.getBoolean();

        String[] ignoredGroupsArr = propignoredGroups.getStringList();
        ignoredGroups = new HashSet<>(ignoredGroupsArr.length);
        for (String s : ignoredGroupsArr) {
            ignoredGroups.add(s.toLowerCase());
        }

        String[] trackedGroupsArr = proptrackedGroups.getStringList();
        trackedGroups = new HashSet<>(trackedGroupsArr.length);
        for (String s : trackedGroupsArr) {
            trackedGroups.add(s.toLowerCase());
        }

        if (config.hasChanged()) {
            config.save();
            syncProperties();
            SnitchcordMod.logger.info("Saved " + SnitchcordMod.MOD_NAME + " config.");
        }
    }
}
