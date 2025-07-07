package marius.server.data.dto;

public class RuleDTO {
    private String name;
    private String description;
    private boolean status;
    private String ip;

    // Getters e setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public boolean isStatus() { return status; }
    public void setStatus(boolean status) { this.status = status; }

    public String getIp() { return ip; }
    public void setIp(String ip) { this.ip = ip; }

    @Override
    public String toString() {
        return "RuleDTO{name='" + name + "', description='" + description +
                "', status=" + status + ", ip='" + ip + "'}";
    }
}
