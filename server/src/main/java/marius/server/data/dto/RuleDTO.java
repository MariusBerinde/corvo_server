package marius.server.data.dto;

/**
 * Represents a single security rule returned by the agent
 * when the {@code getSystemRules} route is called.
 * <p>
 * Contains the rule's name, description, status, and the IPv4 address of the agent.
 * </p>
 *
 * @author Marius Berinde Dumitru
 */
public class RuleDTO {
    /**
     * The name of the rule
     */
    private String name;
    /**
     * The description of the rule
     */
    private String description;
    /**
     * The status of the rule
     */
    private boolean status;
    /**
     * the Ip where the rule is used
     */
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
