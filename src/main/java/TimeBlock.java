import java.time.LocalTime;

class TimeBlock{
    String title;
    LocalTime start;
    LocalTime end;
    BlockType type;
    boolean locked;

    TimeBlock(String title, LocalTime start, LocalTime end, BlockType type, boolean locked) {
        this.title = title;
        this.start = start;
        this.end = end;
        this.type = type;
        this.locked = locked;
    }

    int durationMinutes() {
        return (end.toSecondOfDay() - start.toSecondOfDay()) / 60;
    }

    @Override
    public String toString() {
        return start + " - " + end + " | " + title + " | " + type + (locked ? " | locked" : "");
    }
}
