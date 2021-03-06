package com.hrs.kloping;

import com.hrs.kloping.entity.Card;
import com.hrs.kloping.entity.OCardSet;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.Member;
import net.mamoe.mirai.message.data.At;
import net.mamoe.mirai.message.data.Face;
import net.mamoe.mirai.message.data.Message;
import net.mamoe.mirai.message.data.MessageChainBuilder;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import static com.hrs.kloping.PluginLandlord.threads;
import static com.hrs.kloping.Utils.*;

/**
 * @author github-kloping
 */
public class Table {
    public static final Random rand = new Random();
    public Group group = null;
    public List<Long> players = new CopyOnWriteArrayList<>();
    public List<Card> cards = new CopyOnWriteArrayList<>();
    public List<Card> ListCards = new ArrayList<>();
    public List<Card> Dcards = new CopyOnWriteArrayList<>();
    private Map<Long, List<Card>> playerCards = new ConcurrentHashMap<>();
    private Map<Long, Map.Entry<Member, Message>> playerEns = new ConcurrentHashMap<>();
    private OCardSet this_cards;

    public void destroy() {
        group = null;
        players.clear();
        cards.clear();
        ListCards.clear();
        Dcards.clear();
        playerCards.clear();
        playerEns.clear();
        this_cards = null;
    }

    public Table(Group group, List<Card> imagecs) {
        destroy();
        this.group = group;
        this.cards.addAll(imagecs);
        for (Card card : cards) {
            this.ListCards.add(card);
        }
    }

    public void addPlayer(long qq) {
        players.add(qq);
        if (players.size() == 3) {
            Command.isStarted = true;
            threads.execute(() -> {
                group.sendMessage("人数足够,游戏开始!");
                for (int i = 0; i < 3; i++) {
                    long q = players.get(i);
                    playerEns.put(q, getEntry(group.get(q), new MessageChainBuilder().append(new At(players.get(i))).append("\r\n").build()));
                }
                group.sendMessage("开始发牌!");
                try {
                    startDeal();
                } catch (Throwable e) {
                    e.printStackTrace();
                    group.sendMessage("发牌异常出错,请重新开始游戏");
                    Command.destroy();
                }
            });
        }
    }

    int r1 = -1, r2 = -1, r3 = -1;

    private void startDeal() throws Throwable {
        initR();
        Dcards.add(ListCards.get(r1));
        Dcards.add(ListCards.get(r2));
        Dcards.add(ListCards.get(r3));
        ListCards.remove(r1);
        ListCards.remove(r2);
        ListCards.remove(r3);
        Collections.shuffle(ListCards);
        int index = 1;
        for (Card card : ListCards) {
            long q = players.get(index++ % 3);
            List<Card> list = playerCards.get(q);
            if (list == null) list = new CopyOnWriteArrayList<>();
            if (list.contains(card)) continue;
            list.add(card);
            playerCards.put(q, list);
        }
        ListCards.clear();
        for (long q : playerCards.keySet()) {
            OCardSet.sort(playerCards.get(q));
            Map.Entry<Member, Message> kv = playerEns.get(q);
            sendThisCards(kv, q);
        }
        group.sendMessage("发牌完成!!");
        group.sendMessage("开始抢地主..");
        startRob();
    }

    private synchronized void sendThisCards(final Map.Entry<Member, Message> kv, final long q) throws Exception{
        kv.getKey().sendMessage(
                new MessageChainBuilder()
                        .append("你这一局的牌是:\r\n")
                        .append(getImageFromFilePath(
                                Drawer.createImage(cards2Images(playerCards.get(q))),
                                kv.getKey())
                        ).build()
        );
    }

    private long landlord = -1;
    private int landlording = -1;

    private void startRob() {
        landlording = 0;
        tipsRob();
    }

    private void tipsRob() {
        long q = players.get(landlording);
        Message message = playerEns.get(q).getValue();
        group.sendMessage(new MessageChainBuilder().append(message).append("轮到你抢地主了").build());
    }

