class Goal {
    String title;
    int durationMinutes;
    int priority;
    boolean splittable;

    Goal(String title, int durationMinutes, int priority, boolean splittable) {
        this.title = title;
        this.durationMinutes = durationMinutes;
        this.priority = priority;
        this.splittable = splittable;
    }
}