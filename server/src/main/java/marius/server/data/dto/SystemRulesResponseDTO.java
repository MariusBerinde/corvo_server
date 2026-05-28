package marius.server.data.dto;

import java.util.List;

/**
 * Represents the response returned by the agent when the {@code getSystemRules} route is called.
 * <p>
 * Contains the status, an optional message, and a list of services with their status information.
 * </p>
 *
 * @author Marius Berinde Dumitru
 */
public class SystemRulesResponseDTO {
    private String status;
    private List<RuleDTO> message;

    // Getters e setters
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public List<RuleDTO> getMessage() { return message; }
    public void setMessage(List<RuleDTO> message) { this.message = message; }

    @Override
    public String toString() {
        return "SystemRulesResponseDTO{status='" + status + "', message=" + message + "}";
    }
}
