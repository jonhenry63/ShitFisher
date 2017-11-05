/**
 * Created by Jon on 05/11/2017.
 */

package scripts;

import org.tribot.api.General;
import org.tribot.api2007.*;
import org.tribot.api2007.types.*;
import org.tribot.script.Script;
import org.tribot.api.types.generic.Filter;
import org.tribot.api2007.types.RSArea;
import org.tribot.api2007.types.RSObject;
import org.tribot.api2007.types.RSTile;
import org.tribot.script.ScriptManifest;
import scripts.Debug.Logging;


import java.util.Random;

@ScriptManifest(authors = "cinnes", name = "Fish and Cook", category = "Fishing", version = 1.0, description = "This simple script fishes shrimps, cooks them and drops.")
public class Fishing extends Script {
    private final int DEAD_TREE_ID = 1365;
    private final int FISHING_SPOT_ID = 1530;
    private final int FIRE_ID = 26185;

    private final int LOGS_ID = 1511;
    private final int UNCOOKED_SHRIMP = 317;
    private final int UNCOOKED_ANCHOVIES = 321;

    private final int SMALL_FISHING_NET = 303;
    private final int TINDERBOX = 590;

    private final int CHOPPING_ANIMATION_ID = 879;
    private final int FISHING_ANIMATION_ID = 621;

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

        Random rand = null;

        final RSArea treeRadius = getAreaBoundary(lumbridge_tree, radius);
        final RSArea fishing = getAreaBoundary(FISHING_SPOT, 1);

        // handle login
        Logging.debug("Logging in.");
        Login.login();

        checkInvSetup();
        while(running){

            Logging.debug("Sleeping.");
            General.sleep(300);

            if(isChopping() || isCooking() || isFishing() || Player.isMoving()){
                Logging.debug("Player active.");
                continue;
            }

            Logging.debug("Checking inventory for logs.");
            if(!checkInvLogs()){

                Logging.debug("Walking to Lumbridge Tree.");
                WebWalking.walkTo(lumbridge_tree);

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
                    }}
                    catch (Exception e){
                    Logging.debug(e.toString());
                }

            }
            else if(!Inventory.isFull() && checkInvLogs()){
                Logging.debug("Walking to fishing spot.");
                //WebWalking.walkTo(FISHING_SPOT);
                WebWalking.walkTo(FISHING_SPOT);
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
                    nearestFishing.click();
                }
            }
            else if(checkInvRawFish() && Inventory.isFull()){
                COOKING_SPOT = new RSTile(3224 + (rand.nextInt((6) + 1) - 3),3173 + (rand.nextInt((6) + 1) - 3));


                useItemOnItem("Logs", "Tinderbox");


                RSItem[] uncookedFish = Inventory.find(UNCOOKED_SHRIMP);
                uncookedFish[0].click();

                final RSArea fireArea = getAreaBoundary(COOKING_SPOT, 7);
                RSObject[] nearFires = Objects.findNearest(7, new Filter<RSObject>() {
                    @Override
                    public boolean accept(RSObject obj) {
                        return fireArea.contains(obj) && obj.getID() == FIRE_ID;
                    }
                });

                if(nearFires.length < 1){
                    continue;
                } else {
                    RSObject nearestFire = nearFires[0];
                    Logging.debug("Found tree, attempting to chop.");
                    nearestFire.click();
                    uncookedFish[0].click("Make all");
                }
            } else if(!checkInvRawFish() && Inventory.isFull()){
                droppingHandler();
            }


        }
    }

    private void droppingHandler() {
        Logging.debug("Checking if everything needs to be dropped.");
        if (Inventory.isFull() && Inventory.getCount(UNCOOKED_SHRIMP) == 0) {
            Logging.debug("Dropping inventory.");
            Inventory.dropAllExcept(SMALL_FISHING_NET,TINDERBOX);
        }
    }

    private void checkInvSetup() {
        if(Inventory.getCount(SMALL_FISHING_NET,TINDERBOX) == 0){
            Logging.debug("No small fishing net/tinderbox.");
        }
    }

    private boolean checkInvLogs(){
        if(Inventory.getCount(LOGS_ID) == 0){
            Logging.debug("No logs.");
            return false;
        }else return true;
    }

    private boolean checkInvRawFish(){
        if(Inventory.getCount(UNCOOKED_SHRIMP) == 0 && Inventory.getCount(UNCOOKED_ANCHOVIES) == 0){
            Logging.debug("No fish.");
            return false;
        }else return true;
    }

    private boolean isChopping() {
        return Player.getAnimation() == CHOPPING_ANIMATION_ID;
    }

    private boolean isFishing() {
        return Player.getAnimation() == FISHING_ANIMATION_ID;
    }

    private boolean isCooking() {
        return Player.getAnimation() == CHOPPING_ANIMATION_ID;
    }

    private RSArea getAreaBoundary(RSTile centerTile, int radius) {
        RSTile[] borderTiles = new RSTile[]{
                new RSTile(centerTile.getX() + radius, centerTile.getY() + radius),
                new RSTile(centerTile.getX() + radius, centerTile.getY() - radius),
                new RSTile(centerTile.getX() - radius, centerTile.getY() + radius),
                new RSTile(centerTile.getX() - radius, centerTile.getY() - radius)
        };
        return new RSArea(borderTiles);
    }

    private void useItemOnItem(String item1Name, String item2Name) {
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
}
