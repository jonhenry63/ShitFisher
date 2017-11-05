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
import scripts.debug.Logging;
import scripts.utils.ItemUtil;
import scripts.utils.RSAreaUtil;

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
        final RSArea fishing = RSAreaUtil.getAreaBoundary(FISHING_SPOT, 1);

        // handle login
        Logging.debug("Logging in.");
        Login.login();

        checkInvSetup();
        while(running){

            Logging.debug("Sleeping.");
            General.sleep(300);

            if (Player.getAnimation() != -1  || Player.isMoving()) continue;

            Logging.debug("Checking inventory for logs.");
            if(!checkInvLogs()){

                Logging.debug("Walking to Lumbridge Tree.");
                Walking.walkTo(lumbridge_tree);
                General.sleep(300,1000);
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
                General.sleep(300,1000);
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
                    General.sleep(300,1000);
                }
            }
            else if(checkInvRawFish() && Inventory.isFull()){
                COOKING_SPOT = new RSTile(3224 + General.random(-3,3),3173 + General.random(-3, 3));


                ItemUtil.useItemOnItem("Logs", "Tinderbox");


                RSItem[] uncookedFish = Inventory.find(UNCOOKED_SHRIMP);
                uncookedFish[0].click();
                General.sleep(300,1000);

                final RSArea fireArea = RSAreaUtil.getAreaBoundary(COOKING_SPOT, 7);
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
                    General.sleep(300,1000);
                    uncookedFish[0].click("Make all");
                    General.sleep(300,1000);
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

}
