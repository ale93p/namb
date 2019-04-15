package fr.unice.yamb.utils.configuration.schema;

public class FlinkConfigSchema extends ConfigSchema {

    private int degubFrequency = 0;

    public int getDegubFrequency() {
        return degubFrequency;
    }

    public void setDegubFrequency(int degubFrequency) {
        this.degubFrequency = degubFrequency;
    }
}
