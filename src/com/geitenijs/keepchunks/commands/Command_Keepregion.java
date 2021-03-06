package com.geitenijs.keepchunks.commands;

import com.geitenijs.keepchunks.Hooks;
import com.geitenijs.keepchunks.Strings;
import com.geitenijs.keepchunks.Utilities;
import com.geitenijs.keepchunks.commands.hooks.Keepregion_WE;
import com.geitenijs.keepchunks.commands.hooks.Keepregion_WG;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Integer.max;
import static java.lang.Integer.min;

public class Command_Keepregion implements CommandExecutor, TabCompleter {

    private CommandExecutor WEKeepregion;
    private CommandExecutor WGKeepregion;
    private TabCompleter WEKeepregionTab;
    private TabCompleter WGKeepregionTab;

    Command_Keepregion() {
        if (Hooks.WorldEdit) {
            WEKeepregion = new Keepregion_WE();
            WEKeepregionTab = new Keepregion_WE();
        }
        if (Hooks.WorldGuard) {
            WGKeepregion = new Keepregion_WG();
            WGKeepregionTab = new Keepregion_WG();
        }
    }

    public boolean onCommand(final CommandSender s, final Command c, final String label, final String[] args) {
        if (args.length == 2) {
            if (args[1].equalsIgnoreCase("worldedit")) {
                if (Hooks.WorldEdit) {
                    return WEKeepregion.onCommand(s, c, label, args);
                } else if (Hooks.incompatibleWorldEdit) {
                    Utilities.msg(s, Strings.UPDATEWE);
                } else {
                    Utilities.msg(s, Strings.NOWE);
                }
            } else {
                Utilities.msg(s, Strings.KEEPREGIONUSAGE);
            }
        } else if (args.length == 4) {
            if (args[1].equalsIgnoreCase("worldguard")) {
                if (Hooks.WorldGuard) {
                    return WGKeepregion.onCommand(s, c, label, args);
                } else if (Hooks.incompatibleWorldGuard) {
                    Utilities.msg(s, Strings.UPDATEWG);
                } else {
                    Utilities.msg(s, Strings.NOWG);
                }
            } else {
                Utilities.msg(s, Strings.KEEPREGIONUSAGE);
            }
        } else if (args.length == 7) {
            if (args[1].equalsIgnoreCase("coords")) {
                try {
                    final int X1 = Integer.parseInt(args[2]);
                    final int Z1 = Integer.parseInt(args[3]);
                    final int X2 = Integer.parseInt(args[4]);
                    final int Z2 = Integer.parseInt(args[5]);
                    final int minX = min(X1, X2);
                    final int minZ = min(Z1, Z2);
                    final int maxX = max(X1, X2);
                    final int maxZ = max(Z1, Z2);
                    final String world = args[6];
                    if (Bukkit.getWorld(world) == null) {
                        Utilities.msg(s, "&cWorld &f'" + world + "'&c doesn't exist, or isn't loaded in memory.");
                        return false;
                    }
                    Utilities.msg(s, "&fMarking chunks between &9(" + minX + ", " + minZ + ")&f and &9(" + maxX + ", " + maxZ + ")&f in world &6'" + world + "'&f...");
                    for (int x = minX; x <= maxX; ++x) {
                        for (int z = minZ; z <= maxZ; ++z) {
                            final String chunk = x + "#" + z + "#" + world;
                            if (Utilities.chunks.contains(chunk)) {
                                if (Utilities.config.getBoolean("general.debug")) {
                                    Utilities.consoleMsg(Strings.DEBUGPREFIX + "Chunk (" + x + "," + z + ") in world '" + world + "' is already marked.");
                                }
                            } else {
                                if (Utilities.config.getBoolean("general.debug")) {
                                    Utilities.consoleMsg(Strings.DEBUGPREFIX + "Marking chunk (" + x + "," + z + ") in world '" + world + "'...");
                                }
                                Utilities.chunks.add(chunk);
                                Bukkit.getServer().getWorld(world).loadChunk(x, z);
                                Bukkit.getServer().getWorld(world).setChunkForceLoaded(x, z, true);
                            }
                        }
                    }
                    Utilities.data.set("chunks", new ArrayList<>(Utilities.chunks));
                    Utilities.saveDataFile();
                    Utilities.reloadDataFile();
                    Utilities.msg(s, "&fMarked chunks between &9(" + minX + ", " + minZ + ")&f and &9(" + maxX + ", " + maxZ + ")&f in world &6'" + world + "'&f.");
                } catch (NumberFormatException ex) {
                    Utilities.msg(s, Strings.UNUSABLE);
                }
            } else {
                Utilities.msg(s, Strings.KEEPREGIONUSAGE);
            }
        } else {
            Utilities.msg(s, Strings.KEEPREGIONUSAGE);
        }
        return true;
    }

    public List<String> onTabComplete(CommandSender s, Command c, String label, String[] args) {
        ArrayList<String> tabs = new ArrayList<>();
        String[] newArgs = CommandWrapper.getArgs(args);
        if (newArgs.length == 1) {
            tabs.add("coords");
            tabs.add("worldedit");
            tabs.add("worldguard");
        }
        if (args[1].equals("coords")) {
            if (s instanceof Player) {
                Player player = (Player) s;
                Location loc = player.getLocation();
                if (Hooks.WorldEdit) {
                    return WEKeepregionTab.onTabComplete(s, c, label, args);
                } else {
                    if (newArgs.length == 2) {
                        tabs.add(String.valueOf(loc.getChunk().getX()));
                    }
                    if (newArgs.length == 3) {
                        tabs.add(String.valueOf(loc.getChunk().getZ()));
                    }
                    if (newArgs.length == 4) {
                        tabs.add(String.valueOf(loc.getChunk().getX()));
                    }
                    if (newArgs.length == 5) {
                        tabs.add(String.valueOf(loc.getChunk().getZ()));
                    }
                    if (newArgs.length == 6) {
                        tabs.add(loc.getWorld().getName());
                    }
                    if (newArgs.length > 6) {
                        tabs.clear();
                    }
                    return CommandWrapper.filterTabs(tabs, args);
                }
            } else {
                if (newArgs.length == 2) {
                    tabs.add("<0>");
                }
                if (newArgs.length == 3) {
                    tabs.add("<0>");
                }
                if (newArgs.length == 4) {
                    tabs.add("<0>");
                }
                if (newArgs.length == 5) {
                    tabs.add("<0>");
                }
                if (newArgs.length == 6) {
                    tabs.add("<world>");
                }
                if (newArgs.length > 6) {
                    tabs.clear();
                }
            }
        }
        if (args[1].equals("worldedit")) {
            if (Hooks.WorldEdit) {
                return WEKeepregionTab.onTabComplete(s, c, label, args);
            }
        }
        if (args[1].equals("worldguard")) {
            if (Hooks.WorldGuard) {
                return WGKeepregionTab.onTabComplete(s, c, label, args);
            }
        }
        return CommandWrapper.filterTabs(tabs, args);
    }
}