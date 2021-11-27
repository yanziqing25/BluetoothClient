package cn.yzking.bluetoothclient;

public class Student {
    private final String id;
    private final float temperature;

    public Student(String id, float temperature){
        this.id = id;
        this.temperature = temperature;
    }

    public String getId() {
        return this.id;
    }

    public float getTemperature() {
        return this.temperature;
    }
}
