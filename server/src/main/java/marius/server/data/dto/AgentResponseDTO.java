package marius.server.data.dto;

public class AgentResponseDTO {
    private String status;
    private String message;

    public AgentResponseDTO() {}

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public boolean isSuccess() {
        return "success".equalsIgnoreCase(status);
    }
}