    public String rob(long qq) {
        if (landlording == players.indexOf(qq)) {
            landlording = -2;
            landlord = players.indexOf(qq);
            playerCards.get(qq).addAll(Dcards);
            threads.execute(() -> {
                OCardSet.sort(playerCards.get(qq));
                Map.Entry<Member, Message> kv = playerEns.get(qq);
                kv.getKey().sendMessage(
                        new MessageChainBuilder()
                                .append("您这一局地主的全部牌是:\r\n")
                                .append(getImageFromFilePath(
                                        Drawer.createImage(cards2Images(playerCards.get(qq))),
                                        kv.getKey())
                                ).build()
                );
                tipsPool();
            });
            index = players.indexOf(qq);
            sendLandlordCards();
            return "你抢到地主了!!";
        } else {
            return "还没轮到你抢地主呢";
        }
    }

    private void sendLandlordCards() {
        group.sendMessage(new MessageChainBuilder()
                .append("这一局地主的牌是:\r\n")
                .append(getImageFromFilePath(
                        Drawer.createImage(cards2Images(Dcards)),
                        group)
                ).build());
    }

    public String noRob(long qq) {
        if (landlording == players.indexOf(qq)) {
            landlording++;
            if (landlording < 3) {
                tipsRob();
                return null;
            } else {
                threads.execute(() -> {
                    try {
                        Thread.sleep(500);
                        Command.destroy();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                });
                return "对局结束无人要地主";
            }
        } else {
            return "还没轮到你抢地主呢";
        }
    }

    private void initR() {
        r1 = rand.nextInt(54);
        do {
            r2 = rand.nextInt(53);
        }
        while (r1 == r2);
        do {
            r3 = rand.nextInt(52);
        } while (r1 == r3 || r2 == r3);
    }

    public List<Long> getPlayers() {
        return players;
    }

    public static final Map<Character, Card.En> CHARACTER_2_CARD = new ConcurrentHashMap<>();

    static {
        CHARACTER_2_CARD.put('3', Card.En._3);
        CHARACTER_2_CARD.put('4', Card.En._4);
        CHARACTER_2_CARD.put('5', Card.En._5);
        CHARACTER_2_CARD.put('6', Card.En._6);
        CHARACTER_2_CARD.put('7', Card.En._7);
        CHARACTER_2_CARD.put('8', Card.En._8);
        CHARACTER_2_CARD.put('9', Card.En._9);
        CHARACTER_2_CARD.put('0', Card.En._10);
        CHARACTER_2_CARD.put('j', Card.En._J);
        CHARACTER_2_CARD.put('q', Card.En._Q);
        CHARACTER_2_CARD.put('k', Card.En._K);
        CHARACTER_2_CARD.put('a', Card.En._A);
        CHARACTER_2_CARD.put('2', Card.En._2);
        CHARACTER_2_CARD.put('x', Card.En._X);
        CHARACTER_2_CARD.put('y', Card.En._Y);
    }

    public static synchronized final List<Card> parse2text(String text, List<Card> listCards) {
        List<Card> list = new CopyOnWriteArrayList<>();
        List<Card> nlist = new CopyOnWriteArrayList<>();
        char[] chars = text.trim().toCharArray();
        try {
            for (char c : chars) {
                Card.En en = CHARACTER_2_CARD.get(c);
                int r = findCardByEn(en, listCards, nlist);
                if (r == -1) return null;
                Card card = listCards.get(r);
                list.add(card);
                nlist.add(card);
            }
            return list;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static synchronized final int findCardByEn(Card.En en, List<Card> listCards, List<Card> notN) {
        for (Card card : listCards) {
            if (card.getEn() == en) {
                int i = listCards.indexOf(card);
                if (notN.contains(card)) continue;
                else return i;
            }
        }
        return -1;
    }

    private int index = -1;

    public void pool(String text, long qq) {
        int in = players.indexOf(qq);
        if (in == -1) return;
        if (in != index) {
            tipsNotU(in);
            return;
        }
        List<Card> cards = parse2text(text, playerCards.get(qq));
        if (cards == null) {
            tipsNotEnough();
            return;
        }
        if (istIllegal(cards)) {
            if (this_cards == null) {
                poolNow(qq, cards);
            } else {
                if (isBigger(cards, this_cards.cards)) {
                    poolNow(qq, cards);
                } else {
                    tipsSmaller();
                }
            }
            tipsPool();
        } else {
            tipsIllegal();
        }
    }

    private void poolNow(long qq, List<Card> cards) {
        index = players.indexOf(qq);
        this_cards = new OCardSet(qq);
        this_cards.cards.addAll(cards);
        OCardSet.sort(this_cards.cards);
        playerCards.get(qq).removeAll(this_cards.cards);
        sendThisCards();
        next();
        testWillWin();
        testWin();
    }

    public void pass(long qq) {
        int in = players.indexOf(qq);
        if (in == -1) return;
        if (in != index) {
            tipsNotU(in);
            return;
        }
        if (this_cards != null) {
            next();
            if (index == players.indexOf(this_cards.getQq()))
                this_cards = null;
            tipsPool();
        } else {
            tipsIllegal();
        }
    }

    public void sendThisCards() {
        long qq = players.get(index);
        OCardSet.sort(playerCards.get(qq));
        Map.Entry<Member, Message> kv = playerEns.get(qq);
        kv.getKey().sendMessage(
                new MessageChainBuilder()
                        .append("你这一局剩下的牌是:\r\n")
                        .append(getImageFromFilePath(
                                Drawer.createImage(cards2Images(playerCards.get(qq))),
                                kv.getKey())
                        ).build()
        );
    }

    public static final boolean istIllegal(List<Card> cards) {
        try {
            final int[] values = cards2values(cards);
            Arrays.sort(values);
            if (values.length == 1) return true;
            if (values.length == 2) return values[0] == values[1] || (values[0] == 14 && values[1] == 15);
            if (values.length == 3) {
                return getMaxSameC(values) == 3;
            }
            if (values.length == 4) {
                if (getMaxSameC(values) >= 3) {
                    return true;
                } else if (values[0] == values[1] && values[2] == values[3] && values[1] == values[2]) {
                    return true;
                } else return false;
            }
            if (values.length >= 5) {
                if (Utils.isFly(values) > 0) return true;
                //判断三带二
                if (values.length == 5)
                    if (getMaxSameC(values) == 3)
                        return true;
                //判断顺子
                boolean k = true;
                int v = values[0];
                for (int v1 : values) {
                    if (v1 == v) {
                        v++;
                        continue;
                    } else {
                        k = false;
                        break;
                    }
                }
                if (k) return k;
                //判断连对
                if (values.length >= 6 && values.length % 2 == 0) {
                    k = true;
                    int upV = -1;
                    for (int i = 0; i < values.length; i++) {
                        if (i % 2 == 0) {
                            upV = upV == -1 ? values[i] : upV + 1;
                        } else {
                            if (upV == values[i]) continue;
                            else {
                                k = false;
                                break;
                            }
                        }
                    }
                    if (k) return k;
                }
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 牌比较 cards 比 cards1 大 true
     *
     * @param cards
     * @param cards1
     * @return
     */
    private static boolean isBigger(List<Card> cards, List<Card> cards1) {
        int[] values = cards2values(cards);
        int[] value2s = cards2values(cards1);
        Arrays.sort(value2s);
        Arrays.sort(values);
        if (values.length == 2 && values[0] == 14 && values[1] == 15) {
            return true;
        }
        if (values.length == 1 && value2s.length == 1) {
            return values[0] > value2s[0];
        }
        if (value2s.length == 2 && values.length == 2) {
            return values[0] > value2s[0];
        }
        if (value2s.length == 3 && values.length == 3) {
            return values[0] > value2s[0];
        }
        int vc1 = getMaxSameC(values);
        int vc2 = getMaxSameC(value2s);
        if (values.length == 4) {
            if (value2s.length == 4) {
                if (vc2 < 4 && vc1 == 4) return true;
                if (vc2 == 3 && vc1 == 3) {
                    int v2 = getMaxSameN(value2s);
                    int v1 = getMaxSameN(values);
                    return v1 > v2;
                } else if (vc1 == 4 && vc2 == 4) {
                    return values[0] > value2s[0];
                }
            } else if (value2s.length == 2) {
                if (value2s[0] == 14 && value2s[1] == 15) {
                    return false;
                } else {
                    return true;
                }
            } else {
                return true;
            }
        }
        if (values.length >= 5) {
            if (Utils.isFly(values) > 0 && Utils.isFly(value2s) > 0)
                return Utils.isBiggerFly(values, value2s);
            //判断三带二
            if (values.length == 5) {
                if (getMaxSameC(value2s) == 3 && getMaxSameC(values) == 3) {
                    int v1 = getMaxSameN(values);
                    int v2 = getMaxSameN(value2s);
                    return v1 > v2;
                }
            }
            //判断顺子
            boolean k = true;
            int v = values[0];
            for (int v1 : values) {
                if (v1 == v) {
                    v++;
                    continue;
                } else {
                    k = false;
                }
            }
            if (k) {
                if (values.length == value2s.length) {
                    return values[0] > value2s[0];
                }
            }
            //判断连对
            if (values.length >= 6 && values.length % 2 == 0) {
                k = true;
                int upV = -1;
                for (int i = 0; i < values.length; i++) {
                    if (i % 2 == 0) {
                        upV = upV == -1 ? values[i] : upV + 1;
                    } else {
                        if (upV == values[i]) continue;
                        else {
                            k = false;
                            break;
                        }
                    }
                }
                if (k) {
                    if (values.length == value2s.length) {
                        return values[0] > value2s[0];
                    }
                }
            }
        }
        return false;
    }

    public void tipsPool() {
        long q = players.get(index);
        Message message = playerEns.get(q).getValue();
        if (!overed) {
            if (this_cards == null) {
                group.sendMessage(new MessageChainBuilder()
                        .append(message)
                        .append("轮到你出牌了,你可以随意出")
                        .build()
                );
            } else {
                group.sendMessage(new MessageChainBuilder()
                        .append(message)
                        .append(getNowCardsName(this_cards.cards))
                        .append(getImageFromFilePath(Drawer.createImage(cards2Images(this_cards.cards)), group))
                        .append("轮到你出牌了,这是你要打的牌")
                        .build()
                );
            }
        }
    }

    private static String getNowCardsName(List<Card> cards) {
        final int[] values = cards2values(cards);
        Arrays.sort(values);
        if (values.length == 1) return "";
        if (values.length == 2) return values[0] == values[1] ? "对子" : (values[0] == 14 && values[1] == 15) ? "王炸" : "";
        if (values.length == 3) {
            return getMaxSameC(values) == 3 ? "三个" : "";
        }
        if (values.length == 4) {
            if (getMaxSameC(values) == 3) {
                return "三帯一";
            } else if (getMaxSameC(values) == 4) {
                return "炸弹";
            } else if (values[0] == values[1] && values[2] == values[3] && values[1] == values[2]) {
                return "炸弹";
            } else return "";
        }
        if (values.length >= 5) {
            if (Utils.isFly(values) > 0) return "飞机";
            //判断三带二
            if (values.length == 5)
                if (getMaxSameC(values) == 3)
                    return "三对一对";
            //判断顺子
            boolean k = true;
            int v = values[0];
            for (int v1 : values) {
                if (v1 == v) {
                    v++;
                    continue;
                } else {
                    k = false;
                    break;
                }
            }
            if (k) return "顺子";
            //判断连对
            if (values.length >= 6 && values.length % 2 == 0) {
                k = true;
                int upV = -1;
                for (int i = 0; i < values.length; i++) {
                    if (i % 2 == 0) {
                        upV = upV == -1 ? values[i] : upV + 1;
                    } else {
                        if (upV == values[i]) continue;
                        else {
                            k = false;
                            break;
                        }
                    }
                }
                if (k) return "连对";
            }
        }
        return "";
    }

    private void testWillWin() {
        for (long q : playerCards.keySet()) {
            List<Card> cards = playerCards.get(q);
            if (cards.size() <= 3) {
                tipsWillWin(q, cards.size());
            }
        }
    }

    private void tipsWillWin(long q, int n) {
        if (n <= 0) return;
        Member m = playerEns.get(q).getKey();
        group.sendMessage(new MessageChainBuilder().append(m.getNick()).append("警告!警告!就剩" + n + "张牌了!").build());
    }

    private boolean overed = false;

    private void testWin() {
        for (long q : playerCards.keySet()) {
            List<Card> cards = playerCards.get(q);
            if (cards.size() == 0) {
                overed = true;
                int n = players.indexOf(q);
                if (n == landlord)
                    tipsLandlordWin();
                else
                    tipsCivilianWin();
                threads.execute(() -> {
                    try {
                        Thread.sleep(500);
                        Command.destroy();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                });
            }
        }
    }

    public void tipsCivilianWin() {
        MessageChainBuilder builder = new MessageChainBuilder();
        long lq = players.get((int) landlord);
        for (long q : players) {
            if (q == lq) {
                continue;
            }
            builder.append(new At(q).plus(new Face(Face.DE_YI))).append("\n");
        }
        builder.append(new At(lq).plus(new Face(Face.SHUAI))).append("\n");
        builder.append("平民胜利!!!");
        group.sendMessage(builder.build());
    }

    private void tipsLandlordWin() {
        MessageChainBuilder builder = new MessageChainBuilder();
        long lq = players.get((int) landlord);
        builder.append(new At(lq).plus(new Face(Face.DE_YI))).append("\n");
        for (long q : players) {
            if (q != lq) {
                continue;
            }
            builder.append(new At(q).plus(new Face(Face.SHUAI))).append("\n");
        }
        builder.append("地主胜利!!!");
        group.sendMessage(builder.build());
    }

    private void tipsNotU(int in) {
        long q = players.get(in);
        Message message = playerEns.get(q).getValue();
        group.sendMessage(new MessageChainBuilder().append(message).append("还没轮到你出牌").build());
    }

    private void tipsNotEnough() {
        long q = players.get(index);
        Message message = playerEns.get(q).getValue();
        group.sendMessage(new MessageChainBuilder().append(message).append("您没其中的某个牌").build());
    }

    public void tipsIllegal() {
        long q = players.get(index);
        Message message = playerEns.get(q).getValue();
        group.sendMessage(new MessageChainBuilder().append(message).append("非法的出牌").build());
    }

    public void tipsSmaller() {
        long q = players.get(index);
        Message message = playerEns.get(q).getValue();
        group.sendMessage(new MessageChainBuilder().append(message).append("你的牌打不过").build());
    }

    private void next() {
        index++;
        index = index == 3 ? 0 : index;
    }

    public static void main(String[] args) {
        List<Card> l1 = new CopyOnWriteArrayList<>();
        List<Card> l2 = new CopyOnWriteArrayList<>();
        l1.add(new Card(Card.En._3, Card.Type._V));
        l1.add(new Card(Card.En._4, Card.Type._W));
        l1.add(new Card(Card.En._5, Card.Type._A));
        l1.add(new Card(Card.En._6, Card.Type._X));
        l1.add(new Card(Card.En._7, Card.Type._X));
        l1.add(new Card(Card.En._8, Card.Type._X));

        l2.add(new Card(Card.En._3, Card.Type._V));
        l2.add(new Card(Card.En._3, Card.Type._W));
        l2.add(new Card(Card.En._3, Card.Type._A));
        l2.add(new Card(Card.En._3, Card.Type._X));
        System.out.println(isBigger(l2, l1));
    }
}
