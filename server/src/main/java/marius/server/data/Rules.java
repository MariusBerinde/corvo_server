package marius.server.data;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "rules")
public class Rules {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column()
    private int id;
    @Column(nullable = false)
    String descr ;

    @Column(nullable = false)
    boolean status ;

    @Column(nullable = false)
    String ip ;

    @Column()
    private Integer  service ;

    public Rules(){}


    public Rules(String descr, boolean status, String ip) {
        this.descr = descr; this.status = status; this.ip = ip;
    }
    public Rules(String descr, boolean status, String ip, int service){
        this.descr = descr; this.status = status;
        this.ip = ip; this.service = service;
    }
}
