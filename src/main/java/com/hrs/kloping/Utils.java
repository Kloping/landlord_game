package com.hrs.kloping;

import com.hrs.kloping.entity.Card;
import com.hrs.kloping.entity.OCardSet;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.message.data.Image;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import static com.hrs.kloping.entity.Card.getFileNameFromCard;

public class Utils {
    public final static synchronized Image getImageFromCard(Card card, Contact contact) {
        return Contact.uploadImage(contact, new File(getFileNameFromCard(card)));
    }

    public final static synchronized Image getImageFromFile(File file, Contact contact) {
        return Contact.uploadImage(contact, file);
    }

    public final static synchronized Image getImageFromFilePath(String path, Contact contact) {
        return Contact.uploadImage(contact, new File(path));
    }

    public static List<java.awt.Image> cards2Images(List<Card> cards) {
        List<java.awt.Image> list = new CopyOnWriteArrayList<>();
        for (Card card : cards) {
            list.add(Drawer.loadImage(Card.getFileNameFromCard(card)));
        }
        return list;
    }

    public static int[] cards2values(List<Card> list) {
        OCardSet.sort(list);
        int[] ints = new int[list.size()];
        for (int i = 0; i < list.size(); i++) {
            ints[i] = list.get(i).getEn().getV();
        }
        return ints;
    }

    public static int getMaxSameC(int... values) {
        int[] ints = new int[values.length * 2];
        for (int i = 0; i < values.length; i++) {
            for (int j = 0; j < values.length; j++) {
                if (ints[j] == values[i]) {
                    ints[j + values.length]++;
                    continue;
                }
            }
            ints[i] = values[i];
            ints[i + values.length] = 1;
        }
        int max = 0;
        for (int i = values.length; i < values.length * 2; i++) {
            if (ints[i] >= max) max = ints[i];
        }
        return max;
    }

    public static int getMaxSameN(int... values) {
        int[] ints = new int[values.length * 2];
        for (int i = 0; i < values.length; i++) {
            for (int j = 0; j < values.length; j++) {
                if (ints[j] == values[i]) {
                    ints[j + values.length]++;
                    continue;
                }
            }
            ints[i] = values[i];
            ints[i + values.length] = 1;
        }
        int max = 0;
        int n = -1;
        for (int i = values.length; i < values.length * 2; i++) {
            if (ints[i] >= max) {
                max = ints[i];
                n = i - values.length;
            }
        }
        return values[n];
    }

    public static final <K, V> Map.Entry<K, V> getEntry(K k, V v) {
        Map.Entry<K, V> entry = new Map.Entry<K, V>() {
            private K _k = k;
            private V _v = v;

            @Override
            public K getKey() {
                return _k;
            }

            @Override
            public V getValue() {
                return _v;
            }

            @Override
            public V setValue(V value) {
                V v1 = _v;
                _v = value;
                return v1;
            }
        };
        return entry;
    }
}
