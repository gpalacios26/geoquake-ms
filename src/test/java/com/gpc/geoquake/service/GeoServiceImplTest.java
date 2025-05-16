package com.gpc.geoquake.service;

import static org.junit.jupiter.api.Assertions.*;

import com.gpc.geoquake.model.Geo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

class GeoServiceImplTest {

  @InjectMocks
  private GeoServiceImpl service;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  void testDistanceInKm() {
    // Arrange
    Geo point1 = new Geo(40.7128, -74.0060); // Nueva York
    Geo point2 = new Geo(34.0522, -118.2437); // Los Ángeles

    // Act
    double distance = service.distanceInKm(point1, point2);

    // Assert
    assertEquals(3935, distance, 1); // 3935 km con un margen de error de 1 km
  }
}
