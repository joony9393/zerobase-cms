package com.zerobase.cms.order.domain;

import com.zerobase.cms.order.domain.product.AddProductForm;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.envers.AuditOverride;
import org.hibernate.envers.Audited;

@Entity
@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@AuditOverride(forClass = BaseEntity.class)
@Audited // 엔티티 내용의 변화를 추적하기 위한 것. 클래스 단위로 붙일 경우, 엔티티 클래스의 필드 중 어느 하나라도 변화가 있을 경우 새로운 튜플로 저장하겠다는 의미.
public class Product extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long sellerId;

    private String name;

    private String description;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "product_id")
    private List<ProductItem> productItems = new ArrayList<>();


    public static Product of(Long sellerId, AddProductForm form){
        return Product.builder()
            .sellerId(sellerId)
            .name(form.getName())
            .description(form.getDescription())
            .productItems(form.getItems().stream().map( piForm -> ProductItem.of(sellerId, piForm)).collect(
                Collectors.toList()))
            .build();
    }
}
