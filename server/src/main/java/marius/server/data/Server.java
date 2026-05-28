package marius.server.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

/**
 * Represents a Python agent stored in the {@code server} table.
 * <strong>Notes:</strong>
 * <ul>
 *     <li>The {@code ip} field must be in IPv4 format and must be unique.</li>
 *     <li>The {@code state} field is {@code true} when the client is reachable, {@code false} otherwise.</li>
 *     <li>The {@code port} field stores the agent's port number (0–65,535).
 *     It is marked as {@code @Transient} and {@code @JsonIgnore}, so it is excluded from JSON serialization and is not persisted to the database.</li>
 * </ul>
 *
 * @author Marius Berinde Dumitru
 */
@Data
@Entity
@Table(name = "server")
public class Server {

    /**
     * Unique identifier for the server (auto-generated).
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column()
    private int id;

    /**
     * IPv4 address of the agent, must be unique
     */
    @Column(unique = true,nullable = false)
    private String ip;

    /**
     * {@code true} if the agent is reachable; {@code false} otherwise.
     */
    @Column(nullable = false)
    private boolean state;

    /**
     * Optional human-readable name for the agent.
     */
    @Column(nullable = true)
    private String name;

    /**
     * Optional description of the server
     */
    @Column
    private String descr;

    /**
     * Port number used to contact the agent (0–65,535).
     * Ignored by persistence and JSON serialization.
     */
    @Transient
    @JsonIgnore
    private int port;

    public Server(){}
    public Server( String ip, boolean state, String name, String descr, int port) {
         this.ip = ip; this.state = state; this.name = name; this.descr = descr; this.port = port;
    }

    public int getId() {
        return id;
    }

    public String getIp() {
        return ip;
    }

    public boolean isState() {
        return state;
    }

    public String getName() {
        return name;
    }

    public String getDescr() {
        return descr;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public void setState(boolean state) {
        this.state = state;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescr(String descr) {
        this.descr = descr;
    }
    public void setPort(int port) {
        this.port = port;
    }
    public int getPort() {
        return port;
    }
}
