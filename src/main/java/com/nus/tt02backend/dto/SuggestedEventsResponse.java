package com.nus.tt02backend.dto;

import com.nus.tt02backend.models.Attraction;
import com.nus.tt02backend.models.Restaurant;
import com.nus.tt02backend.models.enums.RoomTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SuggestedEventsResponse {

    private List<Restaurant> restaurants;
    private List<Attraction> attractions;
}
