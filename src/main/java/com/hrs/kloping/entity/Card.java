package com.hrs.kloping.entity;

import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.message.data.Image;

import java.io.File;

public class Card {
    public enum En {
        _3(1, "3"),
        _4(2, "4"),
        _5(3, "5"),
        _6(4, "6"),
        _7(5, "7"),
        _8(6, "8"),
        _9(7, "9"),
        _10(8, "10"),
        _J(9, "j"),
        _Q(10, "q"),
        _K(11, "k"),
        _A(12, "a"),
        _2(13, "2"),
        _X(14, "x"),
        _Y(15, "y"),
        ;
        int v = -1;
        String v2;

        En(int v, String v2) {
            this.v = v;
            this.v2 = v2;
        }

        public int getV() {
            return v;
        }

        public static En getInstance(int n) {
            for (En en : En.values()) {
                if (n == en.v)
                    return en;
            }
            return null;
        }
    }

    public enum Type {
        _A("红桃", 1),
        _V("黑桃", 2),
        _X("方片", 3),
        _W("梅花", 4),
        _G("-", 0),
        ;
        String v;
        int st;

        Type(String v, int st) {
            this.v = v;
            this.st = st;
        }
    }

    private En en;
    private Type type;

    public Card(En en, Type type) {
        this.en = en;
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Card card = (Card) o;
        return en.v == card.en.v && type.v.equals(card.type.v);
    }

    public En getEn() {
        return en;
    }

    public Type getType() {
        return type;
    }

    public static final String getFileNameFromCard(Card card){
        return String.format("./images/%s%s.jpg", card.getType().st == 0 ? "" : card.getType().st, card.en.v2);
    }

    @Override
    public String toString() {
        return "Card{" +
                "en=" + en +
                ", type=" + type +
                '}';
    }
}
