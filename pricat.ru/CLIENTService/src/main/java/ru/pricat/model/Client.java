package ru.pricat.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "clients", indexes = {
        @Index(name = "idx_client_username", columnList = "username"),
        @Index(name = "idx_client_email", columnList = "email"),
        @Index(name = "idx_client_company", columnList = "companyName")
})
public class Client {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * Идентификатор клиента в системе Pricat
     */
    @NotBlank(message = "Username is required")
    @Column(name = "username", nullable = false, unique = true, length = 64)
    private String username;


    /**
     * Email клиента в системе Pricat
     */
    @Email(message = "Email should be valid")
    @NotBlank(message = "Email is required")
    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email;


    /**
     * Имя клиента в системе Pricat
     */
    @Column(name = "name")
    private String name;

    /**
     * Определение ролей клиента в системе Pricat
     */
    @NotBlank(message = "Roles are required")
    @Column(name = "roles", nullable = false, length = 200)
    private String roles = String.valueOf(Role.USER);

    /**
     * Название компании клиента в системе Pricat
     */
    @NotBlank(message = "Company name is required")
    @Column(name = "company_name")
    private String companyName;

    /**
     * Мобильный телефон клиента в системе Pricat
     */
    @Column(name = "mobile_phone", length = 20)
    private String mobilePhone;

    /**
     * Ссылка на аватар клиента в системе Pricat
     */
    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;

    /**
     * Флаг, указывающий, является ли клиент поставщиком
     */
    @NotNull(message = "Is supplier flag is required")
    @Column(name = "is_supplier", nullable = false)
    private Boolean isSupplier = false;

    /**
     * S3 путь к папке с прайс-листами клиента
     */
    @Column(name = "company_folder")
    private String companyFolder;

    /**
     * Варианты получения прайс-листов клиента
     */
    @NotNull(message = "Price list obtaining way is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "pricelist_obtaining_way", nullable = false, length = 20)
    private PricelistObtainingWay pricelistObtainingWay = PricelistObtainingWay.MANUAL;

    /**
     * Варианты возможных форматов прайс-листов клиента
     */
    @NotNull(message = "Price list format is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "pricelist_format", nullable = false, length = 10)
    private PricelistFormat pricelistFormat = PricelistFormat.XLSX;

    /**
     * Поле для хранения даты создания клиента
     */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    /**
     * Поле для хранения даты последнего обновления клиента
     */
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (createdAt == null) {
            createdAt = OffsetDateTime.now();
        }
        updatedAt = OffsetDateTime.now();

        if (roles == null || roles.trim().isEmpty()) {
            roles = "USER";
        }
        if (companyName == null || companyName.trim().isEmpty()) {
            companyName = "Unknown company";
        }
        if (isSupplier == null) {
            isSupplier = false;
        }
        if (pricelistObtainingWay == null) {
            pricelistObtainingWay = PricelistObtainingWay.MANUAL;
        }
        if (pricelistFormat == null) {
            pricelistFormat = PricelistFormat.XLSX;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }

    public Client(String username, String email, String name) {
        this();
        this.username = username;
        this.email = email;
        this.name = name;
    }

    public Client(String username, String email, String name, String roles) {
        this(username, email, name);
        this.roles = roles;
    }

    public boolean isSupplier() {
        return isSupplier;
    }

    public void addRole(String role) {
        if (roles == null || roles.isEmpty()) {
            roles = role;
        } else if (!hasRole(role)) {
            roles += "," + role;
        }
    }

    public boolean hasRole(String role) {
        return roles != null && roles.contains(role);
    }

    public void removeRole(String role) {
        if (roles != null && roles.contains(role)) {
            roles = roles.replace(role, "").replace(",,", ",").trim();
            if (roles.endsWith(",")) {
                roles = roles.substring(0, roles.length() - 1);
            }
            if (roles.startsWith(",")) {
                roles = roles.substring(1);
            }
        }
    }

    public enum Role {
        ADMIN,
        MANAGER,
        USER
    }

    public enum PricelistObtainingWay {
        MANUAL,
        EMAIL,
        S3,
        FTP,
        SFTP,
        SELF_API,
        CLIENT_API
    }

    public enum PricelistFormat {
        JSON,
        XML,
        XLSX,
        XLS,
        CSV,
        TXT
    }
}
