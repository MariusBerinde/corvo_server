package marius.server.data;

import jakarta.persistence.*;
import lombok.Data;

import java.sql.Timestamp;

/**
 * Represents the logs received from the Angular interface or backend,
 * corresponding to the {@code log} table in the database.
 * <p>
 * Logs capture user actions, timestamps, optional target IPs and services, and operation descriptions.
 * </p>
 *
 * @author Marius Berinde Dumitru
 */

@Data
@Entity
@Table(name = "log")
public class Log {

    /**
     * Unique identifier for the log (auto-generated).
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column()
    private int id;

    /**
     * Timestamp when the log was created.
     */
    @Column()
    private Timestamp data;

    /**
     * The user (foreign key that points to a user email)
     */
    @Column(name = "user_email",nullable = false)
    private String userEmail;

    /**
     * IPv4 address where the operation occurred.
     * If {@code null}, it means the operation was performed via the interface.
     */
    @Column(nullable = true)
    private String ip;

    /**
     * Optional the id of the service
     */
    @Column(nullable = true)
    private Integer service ;

    /**
     * The description of the operation nullable
     */
    @Column(nullable = false)
    private String descr ;

    /**
     * Empty constructor
     */
    public Log(){}


    /**
     * Constuctor with email , descr and data
     * @param userEmail the email of the user
     * @param descr the description of the logged operation
     * @param data a timestamp for know when operation is done
     */
    public Log(String userEmail,  String descr,Timestamp data) {
        this.userEmail = userEmail; this.descr = descr; this.data = data;
    }

    /**
     * Constuctor with email,ip and descr
     * @param userEmail the email of the user
     * @param ip  the IPV4 address
     * @param descr the description of the logged operation
     */
    public Log(String userEmail,  String ip ,String descr ) {
        this.userEmail = userEmail; this.ip = ip; this.descr = descr;
    }

    /**
     * Constructor with email,ip,desc and data
     * @param userEmail the email of the user
     * @param ip  the IPV4 address
     * @param descr the description of the logged operation
     * @param data a timestamp for know when operation is done
     */
    public Log(String userEmail, String ip,  String descr, Timestamp data) {
        this.userEmail = userEmail; this.ip = ip;   this.descr = descr; this.data = data;
    }

    /**
     * @param userEmail the email of the user
     * @param ip  the IPV4 address
     * @param service the id of the service
     * @param descr the description of the logged operation
     * @param data a timestamp for know when operation is done
     */
    public Log(String userEmail, String ip, Integer service, String descr, Timestamp data) {
        this.userEmail = userEmail; this.ip = ip;  this.service = service; this.descr = descr;this.data = data;
    }
}
