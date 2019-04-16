package fr.unice.yamb.utils.configuration.schema;

public class StormConfigSchema extends ConfigSchema {
    public enum StormDeployment{
        local, cluster
    }

    private int workers = 1;
    private int maxSpoutPending = 5000;
    private StormDeployment deployment = StormDeployment.local;
    private float debugFrequency = 0;

    public int getWorkers() {
        return workers;
    }

    public void setWorkers(int workers) {
        this.workers = workers;
    }

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
