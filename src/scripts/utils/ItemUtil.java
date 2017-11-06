package scripts.utils;

import org.tribot.api.General;
import org.tribot.api2007.Game;
import org.tribot.api2007.Inventory;
import org.tribot.api2007.types.RSItem;

public class ItemUtil {
    public static void useItemOnItem(String item1Name, String item2Name) {
        RSItem[] item1Arr = Inventory.find(item1Name);
        RSItem[] item2Arr = Inventory.find(item2Name);

        if (item1Arr.length < 1 || item2Arr.length < 1) throw new RuntimeException("Items not found");

        RSItem item1 = item1Arr[0];
        RSItem item2 = item2Arr[0];

        if (Game.getItemSelectionState() == 1 && Game.getSelectedItemName().equals(item1Name)) {
            item2.click();
            return;
        }

        while (Game.getItemSelectionState() == 0) {
            item1.click();
            General.sleep(500,600);
        }

        item2.click();
        General.sleep(500,600);
    }

    public static boolean carryingItem(String name) {
        return Inventory.getCount(name) != 0;
    }

}
