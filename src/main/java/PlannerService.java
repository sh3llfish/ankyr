import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

class PlannerService {

    public List<TimeBlock> generatePlan(Preferences preferences,
                                        List<TimeBlock> fixedBlocks,
                                        List<Goal> goals) {
        validateNoLockedOverlap(fixedBlocks);

        List<TimeBlock> plan = new ArrayList<>();
        plan.addAll(fixedBlocks);

        addSystemBlocks(plan, preferences);
        sortBlocksByStartTime(plan);

        List<TimeBlock> freeSlots = findFreeSlot(
                preferences.wakeTime,
                preferences.sleepTime,
                plan
        );

        sortGoals(goals);

        if (isOverloaded(goals, freeSlots)) {
            System.out.println("Warning: goals need more time than the available free slots.");
        }

        for (Goal goal : goals) {
            boolean scheduled = placeGoal(goal, freeSlots, plan);
            if (!scheduled) {
                System.out.println("Unscheduled goal: " + goal.title);
            }
        }

        sortBlocksByStartTime(plan);
        return plan;
    }

    private void addSystemBlocks(List<TimeBlock> plan,
                                 Preferences preferences) {
        /*
        Given user preferences, add routine blocks before finding free slots.
        System blocks are treated as occupied time, same as fixed blocks.
        If a system block conflicts with existing occupied time, skip it for now.
        */
        addMealBlock(plan, "Breakfast", preferences.breakfast);
        addMealBlock(plan, "Lunch", preferences.lunch);
        addMealBlock(plan, "Dinner", preferences.dinner);

        if (preferences.entertainment.enabled) {
            LocalTime start = preferences.entertainment.preferredStart;
            LocalTime end = start.plusMinutes(preferences.entertainment.durationMinutes);
            addIfNoConflict(plan, new TimeBlock(
                    preferences.entertainment.name,
                    start,
                    end,
                    BlockType.ENTERTAINMENT,
                    false
            ));
        }
    }

    private void addMealBlock(List<TimeBlock> plan,
                              String title,
                              MealPreference mealPreference) {
        /*
        Given one meal preference, convert it into a TimeBlock.
        Disabled meals are ignored.
        Enabled meals use preferredStart and durationMinutes to calculate end time.
        */
        if (!mealPreference.enabled) {
            return;
        }

        LocalTime start = mealPreference.preferredStart;
        LocalTime end = start.plusMinutes(mealPreference.durationMinutes);
        addIfNoConflict(plan, new TimeBlock(title, start, end, BlockType.MEAL, false));
    }

    private void addIfNoConflict(List<TimeBlock> plan,
                                 TimeBlock candidate) {
        /*
        Given a candidate block, add it only if it does not overlap existing blocks.
        This protects fixed blocks and already-added system blocks.
        For the minimum version, conflicting system blocks are skipped.
        */
        for (TimeBlock block : plan) {
            if (overlaps(block, candidate)) {
                return ;
            }
        }
        plan.add(candidate);
    }

    private List<TimeBlock> findFreeSlot(LocalTime wakeTime,
                                         LocalTime sleepTime,
                                         List<TimeBlock> occupiedBlocks) {
        /*
        Given the planning window and occupied blocks, find gaps between them.
        currentTime means the earliest time that is still available.
        If currentTime is before the next block starts, that gap is a free slot.
        */
        List<TimeBlock> freeSlots = new ArrayList<>();
        sortBlocksByStartTime(occupiedBlocks);

        LocalTime currentTime = wakeTime;

        for (TimeBlock block : occupiedBlocks) {
            if (block.end.isBefore(wakeTime) || block.start.isAfter(sleepTime)) {
                continue;
            }

            LocalTime blockStart = block.start.isBefore(wakeTime) ? wakeTime : block.start;
            LocalTime blockEnd = block.end.isAfter(sleepTime) ? sleepTime : block.end;

            if (currentTime.isBefore(blockStart)) {
                freeSlots.add(new TimeBlock(
                        "Free Slot",
                        currentTime,
                        blockStart,
                        BlockType.BUFFER,
                        false
                ));
            }

            if (currentTime.isBefore(blockEnd)) {
                currentTime = blockEnd;
            }
        }

        if (currentTime.isBefore(sleepTime)) {
            freeSlots.add(new TimeBlock(
                    "Free Slot",
                    currentTime,
                    sleepTime,
                    BlockType.BUFFER,
                    false
            ));
        }

        return freeSlots;
    }

