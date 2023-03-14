package fi.metatavu.noheva.geometry

import org.locationtech.jts.geom.Coordinate
import org.locationtech.jts.geom.GeometryFactory
import fi.metatavu.noheva.api.spec.model.Bounds
import fi.metatavu.noheva.api.spec.model.Coordinates
import fi.metatavu.noheva.api.spec.model.Polygon
import org.locationtech.jts.geom.Point

/**
 * Converts spec Polygon to Geometry Polygon
 *
 * @param polygon spec polygon
 * @return null or Geometry Polygon
 */
fun getPolygon(polygon: Polygon?): org.locationtech.jts.geom.Polygon? {

    polygon ?: return null

    val geometryFactory = GeometryFactory()
    val coordinates = mutableListOf<Coordinate>()
    polygon.coordinates?.forEach { shape ->
        shape.forEach { coordinate ->
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
fun getGeoShape(polygon: org.locationtech.jts.geom.Polygon?): Polygon? {
    polygon ?: return null

    val coordinates: MutableList<MutableList<MutableList<Double>>> = mutableListOf(mutableListOf(mutableListOf()))
    polygon.coordinates?.forEachIndexed { index, coordinate ->
        coordinates[0].add(mutableListOf())
        coordinates[0][index].add(coordinate.x)
        coordinates[0][index].add(coordinate.y)
    }
    return Polygon(
        type = polygon.geometryType,
        coordinates = coordinates
    )
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


    return Bounds(
        southWestCorner = Coordinates(
            latitude = swBoundPoint.x,
            longitude = swBoundPoint.y
        ),
        northEastCorner = Coordinates(
            latitude = neBoundPoint.x,
            longitude = neBoundPoint.y
        )
    )
}
