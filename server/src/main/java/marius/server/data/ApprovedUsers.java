package marius.server.data;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.DynamicInsert;

@DynamicInsert
@Data
@Entity
@Table(name = "approvedusers")
public class ApprovedUsers {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id")
    private Integer id;

    @Column(name="email" , unique = true, nullable = false)
    private String email;

    @Column
    private LocalDateTime data;

    public ApprovedUsers() {}
    public ApprovedUsers( String email ) {
        this.email = email;
        //this.data = LocalDateTime.now();
    }
}
