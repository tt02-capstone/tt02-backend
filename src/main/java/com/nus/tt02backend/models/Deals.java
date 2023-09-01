package com.nus.tt02backend.models;


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
public class Deals {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long deal_id;

    private Integer discount_percent;
    private LocalDateTime start_datetime;
    private LocalDateTime end_datetime;
    private String promo_code;
    private Boolean is_govt_voucher;

    @ElementCollection
    @CollectionTable(name="image_list")
    private ArrayList<String> image_list = new ArrayList<>();
    private Boolean is_published;
    private Date publish_date;
//    private DealCategoryEnum deal_type;

}
