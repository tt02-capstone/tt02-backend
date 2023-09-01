package com.nus.tt02backend.models;


import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;

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

//    discount_percent: Integer
//+start_datetime: DateTime
//+end_datetime: DateTime
//+promo_code : String
//+is_govt_voucher : Boolean
//+image : String
//+is_published : boolean
//+publish_date : Date
//+deal_type: DealCategoryEnum
}
