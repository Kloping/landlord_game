package com.hrs.kloping;

import com.hrs.kloping.entity.Card;
import com.hrs.kloping.entity.OCardSet;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.message.data.Image;

import java.io.*;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;

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

    public static List<java.awt.Image> cards2Images(List<Card> cards_) {
        List<java.awt.Image> list = new CopyOnWriteArrayList<>();
        Set<Card> cards = new CopyOnWriteArraySet<>();
        cards.addAll(cards_);
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

    public static int isFly(int... values) {
        Map<Integer, Integer> map = new LinkedHashMap<>();
        for (int v : values) {
            if (map.containsKey(v))
                map.put(v, map.get(v) + 1);
            else map.put(v, 1);
        }
        int sameThree = 0;
        int maxK = 0;
        for (int k : map.keySet()) {
            int v = map.get(k);
            if (v == 3) {
                sameThree++;
                maxK = maxK > k ? maxK : k;
            }
        }
        if (sameThree == 2 && values.length == 8) return maxK;
        if (sameThree == 3 && values.length == 9) return maxK;
        return -1;
    }

    public static String getStrTypeFly(int... values) {
        Map<Integer, Integer> map = new LinkedHashMap<>();
        for (int v : values) {
            if (map.containsKey(v))
                map.put(v, map.get(v) + 1);
            else map.put(v, 1);
        }
        int sameThree = 0;
        for (int v : map.values()) {
            if (v == 3) sameThree++;
        }
        if (sameThree == 2 && values.length == 8) return flyT1;
        if (sameThree == 3 && values.length == 9) return flyT2;
        return null;
    }

    private static final String flyT1 = "AAABBBCD";
    private static final String flyT2 = "AAABBBCCC";

    public static boolean isBiggerFly(int[] values1, int[] values2) {
        String t1 = getStrTypeFly(values1);
        String t2 = getStrTypeFly(values2);
        if (t1 == null || t2 == null) return false;
        if (t1.equals(flyT1) && t2.equals(flyT2)) return false;
        return isFly(values1) > isFly(values2);
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

    public static synchronized final String getStringFromFile(String filepath) {
        try {
            File file = new File(filepath);
            if (!file.exists()) {
                file.getParentFile().mkdirs();
                file.createNewFile();
                return null;
            }
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "utf-8"));
            String line = null;
            StringBuilder sb = new StringBuilder();
            while ((line = br.readLine()) != null) {
                if (line.contains("#") || line.trim().isEmpty()) continue;
                sb.append(line);
            }
            br.close();
            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String thisPath = System.getProperty("user.dir");

    static {
        thisPath = thisPath == null ? "." : thisPath;
    }

    public static synchronized <T> T init(String tips, String fileName, T defaultt, Class<T> clas) {
        String str = getStringFromFile(thisPath + "/conf/Landlord/" + fileName);
        if (str == null || str.trim().isEmpty()) {
            putStringInFile(thisPath + "/conf/Landlord/" + fileName, tips);
        } else {
            if (clas == Long.class)
                return (T) Long.valueOf(str);
        }
        return defaultt;
    }

    public static synchronized final boolean putStringInFile(String filepath, String... lines) {
        try {
            File file = new File(filepath);
            if (!file.exists()) {
                file.getParentFile().mkdirs();
                file.createNewFile();
            }
            PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(file), "utf-8"), true);
            for (String line : lines)
                pw.println(line);
            pw.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

}
