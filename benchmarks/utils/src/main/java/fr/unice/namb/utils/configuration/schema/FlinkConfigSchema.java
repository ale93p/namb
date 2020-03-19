package fr.unice.namb.utils.configuration.schema;

public class FlinkConfigSchema extends ConfigSchema {

    private float
            debugFrequency = 0;

    public float getDebugFrequency() {
        return debugFrequency;
    }

    public void setDebugFrequency(float debugFrequency) {
        this.debugFrequency = debugFrequency;
    }
}
