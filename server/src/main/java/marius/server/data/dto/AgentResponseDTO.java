package marius.server.data.dto;

/**
 * Represents the response returned by the agent when the following routes are called:
 * <ul>
 *     <li>{@code setActiveUser}</li>
 *     <li>{@code addLynisRules}</li>
 *     <li>{@code startLynisScan}</li>
 * </ul>
 * <p>
 * Contains the status and an optional message providing additional information.
 * </p>
 *
 * @author Marius Berinde Dumitru
 */
public class AgentResponseDTO {
    /**
     * the status of the response
     */
    private String status;

    /**
     * the message in the response
     */
    private String message;

    /**
     * Class constructor
     */
    public AgentResponseDTO() {}

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public boolean isSuccess() {
        return "success".equalsIgnoreCase(status);
    }
}
