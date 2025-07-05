package marius.server.data;

import jakarta.persistence.*;
import lombok.Data;


@Data
@Entity
@Table(name = "service")
public class AgentService {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column()
    private int id;

    @Column(nullable = false)
    private String ip;

    @Column(nullable = false)
    private String name;

    @Column(nullable = true)
    private String description;

    @Column(name = "porta",nullable = false)
    private Integer porta;

    @Column(name="automatic_start",nullable = false)
    private boolean automaticStart;

    @Column(nullable = false)
    private boolean state ;

    public AgentService() {}

    public AgentService(String ip, String name, String description, boolean automatic_start, boolean state) {
        this.ip = ip; this.name = name; this.description = description;
        this.automaticStart = automatic_start; this.state = state;
    }
    public AgentService(String ip, String name, String description, int porta, boolean automatic_start, boolean state) {
        this.ip = ip; this.name = name; this.description = description; this.porta = porta;
        this.automaticStart = automatic_start; this.state = state;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getPorta() {
        return porta;
    }

    public void setPorta(int porta) {
        this.porta = porta;
    }

    public boolean isAutomaticStart() {
        return this.automaticStart;
    }

    public void setAutomatic_start(boolean automatic_start) {
        this.automaticStart = automatic_start;
    }

    public boolean isState() {
        return state;
    }

    public void setState(boolean state) {
        this.state = state;
    }
}
