package marius.server.data;

import jakarta.persistence.*;
import lombok.Data;

/**
 * Represents a security rule monitored and controlled by the Python agent.
 * <strong>Notes:</strong>
 * <ul>
 *     <li>The {@code ip} field is a foreign key referencing the {@code ip} field of the {@code server} table.</li>
 *     <li>The {@code status} field is {@code true} when the rule is active on the agent; {@code false} otherwise.</li>
 *     <li>The {@code service} field is an optional foreign key referencing a service ID.</li>
 * </ul>
 *
 * @author Marius Berinde Dumitru
 */
@Data
@Entity
@Table(name = "rules")
public class Rules {
    /**
     * Unique identifier for the server (auto-generated).
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column()
    private int id;

    /**
     * The name of the rule
     */
    @Column(nullable = false)
   private String name;

    /**
     * Description of the rule.
     */
    @Column(nullable = false)
    private String descr ;

    /**
     s
     * {@code true} if the rule is controlled ; {@code false} otherwise.
     */
    @Column(nullable = false)
    private boolean status ;

    /**
     * IPv4 address of the agent
     */
    @Column(nullable = false)
    private String ip ;

    /**
     * Optional ID of the related service (foreign key)
     */
    @Column()
    private Integer  service ;

    public Rules(){}


    public Rules(String name,String descr, boolean status, String ip) {
        this.name = name ;this.descr = descr; this.status = status; this.ip = ip;
    }
    public Rules(String name,String descr, boolean status, String ip, int service){
        this.name = name; this.descr = descr; this.status = status;
        this.ip = ip; this.service = service;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescr() {
        return descr;
    }

    public void setDescr(String descr) {
        this.descr = descr;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public Integer getService() {
        return service;
    }

    public void setService(Integer service) {
        this.service = service;
    }
}
