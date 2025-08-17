package easy.vnnightcoderzzz.easymanager;

import org.bukkit.*;
import org.bukkit.BanEntry;
import org.bukkit.BanList;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class EasyManager extends JavaPlugin implements Listener {

    // ---- Status & Configuration ----
    private boolean lockdownEnabled = false;

    // Plugin's own whitelist
    private boolean emWhitelistEnabled = false;
    private final Set<String> emWhitelist = new HashSet<>();
    private String emWhitelistKickMessage = ChatColor.RED + "You are not on the server's whitelist!";

    // HTU (How-To-Use)
    private final Map<String, List<String>> commandHelp = new LinkedHashMap<>();

    // ---- Life cycle ----
    @Override
    public void onEnable() {
        loadConfigValues();

        // Load command help
        setupCommandHelp();

        // Register listener
        Bukkit.getPluginManager().registerEvents(this, this);

        getLogger().info("EasyManager has been enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("EasyManager has been disabled!");
    }

    private void loadConfigValues() {
        // create default nodes if they don't exist
        getConfig().addDefault("whitelist.enabled", false);
        getConfig().addDefault("whitelist.list", new ArrayList<>());
        getConfig().addDefault("whitelist.kickMessage", emWhitelistKickMessage);
        getConfig().addDefault("lockdown.enabled", false);
        getConfig().options().copyDefaults(true);
        saveConfig();

        emWhitelistEnabled = getConfig().getBoolean("whitelist.enabled", false);
        emWhitelist.clear();
        for (String n : getConfig().getStringList("whitelist.list")) {
            emWhitelist.add(n.toLowerCase(Locale.ROOT));
        }
        emWhitelistKickMessage = ChatColor.translateAlternateColorCodes('&',
                getConfig().getString("whitelist.kickMessage", emWhitelistKickMessage));
        lockdownEnabled = getConfig().getBoolean("lockdown.enabled", false);
    }

    private void saveWhitelistToConfig() {
        getConfig().set("whitelist.enabled", emWhitelistEnabled);
        getConfig().set("whitelist.list", new ArrayList<>(emWhitelist));
        getConfig().set("whitelist.kickMessage", emWhitelistKickMessage);
        saveConfig();
    }

    private void saveLockdownToConfig() {
        getConfig().set("lockdown.enabled", lockdownEnabled);
        saveConfig();
    }

    // ---- Listener: server lockdown & custom whitelist ----
    @EventHandler
    public void onLogin(PlayerLoginEvent e) {
        Player p = e.getPlayer();
        // Lockdown: block everyone except ops or those with bypass permission
        if (lockdownEnabled && !(p.isOp() || p.hasPermission("easymanager.lockdown.bypass"))) {
            e.disallow(PlayerLoginEvent.Result.KICK_OTHER,
                    ChatColor.RED + "The server is temporarily locked down. Please try again later.");
            return;
        }

        // Custom whitelist
        if (emWhitelistEnabled) {
            if (!emWhitelist.contains(p.getName().toLowerCase(Locale.ROOT))) {
                e.disallow(PlayerLoginEvent.Result.KICK_WHITELIST, emWhitelistKickMessage);
            }
        }
    }

    // In case pre-login needs to be locked (less chatty, but for security)
    @EventHandler
    public void onPreLogin(AsyncPlayerPreLoginEvent e) {
        if (lockdownEnabled) {
            // Permissions are unknown at this stage, so just a general warning
            // Mainly rely on PlayerLoginEvent to check for bypass permissions
        }
    }

    // ---- HTU (How-To-Use) ----
    private void setupCommandHelp() {
        commandHelp.put("easyban", Arrays.asList(
                header("easyban"),
                "§eSyntax:",
                "   §a/easyban <player_name> [reason]",
                "§eDefault reason:",
                "   §7\"You have been banned by <banner_name>\"",
                "§eExample:",
                "   /easyban Steve",
                "   /easyban Alex HackClient"
        ));
        commandHelp.put("easyunban", Arrays.asList(
                header("easyunban"),
                "§eSyntax:",
                "   §a/easyunban <player_name>",
                "§eExample:",
                "   /easyunban Steve"
        ));
        commandHelp.put("easybanip", Arrays.asList(
                header("easybanip"),
                "§eSyntax:",
                "   §a/easybanip <player_name/IP> [reason]",
                "§eDefault reason:",
                "   §7\"You have been banned by <banner_name>\"",
                "§eExample:",
                "   /easybanip 192.168.1.2",
                "   /easybanip Steve Toxic"
        ));
        commandHelp.put("easyunbanip", Arrays.asList(
                header("easyunbanip"),
                "§eSyntax:",
                "   §a/easyunbanip <player_name/IP>",
                "§eExample:",
                "   /easyunbanip Steve",
                "   /easyunbanip 192.168.1.2"
        ));
        commandHelp.put("easybetterban", Arrays.asList(
                header("easybetterban"),
                "§eSyntax:",
                "   §a/easybetterban <name> <reason> <serverName> <color> <ddMMyyyy>",
                "§eNotes:",
                "   §7Expiration date format is ddMMyyyy (e.g., §a24022026§7 → 24/02/2026).",
                "   §7Color: ChatColor name (RED, GREEN, GOLD, ...).",
                "§eExample:",
                "   /easybetterban Steve HackClient MyServer RED 24022026"
        ));
        commandHelp.put("easykick", Arrays.asList(
                header("easykick"),
                "§eSyntax:",
                "   §a/easykick <player_name> [reason]",
                "§eExample:",
                "   /easykick Steve",
                "   /easykick Alex SpamChat"
        ));
        commandHelp.put("easylist", Arrays.asList(
                header("easylist"),
                "§eShow a list of online players + ping (if available).",
                "§eExample:",
                "   /easylist"
        ));
        commandHelp.put("easybroadcast", Arrays.asList(
                header("easybroadcast"),
                "§eSyntax:",
                "   §a/easybroadcast <content>",
                "§eExample:",
                "   /easybroadcast The server will be under maintenance in 5 minutes!"
        ));
        commandHelp.put("easytps", Arrays.asList(
                header("easytps"),
                "§eDisplays TPS (if server API supports it).",
                "§eExample:",
                "   /easytps"
        ));
        commandHelp.put("easyonline", Arrays.asList(
                header("easyonline"),
                "§eDisplays the number of online players.",
                "§eExample:",
                "   /easyonline"
        ));
        commandHelp.put("easyclearlag", Arrays.asList(
                header("easyclearlag"),
                "§eRemoves dropped items and some entities to reduce lag.",
                "§eExample:",
                "   /easyclearlag"
        ));
        commandHelp.put("easywhitelist", Arrays.asList(
                header("easywhitelist"),
                "§eSyntax:",
                "   §a/easywhitelist add <name>",
                "   §a/easywhitelist remove <name>",
                "   §a/easywhitelist list",
                "   §a/easywhitelist enable",
                "   §a/easywhitelist disable",
                "   §a/easywhitelist content <kick_message_content>",
                "§eExample:",
                "   /easywhitelist add Steve",
                "   /easywhitelist disable"
        ));
        commandHelp.put("easylockdown", Arrays.asList(
                header("easylockdown"),
                "§eSyntax:",
                "   §a/easylockdown on",
                "   §a/easylockdown off",
                "§eNotes:",
                "   §7When enabled, only OP or players with §aeasymanager.lockdown.bypass §7permission can join."
        ));
        commandHelp.put("easystop", Arrays.asList(
                header("easystop"),
                "§eShuts down the server immediately.",
                "§eExample:",
                "   /easystop"
        ));
        commandHelp.put("easyrestart", Arrays.asList(
                header("easyrestart"),
                "§eExecutes the §a/restart §ecommand from the console.",
                "§eExample:",
                "   /easyrestart"
        ));
        commandHelp.put("easyhtu", Arrays.asList(
                header("easyhtu"),
                "§eSyntax:",
                "   §a/easyhtu                 §7// lists all commands",
                "   §a/easyhtu <keyword>       §7// suggests commands containing the keyword",
                "   §a/easyhtu <command_name>  §7// detailed command help",
                "§eExample:",
                "   /easyhtu ban",
                "   /easyhtu easyban"
        ));
    }

    private String header(String cmd) {
        return "§6[EasyManager] §eHow to use: §a/" + cmd;
    }

    // ---- Helpers ----
    private String defaultReason(CommandSender sender) {
        String who = (sender != null) ? sender.getName() : "Console";
        return "You have been banned by " + who;
    }

    private Date parseDateDDMMYYYY(String raw) throws ParseException {
        // accepts 7-8 digits (e.g., 2422026 -> 24/2/2026, 24022026 -> 24/02/2026)
        String digits = raw.replaceAll("[^0-9]", "");
        if (digits.length() < 7 || digits.length() > 8) {
            throw new ParseException("Invalid date format", 0);
        }
        // left-pad to 8 digits
        while (digits.length() < 8) digits = "0" + digits; // 02422026 -> 02/42/2026 (ugly). Users should ideally enter 8 digits.
        SimpleDateFormat df = new SimpleDateFormat("ddMMyyyy");
        df.setLenient(false);
        return df.parse(digits);
    }

    private ChatColor parseColor(String name, ChatColor def) {
        if (name == null) return def;
        try {
            return ChatColor.valueOf(name.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            return def;
        }
    }

    private String formatDateVietnam(Date d) {
        return new SimpleDateFormat("dd/MM/yyyy").format(d);
    }

    private Integer safePing(Player p) {
        try {
            // Spigot/Paper has Player#getPing()
            return (Integer) Player.class.getMethod("getPing").invoke(p);
        } catch (Throwable ignore) {
            return null;
        }
    }

    private String joinArgs(String[] args, int from) {
        if (from >= args.length) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = from; i < args.length; i++) {
            if (i > from) sb.append(' ');
            sb.append(args[i]);
        }
        return sb.toString();
    }

    // ---- Commands ----
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        String name = cmd.getName().toLowerCase(Locale.ROOT);

        switch (name) {
            case "easyban": return cmdEasyBan(sender, args);
            case "easyunban": return cmdEasyUnban(sender, args);
            case "easybanip": return cmdEasyBanIp(sender, args);
            case "easyunbanip": return cmdEasyUnbanIp(sender, args);
            case "easykick": return cmdEasyKick(sender, args);
            case "easylist": return cmdEasyList(sender);
            case "easybroadcast": return cmdEasyBroadcast(sender, args);
            case "easytps": return cmdEasyTps(sender);
            case "easyonline": return cmdEasyOnline(sender);
            case "easyclearlag": return cmdEasyClearLag(sender);
            case "easywhitelist": return cmdEasyWhitelist(sender, args);
            case "easylockdown": return cmdEasyLockdown(sender, args);
            case "easystop": return cmdEasyStop(sender);
            case "easyrestart": return cmdEasyRestart(sender);
            case "easybetterban": return cmdEasyBetterBan(sender, args);
            case "easyhtu": return cmdEasyHTU(sender, args);
            default: return false;
        }
    }

    // /easyban <player> [reason]
    // /easyban <player> [reason]
private boolean cmdEasyBan(CommandSender sender, String[] args) {
    if (args.length < 1) {
        sender.sendMessage("§cUsage: /easyban <name> [reason]");
        return true;
    }
    String target = args[0];
    String reason = (args.length >= 2) ? joinArgs(args, 1) : defaultReason(sender);

    BanList banList = Bukkit.getBanList(BanList.Type.NAME);
    // Cast null thành Instant để tránh lỗi ambiguous
    banList.addBan(target, reason, (java.util.Date) null, sender.getName());

    Player p = Bukkit.getPlayerExact(target);
    if (p != null) p.kickPlayer(ChatColor.RED + reason);

    sender.sendMessage("§aBanned §e" + target + "§a. Reason: §7" + reason);
    return true;
}

// /easyunban <player>
private boolean cmdEasyUnban(CommandSender sender, String[] args) {
    if (args.length < 1) {
        sender.sendMessage("§cUsage: /easyunban <name>");
        return true;
    }
    String target = args[0];
    Bukkit.getBanList(BanList.Type.NAME).pardon(target);
    sender.sendMessage("§aUnbanned §e" + target);
    return true;
}

// /easybanip <player/ip> [reason]
private boolean cmdEasyBanIp(CommandSender sender, String[] args) {
    if (args.length < 1) {
        sender.sendMessage("§cUsage: /easybanip <name or IP> [reason]");
        return true;
    }
    String who = args[0];
    String reason = (args.length >= 2) ? joinArgs(args, 1) : defaultReason(sender);

    // Nếu là player, lấy IP
    String ip = who;
    Player online = Bukkit.getPlayerExact(who);
    if (online != null && online.getAddress() != null) {
        ip = online.getAddress().getAddress().getHostAddress();
    }

    Bukkit.getBanList(BanList.Type.IP).addBan(ip, reason, (java.util.Date) null, sender.getName());

    // Kick tất cả người chơi có cùng IP
    for (Player p : Bukkit.getOnlinePlayers()) {
        if (p.getAddress() != null && p.getAddress().getAddress().getHostAddress().equals(ip)) {
            p.kickPlayer(ChatColor.RED + reason);
        }
    }

    sender.sendMessage("§aBanned IP §e" + ip + "§a. Reason: §7" + reason);
    return true;
}

// /easyunbanip <player/ip>
private boolean cmdEasyUnbanIp(CommandSender sender, String[] args) {
    if (args.length < 1) {
        sender.sendMessage("§cUsage: /easyunbanip <name or IP>");
        return true;
    }
    String who = args[0];
    String ip = who;

    Player online = Bukkit.getPlayerExact(who);
    if (online != null && online.getAddress() != null) {
        ip = online.getAddress().getAddress().getHostAddress();
    }

    Bukkit.getBanList(BanList.Type.IP).pardon(ip);
    sender.sendMessage("§aUnbanned IP §e" + ip);
    return true;
}


    // /easykick <player> [reason]
    private boolean cmdEasyKick(CommandSender sender, String[] args) {
        if (args.length < 1) {
            sender.sendMessage("§cUsage: /easykick <name> [reason]");
            return true;
        }
        Player p = Bukkit.getPlayerExact(args[0]);
        if (p == null) {
            sender.sendMessage("§cPlayer not found: " + args[0]);
            return true;
        }
        String reason = (args.length >= 2) ? joinArgs(args, 1) : "Kicked by " + sender.getName();
        p.kickPlayer(ChatColor.RED + reason);
        sender.sendMessage("§aKicked §e" + p.getName() + "§a. Reason: §7" + reason);
        return true;
    }

    // /easylist
    private boolean cmdEasyList(CommandSender sender) {
        Collection<? extends Player> players = Bukkit.getOnlinePlayers();
        sender.sendMessage("§6[EasyManager] §eOnline players: §a" + players.size());
        for (Player p : players) {
            Integer ping = safePing(p);
            String pingStr = (ping == null) ? "N/A" : (ping + "ms");
            sender.sendMessage("  §a" + p.getName() + " §7(" + pingStr + ")");
        }
        return true;
    }

    // /easybroadcast <msg>
    private boolean cmdEasyBroadcast(CommandSender sender, String[] args) {
        if (args.length < 1) {
            sender.sendMessage("§cUsage: /easybroadcast <content>");
            return true;
        }
        String msg = ChatColor.translateAlternateColorCodes('&', joinArgs(args, 0));
        Bukkit.broadcastMessage("§6[Announcement] §f" + msg);
        return true;
    }

    // /easytps
    private boolean cmdEasyTps(CommandSender sender) {
        try {
            // Paper/Spigot might have getTPS()
            double[] tps = (double[]) Bukkit.getServer().getClass().getMethod("getTPS").invoke(Bukkit.getServer());
            String tps1 = String.format(Locale.US, "%.2f", tps[0]);
            String tps5 = String.format(Locale.US, "%.2f", tps[1]);
            String tps15 = String.format(Locale.US, "%.2f", tps[2]);
            sender.sendMessage("§6[EasyManager] §eTPS: §a" + tps1 + " §7/5m:§a " + tps5 + " §7/15m:§a " + tps15);
        } catch (Throwable ex) {
            sender.sendMessage("§cThis server does not support getting TPS via this API.");
        }
        return true;
    }

    // /easyonline
    private boolean cmdEasyOnline(CommandSender sender) {
        sender.sendMessage("§6[EasyManager] §eCurrently online: §a" + Bukkit.getOnlinePlayers().size());
        return true;
    }

    // /easyclearlag
    private boolean cmdEasyClearLag(CommandSender sender) {
        int removed = 0;
        for (World w : Bukkit.getWorlds()) {
            for (Entity e : new ArrayList<>(w.getEntities())) {
                EntityType t = e.getType();
                if (t == EntityType.ITEM || t == EntityType.ARROW || t == EntityType.SPECTRAL_ARROW
                        || t == EntityType.SMALL_FIREBALL || t == EntityType.FIREBALL || t == EntityType.FISHING_BOBBER
                        || t == EntityType.EXPERIENCE_ORB) {
                    e.remove();
                    removed++;
                }
            }
        }
        sender.sendMessage("§aRemoved §e" + removed + " §aentities to reduce lag.");
        return true;
    }

    // /easywhitelist ...
    private boolean cmdEasyWhitelist(CommandSender sender, String[] args) {
        if (args.length < 1) {
            sender.sendMessage("§cUsage: /easywhitelist <add/remove/list/enable/disable/content>");
            return true;
        }
        String sub = args[0].toLowerCase(Locale.ROOT);
        switch (sub) {
            case "add": {
                if (args.length < 2) {
                    sender.sendMessage("§cUsage: /easywhitelist add <name>");
                    return true;
                }
                String name = args[1].toLowerCase(Locale.ROOT);
                if (emWhitelist.add(name)) {
                    saveWhitelistToConfig();
                    sender.sendMessage("§aAdded §e" + name + " §ato the whitelist.");
                } else sender.sendMessage("§e" + name + " §ais already on the whitelist.");
                return true;
            }
            case "remove": {
                if (args.length < 2) {
                    sender.sendMessage("§cUsage: /easywhitelist remove <name>");
                    return true;
                }
                String name = args[1].toLowerCase(Locale.ROOT);
                if (emWhitelist.remove(name)) {
                    saveWhitelistToConfig();
                    sender.sendMessage("§aRemoved §e" + name + " §afrom the whitelist.");
                } else sender.sendMessage("§e" + name + " §cis not on the whitelist.");
                return true;
            }
            case "list": {
                sender.sendMessage("§6[EasyManager] §eWhitelist (" + emWhitelist.size() + "):");
                for (String n : emWhitelist) sender.sendMessage("  §a" + n);
                return true;
            }
            case "enable": {
                emWhitelistEnabled = true;
                saveWhitelistToConfig();
                sender.sendMessage("§aEasyManager's custom whitelist has been enabled.");
                return true;
            }
            case "disable": {
                emWhitelistEnabled = false;
                saveWhitelistToConfig();
                sender.sendMessage("§eEasyManager's custom whitelist has been disabled.");
                return true;
            }
            case "content": {
                if (args.length < 2) {
                    sender.sendMessage("§cUsage: /easywhitelist content <kick_message_content>");
                    return true;
                }
                String content = joinArgs(args, 1);
                emWhitelistKickMessage = ChatColor.translateAlternateColorCodes('&', content);
                saveWhitelistToConfig();
                sender.sendMessage("§aSet the kick message for players not on the whitelist.");
                return true;
            }
            default:
                sender.sendMessage("§cInvalid subcommand. Usage: add/remove/list/enable/disable/content");
                return true;
        }
    }

    // /easylockdown on|off
    private boolean cmdEasyLockdown(CommandSender sender, String[] args) {
        if (!sender.isOp()) {
            sender.sendMessage("§cYou do not have permission to use this command!");
             return true;
       }

        if (args.length < 1) {
            sender.sendMessage("§cUsage: /easylockdown <on|off>");
            return true;
        }
        String val = args[0].toLowerCase(Locale.ROOT);
        if (val.equals("on")) {
            lockdownEnabled = true;
            saveLockdownToConfig();
            sender.sendMessage("§cLockdown: §aENABLED");
        } else if (val.equals("off")) {
            lockdownEnabled = false;
            saveLockdownToConfig();
            sender.sendMessage("§cLockdown: §eDISABLED");
        } else {
            sender.sendMessage("§cUsage: /easylockdown <on|off>");
        }
        return true;
    }

    // /easystop
    private boolean cmdEasyStop(CommandSender sender) {
        sender.sendMessage("§cThe server will now shut down...");
        Bukkit.shutdown();
        return true;
    }

    // /easyrestart
    private boolean cmdEasyRestart(CommandSender sender) {
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "restart");
        return true;
    }

    // /easybetterban <player> <reason> <serverName> <color> <ddMMyyyy>
    private boolean cmdEasyBetterBan(CommandSender sender, String[] args) {
        if (args.length < 5) {
            sender.sendMessage("§cUsage: /easybetterban <name> <reason> <serverName> <color> <ddMMyyyy>");
            return true;
        }
        String playerName = args[0];
        String reason = args[1];
        String serverName = args[2];
        ChatColor color = parseColor(args[3], ChatColor.GOLD);
        Date until;
        try {
            until = parseDateDDMMYYYY(args[4]);
        } catch (ParseException e) {
            sender.sendMessage("§cInvalid date. Please enter in ddMMyyyy format (e.g., 24022026).");
            return true;
        }

        // Create a temporary ban entry
        BanList banList = Bukkit.getBanList(BanList.Type.NAME);
        String who = sender.getName();
        String niceDate = formatDateVietnam(until);

        // Kick message to display to the banned player (in the requested format)
        StringBuilder sb = new StringBuilder();
        sb.append(color).append(serverName).append("\n")
          .append(ChatColor.WHITE).append("Reason: ").append(reason).append("\n")
          .append(ChatColor.WHITE).append("Banned By: ").append(who).append("\n")
          .append(ChatColor.WHITE).append("The Ban Will End On: ").append(niceDate);

        BanEntry entry = banList.addBan(playerName, sb.toString(), until, who);

        // Kick if currently online
        Player p = Bukkit.getPlayerExact(playerName);
        if (p != null) {
            p.kickPlayer(sb.toString());
        }

        sender.sendMessage("§aBetter-banned §e" + playerName + "§a until §e" + niceDate + "§a.");
        return true;
    }

    // /easyhtu [keyword|command]
    private boolean cmdEasyHTU(CommandSender sender, String[] args) {
        if (args.length == 0) {
            sender.sendMessage("§6[EasyManager] §eList of commands:");
            for (String key : commandHelp.keySet()) sender.sendMessage("  §a" + key);
            sender.sendMessage("§7Use §a/easyhtu <keyword> §7to get suggestions, or §a/easyhtu <command_name> §7to see detailed help.");
            return true;
        }

        String q = args[0].toLowerCase(Locale.ROOT);
        if (commandHelp.containsKey(q)) {
            for (String line : commandHelp.get(q)) sender.sendMessage(line);
            return true;
        }

        // suggest by keyword
        List<String> hits = new ArrayList<>();
        for (String key : commandHelp.keySet()) if (key.contains(q)) hits.add(key);

        if (hits.isEmpty()) {
            sender.sendMessage("§cNo commands found matching the keyword: §e" + q);
        } else {
            sender.sendMessage("§6[EasyManager] §eCommand suggestions for '" + q + "':");
            for (String k : hits) sender.sendMessage("  §a" + k + " §7// " + shortDesc(k));
        }
        return true;
    }

    private String shortDesc(String key) {
        switch (key) {
            case "easyban": return "bans a player";
            case "easyunban": return "unbans a player";
            case "easybanip": return "bans by IP";
            case "easyunbanip": return "unbans by IP";
            case "easybetterban": return "temporary advanced ban";
            case "easykick": return "kicks a player";
            case "easylist": return "list players + ping";
            case "easybroadcast": return "broadcasts a message to the server";
            case "easytps": return "check TPS";
            case "easyonline": return "count online players";
            case "easyclearlag": return "removes entities to reduce lag";
            case "easywhitelist": return "manages custom whitelist";
            case "easylockdown": return "temporarily locks down the server";
            case "easystop": return "shuts down the server";
            case "easyrestart": return "restarts the server";
            case "easyhtu": return "how to use";
            default: return "";
        }
    }
}
