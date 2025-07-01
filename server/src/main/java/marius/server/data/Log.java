package marius.server.data;

import jakarta.persistence.*;
import lombok.Data;

import java.sql.Timestamp;

@Data
@Entity
@Table(name = "log")
public class Log {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column()
    private int id;

    @Column()
    private Timestamp data;

    @Column(name = "user_email",nullable = false)
    private String userEmail;

    @Column(nullable = true)
    private String ip;

    @Column()
    private Integer service ;

    @Column(nullable = false)
    private String descr ;

    public Log(){}

    public Log(String userEmail,  String descr) {
        this.userEmail = userEmail; this.descr = descr;
    }

    public Log(String userEmail,  String descr,Timestamp data) {
        this.userEmail = userEmail; this.descr = descr; this.data = data;
    }
    public Log(String userEmail,  String ip ,String descr ) {
        this.userEmail = userEmail; this.ip = ip; this.descr = descr;
    }
    public Log(String userEmail, String ip,  String descr, Timestamp data) {
        this.userEmail = userEmail; this.ip = ip;   this.descr = descr; this.data = data;
    }
    public Log(String userEmail, String ip, Integer service, String descr, Timestamp data) {
        this.userEmail = userEmail; this.ip = ip;  this.service = service; this.descr = descr;this.data = data;
    }
}
