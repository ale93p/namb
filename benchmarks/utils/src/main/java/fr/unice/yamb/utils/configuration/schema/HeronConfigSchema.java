package fr.unice.yamb.utils.configuration.schema;

public class HeronConfigSchema extends ConfigSchema {
    public enum StormDeployment{
        local, cluster
    }

    private int maxSpoutPending = 5000;
    private StormDeployment deployment = StormDeployment.local;

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
}
