package fi.metatavu.muisti.geometry

import com.vividsolutions.jts.geom.Coordinate
import com.vividsolutions.jts.geom.GeometryFactory
import com.vividsolutions.jts.geom.Point
import fi.metatavu.muisti.api.spec.model.Bounds
import fi.metatavu.muisti.api.spec.model.Coordinates
import fi.metatavu.muisti.api.spec.model.Polygon

/**
 * Converts spec Polygon to Geometry Polygon
 *
 * @param polygon spec polygon
 * @return null or Geometry Polygon
 */
fun getPolygon(polygon: Polygon?): com.vividsolutions.jts.geom.Polygon? {

  polygon ?: return null

  val geometryFactory = GeometryFactory()
  val coordinates = mutableListOf<Coordinate>()
  polygon.shapes.forEach { shape ->
    shape.forEach {coordinate ->
      coordinates.add(Coordinate(coordinate[0], coordinate[1]))
    }
  }

  return geometryFactory.createPolygon(coordinates.toTypedArray())
}

/**
 * Convert Geometry Polygon from entity to spec Polygon.
 *
 * @param polygon spatial polygon object
 * @return null or GeoJSON with polygon data
 */
fun getGeoShape(polygon: com.vividsolutions.jts.geom.Polygon?): Polygon? {
  polygon ?: return null

  val result = Polygon()
  result.shapes = mutableListOf(mutableListOf())

  polygon.coordinates.forEachIndexed { index, coordinate ->
    result.shapes[0].add(mutableListOf<Double>())
    result.shapes[0][index].add(coordinate.x)
    result.shapes[0][index].add(coordinate.y)
  }
  return result
}

/**
 * Converts spec Coordinate to Geometry Point
 *
 * @param coordinates coordinates
 * @return null or Geometry Point
 */
fun getGeometryPoint(coordinates: Coordinates?): Point? {

  coordinates ?: return null

  val geometryFactory = GeometryFactory()
  return geometryFactory.createPoint(Coordinate(coordinates.latitude, coordinates.longitude))
}

/**
 * Convert Geometry Points to api spec Bounds
 *
 * @param neBoundPoint North East geometry point
 * @param swBoundPoint South West geometry point
 * @return null or bounds
 */
fun getBounds(neBoundPoint: Point?, swBoundPoint: Point?): Bounds? {

  neBoundPoint ?: return null
  swBoundPoint ?: return null

  val result = Bounds()
  result.southWestCorner = Coordinates()
  result.southWestCorner.latitude = swBoundPoint.x
  result.southWestCorner.longitude = swBoundPoint.y

  result.northEastCorner = Coordinates()
  result.northEastCorner.latitude = neBoundPoint.x
  result.northEastCorner.longitude = neBoundPoint.y

  return result
}
