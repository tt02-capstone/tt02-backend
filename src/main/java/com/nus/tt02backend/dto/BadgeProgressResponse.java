package com.nus.tt02backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BadgeProgressResponse {
    private Double ACCOMMODATION_EXPERT;
    private Double TELECOM_EXPERT;
    private Double ATTRACTION_EXPERT;
    private Double FOODIE;
    private Double TOUR_EXPERT;
    private Double TOP_CONTRIBUTOR;
}
