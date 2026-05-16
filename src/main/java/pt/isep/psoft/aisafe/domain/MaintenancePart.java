package pt.isep.psoft.aisafe.domain;

import jakarta.persistence.*;
import org.springframework.util.Assert;

@Entity
public class MaintenancePart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long pk;

    @Embedded
    @Column(unique = true, nullable = false)
    private PartNumber partNumber;

    @Column(nullable = false)
    private String name;

    private String description;

    @Column(nullable = false)
    private Integer stockQuantity;

    @Column(nullable = false)
    private Integer minimumThreshold;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ComponentCategory componentCategory;

    protected MaintenancePart() {}

    public MaintenancePart(PartNumber partNumber, String name, String description, Integer stockQuantity, Integer minimumThreshold, ComponentCategory componentCategory) {
        Assert.notNull(partNumber, "Part number is required.");
        Assert.hasText(name, "Name is required.");
        Assert.notNull(stockQuantity, "Stock quantity cannot be null.");
        Assert.isTrue(stockQuantity >= 0, "Stock quantity cannot be negative.");
        Assert.notNull(minimumThreshold, "Minimum threshold cannot be null.");
        Assert.isTrue(minimumThreshold >= 0, "Minimum threshold cannot be negative.");
        Assert.notNull(componentCategory, "Component category is required.");

        this.partNumber = partNumber;
        this.name = name;
        this.description = description;
        this.stockQuantity = stockQuantity;
        this.minimumThreshold = minimumThreshold;
        this.componentCategory = componentCategory;
    }

    public Long getPk() { return pk; }
    public PartNumber getPartNumber() { return partNumber; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public Integer getStockQuantity() { return stockQuantity; }
    public Integer getMinimumThreshold() { return minimumThreshold; }
    public ComponentCategory getComponentCategory() { return componentCategory; }
}