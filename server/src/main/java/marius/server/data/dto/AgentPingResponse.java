package marius.server.data.dto;

/**
 * Represents the response returned by the agent when the ping route is called.
 * <p>
 * Contains the status and an optional message providing additional information.
 * </p>
 *
 * @author Marius Berinde Dumitru
 */
public class AgentPingResponse {

    /**
     * the status of the response
     */
    private String status;
    /**
     * the message in the response
     */
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
