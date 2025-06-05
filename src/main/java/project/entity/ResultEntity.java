package project.entity;

import jakarta.persistence.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "results", schema = "s409203")
public class ResultEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "result_sequence_generator")
    @SequenceGenerator(
            name = "result_sequence_generator",
            sequenceName = "results_id_seq",
            allocationSize = 1
    )
    private Long id;

    @Column(name = "x_value", nullable = false, precision = 5, scale = 2)
    private Double x;

    @Column(name = "y_value", nullable = false, precision = 5, scale = 2)
    private Double y;

    @Column(name = "r_value", nullable = false, precision = 5, scale = 2)
    private Double r;

    @Column(name = "hit_result", nullable = false)
    private Boolean hitResult;

    @Column(name = "hit_time", nullable = false)
    private LocalTime hitTime;

    @Column(name = "execution_time", nullable = false, precision = 10, scale = 6)
    private BigDecimal executionTime;

    @Column(
            name = "result_timestamp",
            updatable = false,
            insertable = false,
            columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP"
    )
    private LocalDateTime resultTimestamp;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "user_results",
            schema = "s409203",
            joinColumns = @JoinColumn(name = "result_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<WebUser> users = new HashSet<>();

    @Transient
    private int reverseId;

    public ResultEntity() {
    }

    public ResultEntity(
            Long id,
            Double x,
            Double y,
            Double r,
            Boolean hitResult,
            LocalTime hitTime,
            BigDecimal executionTime,
            LocalDateTime resultTimestamp,
            Set<WebUser> users,
            int reverseId
    ) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.r = r;
        this.hitResult = hitResult;
        this.hitTime = hitTime;
        this.executionTime = executionTime;
        this.resultTimestamp = resultTimestamp;
        this.users = (users != null ? users : new HashSet<>());
        this.reverseId = reverseId;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private Double x;
        private Double y;
        private Double r;
        private Boolean hitResult;
        private LocalTime hitTime;
        private BigDecimal executionTime;
        private LocalDateTime resultTimestamp;
        private Set<WebUser> users = new HashSet<>();
        private int reverseId;

        private Builder() {
        }

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder x(Double x) {
            this.x = x;
            return this;
        }

        public Builder y(Double y) {
            this.y = y;
            return this;
        }

        public Builder r(Double r) {
            this.r = r;
            return this;
        }

        public Builder hitResult(Boolean hitResult) {
            this.hitResult = hitResult;
            return this;
        }

        public Builder hitTime(LocalTime hitTime) {
            this.hitTime = hitTime;
            return this;
        }

        public Builder executionTime(BigDecimal executionTime) {
            this.executionTime = executionTime;
            return this;
        }

        public Builder resultTimestamp(LocalDateTime resultTimestamp) {
            this.resultTimestamp = resultTimestamp;
            return this;
        }

        public Builder users(Set<WebUser> users) {
            this.users = (users != null ? users : new HashSet<>());
            return this;
        }

        public Builder reverseId(int reverseId) {
            this.reverseId = reverseId;
            return this;
        }

        public ResultEntity build() {
            return new ResultEntity(
                    id,
                    x,
                    y,
                    r,
                    hitResult,
                    hitTime,
                    executionTime,
                    resultTimestamp,
                    users,
                    reverseId
            );
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Double getX() {
        return x;
    }

    public void setX(Double x) {
        this.x = x;
    }

    public Double getY() {
        return y;
    }

    public void setY(Double y) {
        this.y = y;
    }

    public Double getR() {
        return r;
    }

    public void setR(Double r) {
        this.r = r;
    }

    public Boolean getHitResult() {
        return hitResult;
    }

    public void setHitResult(Boolean hitResult) {
        this.hitResult = hitResult;
    }

    public LocalTime getHitTime() {
        return hitTime;
    }

    public void setHitTime(LocalTime hitTime) {
        this.hitTime = hitTime;
    }

    public BigDecimal getExecutionTime() {
        return executionTime;
    }

    public void setExecutionTime(BigDecimal executionTime) {
        this.executionTime = executionTime;
    }

    public LocalDateTime getResultTimestamp() {
        return resultTimestamp;
    }

    public Set<WebUser> getUsers() {
        return users;
    }

    public void setUsers(Set<WebUser> users) {
        this.users = (users != null ? users : new HashSet<>());
    }

    public int getReverseId() {
        return reverseId;
    }

    public void setReverseId(int reverseId) {
        this.reverseId = reverseId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ResultEntity)) return false;
        ResultEntity that = (ResultEntity) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    public Date getResultTimestampAsDate() {
        if (this.resultTimestamp == null) {
            return null;
        }
        return Date.from(this.resultTimestamp.atZone(ZoneId.systemDefault()).toInstant());
    }
}
