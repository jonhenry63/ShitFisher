/**
 * Created by Jon on 05/11/2017.
 */

package scripts;

import org.tribot.api.General;
import org.tribot.api.types.generic.Filter;
import org.tribot.api2007.*;
import org.tribot.api2007.types.*;
import org.tribot.script.Script;
import org.tribot.script.ScriptManifest;
import scripts.debug.Logging;
import scripts.utils.ItemUtil;
import scripts.utils.RSAreaUtil;


@ScriptManifest(authors = "cinnes", name = "Cooking Test", category = "Cooking", version = 1.0, description = "Running cooks raw shrimps and raw anchovies on lumbridge range.")
public class Cooking extends Script {
    private final int RANGE_ID = 114;

    private boolean debug = true;

    private final int radius = 10;

    private boolean running = true;

    private RSTile LUMBRIDGE_RANGE = new RSTile(3211,3215);

    @Override
    public void run() {
        Logging.debug("Starting...");

        Logging.debug("Loading ban compliance.");
        General.useAntiBanCompliance(true);

        final RSArea treeRadius = RSAreaUtil.getAreaBoundary(LUMBRIDGE_RANGE, radius);

        // handle login
        Logging.debug("Logging in.");
        Login.login();

        RSObject[] nearRanges = Objects.findNearest(radius, new Filter<RSObject>() {
            @Override
            public boolean accept(RSObject obj) {
                return treeRadius.contains(obj) && obj.getID() == RANGE_ID;
            }
        });

        while(true){

            General.sleep(300);

            if (Player.getAnimation() != -1  || Player.isMoving()) {
                //ABCUtil.performExamineObject();
                continue;
            }
            RSObject nearestRange = nearRanges[0];
            if(ItemUtil.carryingItem("Raw Shrimps")) {
                Cooking.cookAll("Raw shrimps", nearestRange);
            } else if (ItemUtil.carryingItem("Raw anchovies")) {
                Cooking.cookAll("Raw anchovies", nearestRange);
            }

        }
    }

    public static void cookAll(String item, RSObject fire){
        /**
         * have counter for which fish you are on
         * initially start cooking by using the item on the fire
         * clickCookInterface to cook all
         * while animation isn't -1 and there are items in the inventory to be cooked
         * if counter is 1 or 2, sleep for ~3 ticks
         * else if counter is 3 -> 28 sleep for ~4 ticks
         */

        Logging.debug("Cooking all.");
        int cookCount = 0;
        while(ItemUtil.carryingItem(item)) {

            if (cookCount == 0) {
                RSItem[] uncookedFish = Inventory.find(item);
                uncookedFish[0].click();
                General.sleep(500,1000);
                fire.click();
                General.sleep(500,1000);
                clickCookInterface();
                General.sleep(500,1000);
            }

            if (cookCount < 2) {
                General.sleep(900);
                cookCount++;
                if(Player.getAnimation() == -1) {
                    cookCount = 0;
                }
                General.sleep(900);
            } else if (cookCount >= 2) {
                General.sleep(1200);
                cookCount++;
                if(Player.getAnimation() == -1) {
                    cookCount = 0;
                }
                General.sleep(1200);
            }
        }
    }

    public static void clickCookInterface(){
        RSInterfaceChild makeAll;
        makeAll = Interfaces.get(270,14);
        makeAll.click();
        Logging.debug("Clicking make all.");
    }

}
