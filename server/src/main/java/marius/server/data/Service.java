package marius.server.data;

import jakarta.persistence.*;
import lombok.Data;


@Data
@Entity
@Table(name = "service")
public class Service {
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

    @Column(nullable = true)
    private int porta;

    @Column(nullable = false)
    private boolean automatic_start;

    @Column(nullable = false)
    private boolean state ;

    public Service() {}

    public Service(String ip, String name, String description,  boolean automatic_start, boolean state) {
        this.ip = ip; this.name = name; this.description = description;
        this.automatic_start = automatic_start; this.state = state;
    }
    public Service(String ip, String name, String description, int porta, boolean automatic_start, boolean state) {
        this.ip = ip; this.name = name; this.description = description; this.porta = porta;
        this.automatic_start = automatic_start; this.state = state;
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

    public boolean isAutomatic_start() {
        return automatic_start;
    }

    public void setAutomatic_start(boolean automatic_start) {
        this.automatic_start = automatic_start;
    }

    public boolean isState() {
        return state;
    }

    public void setState(boolean state) {
        this.state = state;
    }
}
