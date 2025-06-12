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
}
