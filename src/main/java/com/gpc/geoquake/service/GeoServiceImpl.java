package com.gpc.geoquake.service;

import com.gpc.geoquake.model.Geo;
import org.springframework.stereotype.Service;

@Service
public class GeoServiceImpl implements GeoService {

  @Override
  public double distanceInKm(Geo c1, Geo c2) {
    double R = 6371; // Radio de la Tierra en km
    double latDistance = Math.toRadians(c2.getLatitude() - c1.getLatitude());
    double lonDistance = Math.toRadians(c2.getLongitude() - c1.getLongitude());

    double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
        + Math.cos(Math.toRadians(c1.getLatitude())) * Math.cos(Math.toRadians(c2.getLatitude()))
        * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    return R * c;
  }
}
