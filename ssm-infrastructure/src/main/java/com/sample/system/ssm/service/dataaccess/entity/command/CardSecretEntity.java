package com.sample.system.ssm.service.dataaccess.entity.command;
import com.sample.system.ssm.service.dataaccess.entity.Auditable;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.envers.AuditOverride;
import org.hibernate.envers.Audited;
import java.time.Instant;
import java.util.UUID;

/**
 * Entity for storing encrypted card secrets (PIN1, CVV2)
 * Following security best practices: only encrypted values are stored
 */
@Audited
@AuditOverride(forClass = Auditable.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "SSM_CARD_SECRETS", indexes = {
    @Index(name = "IDX_CARD_SECRETS_CARD_ID", columnList = "CARD_ID"),
    @Index(name = "IDX_CARD_SECRETS_TYPE_STATUS", columnList = "SECRET_TYPE, STATUS")
})
public class CardSecretEntity extends Auditable {

    @Id
    @Column(name = "CARD_SECRET_ID", nullable = false)
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID cardSecretId;

    @Column(name = "CARD_ID", nullable = false)
    private UUID cardId;

    @Column(name = "SECRET_TYPE", nullable = false, length = 10)
    private String secretType; // PIN1, CVV2

    @Column(name = "ENCRYPTED_VALUE", nullable = false, columnDefinition = "CLOB")
    private String encryptedValue; // Encrypted PIN/CVV2 from vault-service

    @Column(name = "HASH_VALUE", length = 512)
    private String hashValue; // HMAC hash for PIN1 validation

    @Column(name = "KEY_VERSION", length = 20)
    private String keyVersion; // Key version from vault-service (optional)

    @Column(name = "STATUS", nullable = false, length = 20)
    private String status; // ACTIVE, REVOKED

    @Column(name = "CREATED_AT", nullable = false)
    private Instant createdAt;

    @Column(name = "REVOKED_AT")
    private Instant revokedAt;
}

