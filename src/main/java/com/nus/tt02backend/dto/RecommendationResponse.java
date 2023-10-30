package com.nus.tt02backend.dto;

import com.nus.tt02backend.models.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecommendationResponse {

  private List<Attraction> attractionList;

  private List<Telecom> telecomList;

  private List<Accommodation> accommodationList;

  private List<Restaurant> restaurantList;

  private List<Tour> tourList;

}
