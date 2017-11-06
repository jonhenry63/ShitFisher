/**
 * Created by Jon on 05/11/2017.
 */

package scripts;

import org.tribot.api.DynamicClicking;
import org.tribot.api.General;
import org.tribot.api.util.abc.ABCUtil;
import org.tribot.api2007.*;
import org.tribot.api2007.types.*;
import org.tribot.script.Script;
import org.tribot.api.types.generic.Filter;
import org.tribot.api2007.types.RSArea;
import org.tribot.api2007.types.RSObject;
import org.tribot.api2007.types.RSTile;
import org.tribot.script.ScriptManifest;
import scripts.debug.Logging;
import scripts.utils.ItemUtil;
import scripts.utils.RSAreaUtil;

@ScriptManifest(authors = "cinnes", name = "Fish and Cook", category = "Fishing", version = 1.0, description = "This simple script fishes shrimps, cooks them and drops. Should start a fire and continue to cook on it.")
public class Fishing extends Script {
    private final int DEAD_TREE_ID = 1365;
    private final int FISHING_SPOT_ID = 1530;
    private final int FIRE_ID = 26185;

    private boolean debug = true;

    private final int radius = 10;

    private boolean running = true;

    private RSTile lumbridge_tree = new RSTile(3224,3173);
    private RSTile FISHING_SPOT = new RSTile(3242,3151);
    private RSTile COOKING_SPOT;
    @Override
    public void run() {
        Logging.debug("Starting...");

        Logging.debug("Loading ban compliance.");
        General.useAntiBanCompliance(true);

        final RSArea treeRadius = RSAreaUtil.getAreaBoundary(lumbridge_tree, radius);
        final RSArea fishing = RSAreaUtil.getAreaBoundary(FISHING_SPOT, 10);

        // handle login
        Logging.debug("Logging in.");
        Login.login();

        if (!ItemUtil.carryingItem("Small fishing net") || !ItemUtil.carryingItem("Tinderbox")) {
            throw new RuntimeException("Missing required items.");
        }

        COOKING_SPOT = new RSTile(3241 + General.random(-3,3),3151 + General.random(-3, 3));

        while(running){

            final RSArea fireArea = RSAreaUtil.getAreaBoundary(COOKING_SPOT, 10);
            RSObject[] nearFires = Objects.findNearest(10, new Filter<RSObject>() {
                @Override
                public boolean accept(RSObject obj) {
                    return fireArea.contains(obj) && obj.getID() == FIRE_ID;
                }
            });
            Logging.debug(COOKING_SPOT.toString());
            Logging.debug("There are " + nearFires.length + " fires close by.");
            Logging.debug("Is inventory full? " + Inventory.isFull());
            Logging.debug("Carrying uncooked fish? " + (ItemUtil.carryingItem("Raw Shrimp") || ItemUtil.carryingItem("Raw Anchovie")));
            Logging.debug("Is there a fire close by? " + (nearFires.length > 0));
            Logging.debug("Complete Test: " + (Inventory.isFull() && (ItemUtil.carryingItem("Raw Shrimp") || ItemUtil.carryingItem("Raw Anchovie")) && nearFires.length < 1) + "");
            Logging.debug("Sleeping.");
            General.sleep(300);

            if (Player.getAnimation() != -1  || Player.isMoving()) {
                //ABCUtil.performExamineObject();
                continue;
            }

            Logging.debug("Checking inventory for logs.");
            if(!ItemUtil.carryingItem("Logs") && !(ItemUtil.carryingItem("Raw shrimps") || ItemUtil.carryingItem("Raw anchovies")) && nearFires.length < 1) {

                Logging.debug("Walking to Lumbridge Tree.");

                Walking.blindWalkTo(lumbridge_tree);

                try{
                    RSObject[] nearTrees = Objects.findNearest(radius, new Filter<RSObject>() {
                        @Override
                        public boolean accept(RSObject obj) {
                            return treeRadius.contains(obj) && obj.getID() == DEAD_TREE_ID;
                        }
                    });
                    if (nearTrees.length < 1){
                        Logging.debug("No trees found, waiting til respawn.");
                        General.sleep(500);
                    } else {
                        RSObject nearestTree = nearTrees[0];
                        Logging.debug("Found tree, attempting to chop.");
                        nearestTree.click();
                        General.sleep(1500);
                    }}
                    catch (Exception e){
                    Logging.debug(e.toString());
                }

            } else if(!Inventory.isFull() && ItemUtil.carryingItem("Logs")){
                Logging.debug("Walking to fishing spot.");

                if (!fishing.contains(Player.getPosition())) {
                    Walking.blindWalkTo(FISHING_SPOT);
                    General.sleep(1000);
                }

                RSNPC[] nearFishingSpots = NPCs.findNearest(new Filter<RSNPC>(){
                    @Override
                    public boolean accept(RSNPC npc) {
                        return fishing.contains(npc) && npc.getID() == FISHING_SPOT_ID;
                    }
                });

                if (nearFishingSpots.length < 1){
                    Logging.debug("No fishing spots found, waiting til respawn.");
                    General.sleep(300);
                } else {
                    RSNPC nearestFishing = nearFishingSpots[0];

                    nearestFishing.getModel().click("Net");
                }
            } else if((ItemUtil.carryingItem("Raw shrimps") || ItemUtil.carryingItem("Raw anchovies")) && nearFires.length > 0){
                RSItem[] uncookedFish = Inventory.find("Raw shrimps");
                uncookedFish[0].click();
                General.sleep(500,1000);

                if(nearFires.length < 1){
                    continue;
                } else {
                    RSObject nearestFire = nearFires[0];
                    Logging.debug("Found tree, attempting to chop.");
                    nearestFire.click();
                    General.sleep(500, 1000);
                    uncookedFish[0].click("Make all");
                    General.sleep(500, 1000);
                }
            } else if(Inventory.isFull() && (ItemUtil.carryingItem("Raw shrimps") || ItemUtil.carryingItem("Raw anchovies")) && nearFires.length < 1) {
                COOKING_SPOT = new RSTile(3241 + General.random(-3,3),3151 + General.random(-3, 3));
                Walking.blindWalkTo(COOKING_SPOT);
                ItemUtil.useItemOnItem("Logs", "Tinderbox");
            }
        }
    }

}
