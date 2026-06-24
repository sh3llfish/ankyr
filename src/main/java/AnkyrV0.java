import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;


public class AnkyrV0 {

    public static void main(String[] args) {
        
        Preferences preferences = new Preferences(
                LocalTime.of(7, 30),   // wake time
                LocalTime.of(23, 30),  // sleep time
                0,                     // target sleep minutes
                new MealPreference(true, 30, LocalTime.of(8, 0)),
                new MealPreference(true, 45, LocalTime.of(12, 30)),
                new MealPreference(true, 45, LocalTime.of(19, 30)),
                new EntertainmentPreference(true, 60, "Entertainment", 1, LocalTime.of(21, 30)),
                new BufferPreference(0.20)
        );

        List<TimeBlock> fixedBlocks = new ArrayList<>();
        fixedBlocks.add(new TimeBlock(
                "Work",
                LocalTime.of(11, 0),
                LocalTime.of(19, 0),
                BlockType.FIXED,
                true
        ));

        List<Goal> goals = new ArrayList<>();
        goals.add(new Goal("LeetCode", 60, 3, false));
        goals.add(new Goal("Ankyr Project", 120, 3, true));
        goals.add(new Goal("Workout", 45, 2, false));
        goals.add(new Goal("Reading", 30, 1, false));

        PlannerService plannerService = new PlannerService();
        List<TimeBlock> plan = plannerService.generatePlan(preferences, fixedBlocks, goals);

        for (TimeBlock block : plan) {
            System.out.println(block);
        }
    }
}
