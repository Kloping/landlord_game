package com.hrs.kloping.entity;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class OCardSet {
    private long qq = -1;
    public List<Card> cards;

    private static final OCardSet set = new OCardSet();

    public OCardSet() {
        cards = new CopyOnWriteArrayList<>();
        for (int type = 1; type <= 4; type++) {
            for (int i = 1; i <= 13; i++) {
                cards.add(new Card(Card.En.getInstance(i), type == 1 ? Card.Type._A : type == 2 ? Card.Type._V : type == 3 ? Card.Type._X : Card.Type._W));
            }
        }
        cards.add(new Card(Card.En.getInstance(14), Card.Type._G));
        cards.add(new Card(Card.En.getInstance(15), Card.Type._G));
        sort(cards);
    }

    public static void sort(List<Card> list) {
        Collections.sort(list, new Comparator<Card>() {
            @Override
            public int compare(Card o1, Card o2) {
                return -(o1.getEn().v - o2.getEn().v);
            }
        });
    }

    public static OCardSet getSet() {
        return set;
    }

    public OCardSet(long qq) {
        this.qq = qq;
        cards = new CopyOnWriteArrayList<>();
    }

    public final synchronized static List<Card> getCards() {
        return set.cards;
    }

    public long getQq() {
        return qq;
    }
}
