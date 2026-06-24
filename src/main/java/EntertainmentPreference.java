import java.time.LocalTime;

class EntertainmentPreference {
    boolean enabled;
    int durationMinutes;
    String name;
    int count;
    LocalTime preferredStart;

    EntertainmentPreference(boolean enabled, int durationMinutes, String name, int count, LocalTime preferredStart) {
        this.enabled = enabled;
        this.durationMinutes = durationMinutes;
        this.name = name;
        this.count = count;
        this.preferredStart = preferredStart;
    }
}
