package io.github.xilinjia.krdb.internal.geo

import io.github.xilinjia.krdb.annotations.ExperimentalGeoSpatialApi
import io.github.xilinjia.krdb.types.geo.Distance
import io.github.xilinjia.krdb.types.geo.GeoCircle
import io.github.xilinjia.krdb.types.geo.GeoPoint

@OptIn(ExperimentalGeoSpatialApi::class)
public data class UnmanagedGeoCircle(
    public override val center: GeoPoint,
    public override val radius: Distance
) : GeoCircle {
    init {
        if (radius.inRadians < 0) {
            // Currently `Distance` does not allow negative values, but since a UDouble doesn't
            // exists, we also validate the input here, just in case.
            throw IllegalArgumentException("A negative radius is not allowed: $radius")
        }
    }

    override fun toString(): String {
        return "geoCircle([${center.longitude}, ${center.latitude}], ${radius.inRadians})"
    }
}
