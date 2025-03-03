package io.realm.sample.minandroidsample

import io.github.xilinjia.krdb.Realm
import io.github.xilinjia.krdb.RealmConfiguration
import io.github.xilinjia.krdb.types.RealmObject

class Sample: RealmObject {
    var name: String = ""
}

actual class Platform actual constructor() {
    val config = RealmConfiguration.create(schema = setOf(Sample::class))
    val realm = Realm.open(config)
    actual val platform: String = "Android ${android.os.Build.VERSION.SDK_INT}"
}
