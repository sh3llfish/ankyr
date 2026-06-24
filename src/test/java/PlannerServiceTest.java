import org.junit.jupiter.api.Test;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PlannerServiceTest {

    @Test
    void throwsWhenLockedBlocksOverlap() {
        PlannerService plannerService = new PlannerService();

        Preferences preferences = new Preferences(
                LocalTime.of(7, 30),
                LocalTime.of(23, 30),
                0,
                new MealPreference(false, 0, LocalTime.of(8, 0)),
                new MealPreference(false, 0, LocalTime.of(12, 0)),
                new MealPreference(false, 0, LocalTime.of(19, 0)),
                new EntertainmentPreference(false, 0, "Entertainment", 0, LocalTime.of(21, 0)),
                new BufferPreference(0.20)
        );

        List<TimeBlock> fixedBlocks = List.of(
                new TimeBlock("Work", LocalTime.of(10, 0),
                LocalTime.of(12, 0), BlockType.FIXED, true),
                new TimeBlock("Meeting", LocalTime.of(11, 0),
                LocalTime.of(13, 0), BlockType.FIXED, true)
        );

        List<Goal> goals = List.of();

        assertThrows(
                IllegalArgumentException.class,
                () -> plannerService.generatePlan(preferences, fixedBlocks, goals)
        );
    }

    @Test
    void schedulesGoalInsideAvailableTime() {
        PlannerService plannerService = new PlannerService();

        Preferences preferences = new Preferences(
                LocalTime.of(9, 0),
                LocalTime.of(12, 0),
                0,
                new MealPreference(false, 0, LocalTime.of(8, 0)),
                new MealPreference(false, 0, LocalTime.of(12, 0)),
                new MealPreference(false, 0, LocalTime.of(19, 0)),
                new EntertainmentPreference(false, 0, "Entertainment",
                        0, LocalTime.of(21, 0)),
                new BufferPreference(0.20)
        );

        List<TimeBlock> fixedBlocks = List.of();

        List<Goal> goals = new ArrayList<>();
        goals.add(new Goal("Study", 60, 1, false));

        List<TimeBlock> plan =
                plannerService.generatePlan(preferences, fixedBlocks, goals);

        boolean hasStudyBlock = plan.stream()
                .anyMatch(block ->
                        block.title.equals("Study")
                                && block.start.equals(LocalTime.of(9,
                                0))
                                && block.end.equals(LocalTime.of(10,
                                0))
                                && block.type == BlockType.GOAL
                );

        assertTrue(hasStudyBlock);
    }
}
