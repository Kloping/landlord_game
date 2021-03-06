package com.hrs.kloping;

import com.hrs.kloping.entity.Card;
import com.hrs.kloping.entity.OCardSet;
import net.mamoe.mirai.contact.Group;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author github-kloping
 */
public class Command {
    public static boolean isStarted = false;
    public static boolean isCreated = false;

    public static synchronized boolean isCommand(String text) {
        return text.startsWith("/");
    }

    public static synchronized void exec(String text, long qq, Group group) {
        Object result = null;
        text = text.trim().toLowerCase();
        switch (text) {
            case "创建游戏":
                result = InitTable(group);
                break;
            case "加入游戏":
                if (table != null)
                    result = joinGame(qq);
                break;
            case "抢":
            case "抢地主":
                if (table != null)
                    result = table.rob(qq);
                break;
            case "不抢":
            case "不抢地主":
                if (table != null) result = table.noRob(qq);
                break;
            case "结束游戏":
                if (isCreated && !isStarted) destroy();
                else tipsCantClose(group);
                break;
            case "强制结束游戏":
                if (qq == PluginLandlord.youQQ) destroy();
                else tipsCantClose(group);
                break;
            default:
                break;
        }
        if (result != null)
            group.sendMessage(result.toString());
        else {
            if (!isStarted) return;
            if (text.startsWith("出")) {
                text = text.substring(1).replaceAll(" ", "");
                table.pool(text, qq);
            } else if (text.startsWith("过")) {
                table.pass(qq);
            }
        }
    }

    private static void tipsCantClose(Group group) {
        group.sendMessage("无法结束游戏,可能因为没有创建游戏");
    }

    public static synchronized String joinGame(long qq) {
        if (isCreated && !isStarted) {
            if (table.players.contains(qq))
                return "你已经加入游戏了哦~";
            table.addPlayer(qq);
            return "加入成功";
        } else return "游戏未创建或已开启";
    }

    public static final Map<Long, List<Card>> LIST_IMAGE_MAP = new ConcurrentHashMap<>();
    public static Table table;

    public static synchronized String InitTable(Group group) {
        if (!isCreated) {
            isCreated = true;
            PluginLandlord.threads.execute(() -> {
                if (LIST_IMAGE_MAP.containsKey(group.getId())) {
                    table = new Table(group, LIST_IMAGE_MAP.get(group.getId()));
                } else {
                    List<Card> map = new CopyOnWriteArrayList<>();
                    for (Card card : OCardSet.getCards()) {
                        map.add(card);
                    }
                    LIST_IMAGE_MAP.put(group.getId(), map);
                    table = new Table(group, map);
                }
                group.sendMessage("创建完成!");
            });
            return "创建中...";
        } else return "已经有一个游戏正在...";
    }

    public static void destroy() {
        table.group.sendMessage("对局结束");
        if (table != null) {
            table.destroy();
        }
        isCreated = false;
        isStarted = false;
        table = null;
        System.gc();
    }
}
