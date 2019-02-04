package fr.unice.namb.utils;

public class StormConfigScheme {
    public enum StormDeployment{
        local, cluster
    }
    private StormDeployment deployment = StormDeployment.local;

    public StormDeployment getDeployment() {
        return deployment;
    }

    public void setDeployment(StormDeployment deployment) {
        this.deployment = deployment;
    }
}
