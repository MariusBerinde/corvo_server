package marius.server.data;

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

    @Column(nullable = false)
    private String name;

    @Column
    private String descr;

    public Server(){}
    public Server( String ip, boolean state, String name, String descr) {
         this.ip = ip; this.state = state; this.name = name; this.descr = descr;
    }


}
