package com.gpc.geoquake.service;

import com.gpc.geoquake.model.Geo;

public interface GeoService {

  double distanceInKm(Geo c1, Geo c2);
}
