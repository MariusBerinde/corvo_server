package marius.server.data;

import jakarta.persistence.*;
import lombok.Data;

/**
 * Represents the details of the Lynis configuration to share with the Python agent.
 * <strong>Notes:</strong>
 * <ul>
 *     <li>The {@code auditor} field is the name of the user who launches the scan.</li>
 *     <li>The {@code ip} field is the IPv4 address of the agent where the configuration applies.</li>
 *     <li>The {@code loaded} field is {@code true} when the skipped tests are loaded on the agent; {@code false} otherwise.</li>
 *     <li>The {@code listIdSkippedTest} field contains a comma-separated list of IDs of the skipped tests.
 *     If {@code null}, it means that all rules must be checked.</li>
 * </ul>
 *
 * @author Marius Berinde Dumitru
 */
@Data
@Entity
@Table(name = "lynis")
public class Lynis {

    /**
     * Unique identifier for the configuration (auto-generated).
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private int id;

    /**
     * The name of user who created the config
     */
    @Column(nullable = false)
    private String auditor;

    /**
     * the ip address (IPV4 version) where the config is stored
     */
    @Column(nullable = false)
    private String ip ;

    /**
     * The list of the id for the tests that will be skipped
     */
    @Column(name = "list_id_skipped_test", nullable = true)
    private String listIdSkippedTest;

    /**
     * Says if the listIdSkippedTest is loaded on the python agent
     */
    @Column(nullable = false)
    private Boolean loaded=false;

    public Lynis() {}
    public Lynis( String auditor, String ip, Boolean loaded) {
        this.auditor = auditor; this.ip = ip; this.loaded = loaded;
    }
    public Lynis( String auditor, String ip, String listIdSkippedTest, Boolean loaded ) {
        this.auditor = auditor; this.ip = ip; this.listIdSkippedTest = listIdSkippedTest; this.loaded = loaded;
    }
    public int getId() {
        return id;
    }

    public String getListIdSkippedTest() {
        return listIdSkippedTest;
    }

    public void setListIdSkippedTest(String listIdSkippedTest) {
        this.listIdSkippedTest = listIdSkippedTest;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getAuditor() {
        return auditor;
    }

    public void setAuditor(String auditor) {
        this.auditor = auditor;
    }

    public Boolean getLoaded() {
        return loaded;
    }

    public void setLoaded(Boolean loaded) {
        this.loaded = loaded;
    }
}
