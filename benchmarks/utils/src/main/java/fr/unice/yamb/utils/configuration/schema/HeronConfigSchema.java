package fr.unice.yamb.utils.configuration.schema;

public class HeronConfigSchema extends ConfigSchema {
    public enum StormDeployment{
        local, cluster
    }

    private int maxSpoutPending = 5000;
    private StormDeployment deployment = StormDeployment.local;
    private boolean debug = false;

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

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }
}
