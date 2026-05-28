package marius.server.data;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.DynamicInsert;

/**
 * Represents a user entity stored in the PostgreSQL database.
 * <p>
 * <strong>Important:</strong> The {@code password} field must be encrypted using
 * the {@code hashPassword} method from the {@code Tool} package before saving to the database.
 * </p>
 *
 * @author Marius Berinde Dumitru
 */
@DynamicInsert
@Data
@Entity
@Table(name = "users")
public class User{
    @Id
    @Column(name="email")
    private String email;

    @Column(name="username" , unique = true, nullable = false)
    private String username;

    @Column(name="pwd",nullable = false)
    private String  password;

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "role", nullable = false)
    private RoleEnum role;

    public User(){}

    public User(String username,String email , String password, RoleEnum role) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    public String getEmail() {
        return email;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public RoleEnum getRole() {
        return role;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {  this.password = password; }

    public void setRole(RoleEnum newRole) {
            this.role = newRole;
    }

    @Override
    public String toString() {
        return "User{" +
                "email='" + email + '\'' +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", role=" + role +
                '}';
    }
}
