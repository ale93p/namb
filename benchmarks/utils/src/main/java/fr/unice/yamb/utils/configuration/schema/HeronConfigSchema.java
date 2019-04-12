package fr.unice.yamb.utils.configuration.schema;

public class HeronConfigSchema extends ConfigSchema {
    public enum StormDeployment{
        local, cluster
    }

    private int maxSpoutPending = 5000;
    private StormDeployment deployment = StormDeployment.local;
    private int degubFrequency = 0;

    public StormDeployment getDeployment() {
        return deployment;
    }

    public void setDeployment(StormDeployment deployment) {
        this.deployment = deployment;
    }

    public int getMaxSpoutPending() {
        return maxSpoutPending;
    }

    public void setMaxSpoutPending(int maxSpoutPending) {
        this.maxSpoutPending = maxSpoutPending;
    }

    public int getDegubFrequency() {
        return degubFrequency;
    }

    public void setDegubFrequency(int degubFrequency) {
        this.degubFrequency = degubFrequency;
    }
}
