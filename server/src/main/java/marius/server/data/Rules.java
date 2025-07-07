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
   private String name;

    @Column(nullable = false)
    private String descr ;

    @Column(nullable = false)
    private boolean status ;

    @Column(nullable = false)
    private String ip ;

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
