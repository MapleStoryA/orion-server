package client;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import lombok.Getter;
import networking.data.output.OutPacket;

public class WishList {

    private final Set<Integer> items;

    @Getter
    private boolean changed;

    public WishList() {
        this.items = new LinkedHashSet<>(10);
    }

    public void addItem(int item) {
        items.add(item);
        changed = true;
    }

    public void setItem(int item) {
        items.add(item);
    }

    public void clear() {
        changed = true;
        items.clear();
    }

    public void encodeToCharInfo(OutPacket packet) {
        packet.write(items.size());
        if (items.size() > 0) {
            for (var item : items) {
                packet.writeInt(item);
            }
        }
    }

    public void encodeToCashShop(OutPacket packet) {
        var list = toArray();
        for (int i = 0; i < 10; i++) {
            packet.writeInt(list[i] != -1 ? list[i] : 0);
        }
    }

    public Set<Integer> getItems() {
        return items;
    }

    public void update(int[] wishlist) {
        Arrays.stream(wishlist).forEach(this::addItem);
    }

    private int[] toArray() {
        var list = new int[10];
        var src = items.stream().mapToInt(Integer::intValue).toArray();
        System.arraycopy(src, 0, list, 0, src.length);
        return list;
    }
}
