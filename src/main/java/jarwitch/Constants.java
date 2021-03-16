package jarwitch;

import jarwitch.commands.*;
import jarwitch.data.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.LinkedHashMap;
import java.util.Map;

public class Constants {
    private static final Logger log = LogManager.getLogger(Constants.class);
    public static final String ZWSP = "\u200B";
    
    private static <T> Class<? extends Command> subclass(Class<T> c) {
        return c.asSubclass(Command.class);
    }

    public static final Map<String, Pair<Class<? extends Command>, String[]>> commands = generateCommands();

    private static Map<String, Pair<Class<? extends Command>, String[]>> generateCommands() {
        log.debug("generating map");
        Map<String, Pair<Class<? extends Command>, String[]>> map = new LinkedHashMap<>();
        // General
////        map.put("about", Pair.of(subclass(About.class), new String[0]));
        map.put("help", Pair.of(subclass(Help.class), new String[]{"?", "what"}));
        map.put("invite", Pair.of(subclass(Invite.class), new String[0]));
        map.put("ping", Pair.of(subclass(Ping.class), new String[0]));
//        map.put("permissions", Pair.of(subclass(Permissions.class), new String[]{"perm"}));
////        map.put("hello", Pair.of(subclass(Hello.class), new String[]{"hi"}));
////        map.put("repeat", Pair.of(subclass(Repeat.class), new String[0]));
////        map.put("whoisironman", Pair.of(subclass(WhoIsIronMan.class), new String[]{"whoisrealironman"}));

        // Settings
////        map.put("backup", Pair.of(subclass(Backup.class), new String[0]));
////        map.put("reload", Pair.of(subclass(Reload.class), new String[]{"refresh"}));
////        map.put("reset", Pair.of(subclass(Reset.class), new String[0]));
        map.put("prefix", Pair.of(subclass(Prefix.class), new String[]{"pre", "pf"}));
        map.put("noprefix", Pair.of(subclass(NoPrefix.class), new String[]{"clearprefix"}));
        map.put("nick", Pair.of(subclass(Nick.class), new String[]{"nickname", "name"}));
        map.put("set", Pair.of(subclass(Set.class), new String[]{"settings", "config"}));

////        // Features
////        map.put("info", Pair.of(subclass(Info.class), new String[]{"userinfo", "infouser", "who", "whois", "usrinfo", "myid", "id"}));
////        map.put("history", Pair.of(subclass(History.class), new String[0]));
////        map.put("purge", Pair.of(subclass(Purge.class), new String[]{"delete", "bulkdelete", "del", "d"}));
////        map.put("movevoice", Pair.of(subclass(MoveVoice.class), new String[]{"mvvoice", "mvv", "mvchannel", "mvchan", "mvvc"}));
////        map.put("mvregex", Pair.of(subclass(MoveRegex.class), new String[0]));
////        map.put("muteall", Pair.of(subclass(MuteAll.class), new String[0]));
////        map.put("unmuteall", Pair.of(subclass(UnmuteAll.class), new String[0]));
////        map.put("disconnectall", Pair.of(subclass(DisconnectAll.class), new String[0]));
////        map.put("hook", Pair.of(subclass(Hook.class), new String[0]));
////        map.put("ask", Pair.of(subclass(Ask.class), new String[]{"calc", "calculate", "wolfram", "wa", "wolframalpha"}));
////        map.put("askimg", Pair.of(subclass(AskImg.class), new String[]{"askpic", "image"}));
////        map.put("rank", Pair.of(subclass(Rank.class), new String[0]));
////        map.put("morse", Pair.of(subclass(Morse.class), new String[0]));
//
////        // Music
////        map.put("play", Pair.of(subclass(Play.class), new String[]{"p"}));
////        map.put("pause", Pair.of(subclass(Pause.class), new String[0]));
////        map.put("resume", Pair.of(subclass(Resume.class), new String[0]));
////        map.put("queue", Pair.of(subclass(Queue.class), new String[]{"q"}));
////        map.put("nowplaying", Pair.of(subclass(NowPlaying.class), new String[]{"np"}));
////        map.put("search", Pair.of(subclass(Search.class), new String[]{"youtube", "yt", "find", "song"}));
////        map.put("join", Pair.of(subclass(Join.class), new String[0]));
////        map.put("leave", Pair.of(subclass(Leave.class), new String[]{"disconneect", "dc"}));
////        map.put("skip", Pair.of(subclass(Skip.class), new String[]{"s"}));
////        map.put("remove", Pair.of(subclass(Remove.class), new String[]{"rm"}));
////        map.put("rmrange", Pair.of(subclass(RemoveRange.class), new String[]{"removerange", "rmr"}));
////        map.put("volume", Pair.of(subclass(Volume.class), new String[]{"vol"}));
////        map.put("loop", Pair.of(subclass(Loop.class), new String[0]));
////        map.put("shuffle", Pair.of(subclass(Shuffle.class), new String[0]));
////        map.put("moveq", Pair.of(subclass(MoveQueue.class), new String[]{"mv", "move", "movequeue"}));
////        map.put("seek", Pair.of(subclass(Seek.class), new String[0]));
////        map.put("clear", Pair.of(subclass(ClearQueue.class), new String[]{"clearqueue"}));
////
        // Misc
////        map.put("rickroll", Pair.of(subclass(Rickroll.class), new String[0]));
////        map.put("duckroll", Pair.of(subclass(Duckroll.class), new String[0]));
        map.put("ip", Pair.of(subclass(IP.class), new String[]{"ipaddress", "ipannounce", "announceip"}));
        map.put("uptime", Pair.of(subclass(Uptime.class), new String[0]));
        map.put("say", Pair.of(subclass(Say.class), new String[]{"echo"}));
        map.put("impersonate", Pair.of(subclass(Impersonate.class), new String[]{"hack", "i"}));

        return map;
    }
}
