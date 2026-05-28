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


/**
 * Represents the response returned by the agent when the {@code getServiceStatus} route is called.
 * <p>
 * Contains the status, an optional message, and a list of services with their status information.
 * </p>
 *
 * @author Marius Berinde Dumitru
 */
public class ServiceStatusResponseDTO {
    /**
     * the status of the response
     */
    private String status;

    /**
     * the message in the response
     */
    private String message;

    /**
     * The list of  status services that contains
     */
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


    /**
     * Represents the status information of a single service.
     */
    public static class ServiceInfo {
        /**
         * The name of the service
         */
        private String name;

        /**
         * {@code true} if the service is running; {@code false} otherwise.
         */
        private boolean status;

        /**
         * {@code true} if the service starts automatically; {@code false} otherwise.
         */
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