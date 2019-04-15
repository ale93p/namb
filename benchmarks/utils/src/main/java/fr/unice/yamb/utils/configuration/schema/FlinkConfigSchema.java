package fr.unice.yamb.utils.configuration.schema;

public class FlinkConfigSchema extends ConfigSchema {

    private int debugFrequency = 0;

    public int getDebugFrequency() {
        return debugFrequency;
    }

    public void setDebugFrequency(int debugFrequency) {
        this.debugFrequency = debugFrequency;
    }
}
