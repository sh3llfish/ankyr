import java.time.LocalTime;

class Preferences {
    LocalTime wakeTime;
    LocalTime sleepTime;
    int targetSleepMinutes;

    MealPreference breakfast;
    MealPreference lunch;
    MealPreference dinner;
    EntertainmentPreference entertainment;
    BufferPreference buffer;

    Preferences(LocalTime wakeTime,
                LocalTime sleepTime,
                int targetSleepMinutes,
                MealPreference breakfast,
                MealPreference lunch,
                MealPreference dinner,
                EntertainmentPreference entertainment,
                BufferPreference buffer) {
        this.wakeTime = wakeTime;
        this.sleepTime = sleepTime;
        this.targetSleepMinutes = targetSleepMinutes;
        this.breakfast = breakfast;
        this.lunch = lunch;
        this.dinner = dinner;
        this.entertainment = entertainment;
        this.buffer = buffer;
    }
}
