package marius.server.data.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

public class ServiceStatusResponseDTO {
    private String status;
    private String message;

    @JsonProperty("status_services")
    private List<Map<String, Boolean>> statusServices;

    public String getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public List<Map<String, Boolean>> getStatusServices() {
        return statusServices;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setStatusServices(List<Map<String, Boolean>> statusServices) {
        this.statusServices = statusServices;
    }
}
