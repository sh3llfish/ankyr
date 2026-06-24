import java.time.LocalTime;

class MealPreference {
    boolean enabled;
    int durationMinutes;
    LocalTime preferredStart;

    MealPreference(boolean enabled, int durationMinutes, LocalTime preferredStart) {
        this.enabled = enabled;
        this.durationMinutes = durationMinutes;
        this.preferredStart = preferredStart;
    }
}