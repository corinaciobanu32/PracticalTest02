package ro.pub.cs.systems.eim.practicaltest02.model;

public class TimedKey {

    private String value;
    private Integer time;

    public TimedKey() {
        this.value = null;
        this.time = null;
    }

    public TimedKey(String value, Integer time) {
        this.value = value;
        this.time = time;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Integer getTime() {
        return time;
    }

    public void setTime(Integer time) {
        this.time = time;
    }

    @Override
    public String toString() {
        return "TimedKey{" +
                "value='" + value + '\'' +
                ", time=" + time +
                '}';
    }
}
