/*
 * Copyright 2023 Realm Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.xilinjia.krdb.internal.geo

import io.github.xilinjia.krdb.annotations.ExperimentalGeoSpatialApi
import io.github.xilinjia.krdb.types.geo.GeoBox
import io.github.xilinjia.krdb.types.geo.GeoPoint

@OptIn(ExperimentalGeoSpatialApi::class)
public data class UnmanagedGeoBox(
    public override val bottomLeft: GeoPoint,
    public override val topRight: GeoPoint
) : GeoBox {
    public override fun toString(): String {
        return "geoBox([${bottomLeft.longitude}, ${bottomLeft.latitude}], [${topRight.longitude}, ${topRight.latitude}])"
    }
}
