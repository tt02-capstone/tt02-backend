package com.nus.tt02backend.models;


import com.nus.tt02backend.models.enums.DealCategoryEnum;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class Deal {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long deal_id;

    @Column(nullable = false)
    private Integer discount_percent;

    @Column(nullable = false)
    private LocalDateTime start_datetime;

    @Column(nullable = false)
    private LocalDateTime end_datetime;

    @Column(nullable = true)
    private String promo_code;

    @Column(nullable = false)
    private Boolean is_govt_voucher;

    @ElementCollection
    @CollectionTable(name="deal_image_list")
    private ArrayList<String> deal_image_list = new ArrayList<>();

    @Column(nullable = false)
    private Boolean is_published;

    @Column(nullable = true)
    private Date publish_date;

    @Enumerated(EnumType.STRING)
    private DealCategoryEnum deal_type;

    public Deal(Long deal_id, Integer discount_percent, LocalDateTime start_datetime, LocalDateTime end_datetime, Boolean is_govt_voucher, ArrayList<String> image_list, Boolean is_published, DealCategoryEnum deal_type) {
        this.deal_id = deal_id;
        this.discount_percent = discount_percent;
        this.start_datetime = start_datetime;
        this.end_datetime = end_datetime;
        this.is_govt_voucher = is_govt_voucher;
        this.deal_image_list = image_list;
        this.is_published = is_published;
        this.deal_type = deal_type;
    }
}
