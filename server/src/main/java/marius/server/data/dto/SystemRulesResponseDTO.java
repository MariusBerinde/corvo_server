package marius.server.data.dto;

import java.util.List;

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