    private boolean placeGoal(Goal goal,
                              List<TimeBlock> freeSlots,
                              List<TimeBlock> plan) {
        /*
        Given one goal, place it into the available free slots.
        Non-splittable goals must fit in one slot.
        Splittable goals can consume several slots until their full duration is scheduled.
        */
        if (goal.splittable) {
            return placeSplittableGoal(goal, freeSlots, plan);
        }

        for (TimeBlock slot : freeSlots) {
            if (slot.durationMinutes() >= goal.durationMinutes) {
                LocalTime start = slot.start;
                LocalTime end = start.plusMinutes(goal.durationMinutes);

                plan.add(new TimeBlock(goal.title, start, end, BlockType.GOAL, false));
                slot.start = end;
                return true;
            }
        }

        return false;
    }

    private boolean placeSplittableGoal(Goal goal,
                                        List<TimeBlock> freeSlots,
                                        List<TimeBlock> plan) {
        /*
        First make sure the combined remaining free time can fit the goal.
        This avoids partially scheduling a goal that cannot be completed.
        */
        if (calculateAvailableMinutes(freeSlots) < goal.durationMinutes) {
            return false;
        }

        int remainingMinutes = goal.durationMinutes;
        int partNumber = 1;

        for (TimeBlock slot : freeSlots) {
            if (remainingMinutes == 0) {
                return true;
            }

            int slotMinutes = slot.durationMinutes();
            if (slotMinutes <= 0) {
                continue;
            }

            int minutesToSchedule = Math.min(remainingMinutes, slotMinutes);
            LocalTime start = slot.start;
            LocalTime end = start.plusMinutes(minutesToSchedule);
            String title = goal.title;

            if (minutesToSchedule < goal.durationMinutes) {
                title = goal.title + " (part " + partNumber + ")";
            }

            plan.add(new TimeBlock(title, start, end, BlockType.GOAL, false));
            slot.start = end;
            remainingMinutes -= minutesToSchedule;
            partNumber++;
        }

        return remainingMinutes == 0;
    }

    private boolean overlaps(TimeBlock a, TimeBlock b) {
        /*
        Given two TimeBlock, determine if they have overlapping area.
        Overlap Condition:
            a start before b end
            b start before a end
            if these 2 condition hold, a and b overlap.
        */
        return a.start.isBefore(b.end) && b.start.isBefore(a.end);
    }

    private void validateNoLockedOverlap(List<TimeBlock> blocks) {
        /*
        Given locked blocks, make sure the user-created fixed schedule is possible.
        Compare each pair once by starting j at i + 1.
        If two locked blocks overlap, the minimum manual schedule is invalid.
        */
        for (int i = 0; i < blocks.size(); i++) {
            for (int j = i + 1; j < blocks.size(); j++) {
                TimeBlock a = blocks.get(i);
                TimeBlock b = blocks.get(j);

                if (a.locked && b.locked && overlaps(a, b)) {
                    throw new IllegalArgumentException(
                            "Locked blocks overlap: " + a.title + " and " + b.title
                    );
                }
            }
        }
    }

    private int calculateTotalGoalMinutes(List<Goal> goals) {
        /*
        Given all goals, calculate how many minutes they require in total.
        This is used to detect whether the day is overloaded.
        */
        int total = 0;
        for (Goal goal : goals) {
            total += goal.durationMinutes;
        }
        return total;
    }

    private int calculateAvailableMinutes(List<TimeBlock> freeSlots) {
        /*
        Given all free slots, calculate how many minutes can still be scheduled.
        This is the available time after fixed and system blocks are protected.
        */
        int total = 0;
        for (TimeBlock slot : freeSlots) {
            total += slot.durationMinutes();
        }
        return total;
    }

    private boolean isOverloaded(List<Goal> goals,
                                 List<TimeBlock> freeSlots) {
        /*
        Given required goal time and available free time, detect overload.
        Minimum version:
            overloaded = total goal minutes > total free slot minutes
        */
        return calculateTotalGoalMinutes(goals) > calculateAvailableMinutes(freeSlots);
    }

    private void sortGoals(List<Goal> goals) {
        goals.sort(
                Comparator.comparingInt((Goal goal) -> goal.priority).reversed()
                        .thenComparingInt(goal -> goal.durationMinutes)
        );
    }

    private void sortBlocksByStartTime (List<TimeBlock> blocks) {
        blocks.sort(
                Comparator.comparing((TimeBlock b) -> b.start)
                        .thenComparing(b -> b.end)
        );
    }
}
