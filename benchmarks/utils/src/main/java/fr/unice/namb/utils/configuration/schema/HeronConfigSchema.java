package fr.unice.namb.utils.configuration.schema;

public class HeronConfigSchema extends ConfigSchema {
    public enum StormDeployment{
        local, cluster
    }

    private int maxSpoutPending = 5000;
    private StormDeployment deployment = StormDeployment.local;
    private float debugFrequency = 0;

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

    public float getDebugFrequency() {
        return debugFrequency;
    }

    public void setDebugFrequency(float debugFrequency) {
        this.debugFrequency = debugFrequency;
    }
}
