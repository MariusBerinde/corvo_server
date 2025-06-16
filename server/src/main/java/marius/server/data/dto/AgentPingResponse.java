package marius.server.data.dto;

public class AgentPingResponse {

    private String status;
    private String message;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMsg(String msg) {
        this.message = msg;
    }
}
