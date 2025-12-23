package floorida.example.floorida.Item.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "item")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "item_id")
    private Long itemId;

    @Column(nullable = false, length = 50)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ItemType type;

    @Column(nullable = false)
    private int price;

    @Column(name = "img_url", nullable = false, length = 255)
    private String imgUrl;

    @Column(columnDefinition = "TEXT")
    private String description;

    /* ========= 생성자 ========= */

    public Item(String name,
                ItemType type,
                int price,
                String imgUrl,
                String description) {

        this.name = name;
        this.type = type;
        this.price = price;
        this.imgUrl = imgUrl;
        this.description = description;
    }
}
