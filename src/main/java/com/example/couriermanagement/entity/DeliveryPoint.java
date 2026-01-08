package com.example.couriermanagement.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.BatchSize;
import org.hibernate.proxy.HibernateProxy;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(
        name = "delivery_points",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"delivery_id", "sequence"})
        }
)
@Builder(toBuilder = true)
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryPoint {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Builder.Default
    private Long id = 0L;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "delivery_id", nullable = false)
    @ToString.Exclude
    private Delivery delivery;

    @Column(name = "sequence", nullable = false)
    private Integer sequence;

    @Column(name = "latitude", precision = 10, scale = 8, nullable = false)
    private BigDecimal latitude;

    @Column(name = "longitude", precision = 11, scale = 8, nullable = false)
    private BigDecimal longitude;

    @OneToMany(mappedBy = "deliveryPoint", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    @Builder.Default
    private List<DeliveryPointProduct> deliveryPointProducts = new ArrayList<>();

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy proxy ? proxy.getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy proxy ? proxy.getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        DeliveryPoint that = (DeliveryPoint) o;
        return getId() != null && Objects.equals(getId(), that.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy proxy ? proxy.getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }
}