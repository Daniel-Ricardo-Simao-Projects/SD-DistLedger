package pt.tecnico.distledger.namingserver.domain;

public class ServerEntry {

    private String target;

    private String qualifier;

    public ServerEntry(String target, String qualifier) {
        this.target = target;
        this.qualifier = qualifier;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getQualifier() {
        return qualifier;
    }

    public void setQualifier(String qualifier) {
        this.qualifier = qualifier;
    }

    @Override
    public String toString() {
        return "ServerEntry{" +
                "target='" + target + '\'' +
                ", qualifier='" + qualifier + '\'' +
                '}';
    }
}