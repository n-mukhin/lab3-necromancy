package project.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "web_users", schema = "s409203")
public class WebUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "username", length = 50, nullable = false, unique = true)
    private String username;

    @Column(name = "password_hash", length = 256, nullable = false)
    private String passwordHash;

    @Column(name = "salt", length = 50, nullable = false)
    private String salt;

    @Column(
            name = "registration_date",
            nullable = false,
            updatable = false,
            insertable = false,
            columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP"
    )
    private LocalDateTime registrationDate;

    @ManyToMany(mappedBy = "users", fetch = FetchType.LAZY)
    private Set<ResultEntity> results = new HashSet<>();

    public WebUser() {
    }

    public WebUser(
            Long id,
            String username,
            String passwordHash,
            String salt,
            LocalDateTime registrationDate,
            Set<ResultEntity> results
    ) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.salt = salt;
        this.registrationDate = registrationDate;
        this.results = (results != null ? results : new HashSet<>());
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private String username;
        private String passwordHash;
        private String salt;
        private LocalDateTime registrationDate;
        private Set<ResultEntity> results = new HashSet<>();

        private Builder() {
        }

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder username(String username) {
            this.username = username;
            return this;
        }

        public Builder passwordHash(String passwordHash) {
            this.passwordHash = passwordHash;
            return this;
        }

        public Builder salt(String salt) {
            this.salt = salt;
            return this;
        }

        public Builder registrationDate(LocalDateTime registrationDate) {
            this.registrationDate = registrationDate;
            return this;
        }

        public Builder results(Set<ResultEntity> results) {
            this.results = (results != null ? results : new HashSet<>());
            return this;
        }

        public WebUser build() {
            return new WebUser(
                    id,
                    username,
                    passwordHash,
                    salt,
                    registrationDate,
                    results
            );
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }

    public LocalDateTime getRegistrationDate() {
        return registrationDate;
    }

    public Set<ResultEntity> getResults() {
        return results;
    }

    public void setResults(Set<ResultEntity> results) {
        this.results = (results != null ? results : new HashSet<>());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof WebUser)) return false;
        WebUser webUser = (WebUser) o;
        return Objects.equals(id, webUser.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
