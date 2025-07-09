package marius.server.data.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

/*
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
 */
public class ServiceStatusResponseDTO {
    private String status;
    private String message;
    @JsonProperty("status_services")
    private List<ServiceInfo> statusServices;

    public String getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public List<ServiceInfo> getStatusServices() {
        return statusServices;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setStatusServices(List<ServiceInfo> statusServices) {
        this.statusServices = statusServices;
    }

    // Classe interna per rappresentare ogni servizio
    public static class ServiceInfo {
        private String name;
        private boolean status;
        private boolean automaticStart;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public boolean isStatus() {
            return status;
        }

        public void setStatus(boolean status) {
            this.status = status;
        }

        public boolean isAutomaticStart() {
            return automaticStart;
        }

        public void setAutomaticStart(boolean automaticStart) {
            this.automaticStart = automaticStart;
        }
    }
}