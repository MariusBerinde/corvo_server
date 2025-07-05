package marius.server.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "server")
public class Server {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column()
    private int id;

    @Column(unique = true,nullable = false)
    private String ip;

    @Column(nullable = false)
    private boolean state;

    @Column(nullable = true)
    private String name;

    @Column
    private String descr;

    @Transient
    @JsonIgnore //is user for exclude port for the serialization
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
