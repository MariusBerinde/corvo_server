package marius.server.data;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.DynamicInsert;

/**
 * Represents a user who has been pre-approved to register to the application,
 * corresponding to the {@code approvedusers} table in the database.
 * <p>
 * Each entry contains the user's email and the timestamp when the pre-approval was granted.
 * </p>
 *
 * @author Marius Berinde Dumitru
 */
@DynamicInsert
@Data
@Entity
@Table(name = "approvedusers")
public class ApprovedUsers {


    /**
     * Unique identifier for the ApprovedUser entry (auto-generated).
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id")
    private Integer id;

    /**
     * the email of the user that have the preapprovation for the registration
     */
    @Column(name="email" , unique = true, nullable = false)
    private String email;

    /**
     * A time stamp for know when the pre-approvation is done
     */
    @Column
    private LocalDateTime data;

    public ApprovedUsers() {}
    public ApprovedUsers( String email ) {
        this.email = email;
        //this.data = LocalDateTime.now();
    }
    public Integer getId(){ return this.id; }
    public String getEmail(){ return this.email; }
    public LocalDateTime getData(){ return this.data; }
    public void setId(Integer id){ this.id = id; }
    public void setEmail(String email){ this.email = email; }
    public void setData(LocalDateTime data){ this.data = data; }
}
