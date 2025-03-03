package io.github.xilinjia.krdb.test.mongodb.common

import io.github.xilinjia.krdb.ext.realmListOf
import io.github.xilinjia.krdb.types.AsymmetricRealmObject
import io.github.xilinjia.krdb.types.EmbeddedRealmObject
import io.github.xilinjia.krdb.types.RealmList
import io.github.xilinjia.krdb.types.RealmObject
import io.github.xilinjia.krdb.types.annotations.PersistedName
import io.github.xilinjia.krdb.types.annotations.PrimaryKey
import org.mongodb.kbson.BsonObjectId
import org.mongodb.kbson.ObjectId

class DeviceParent : RealmObject {
    @PersistedName("_id")
    @PrimaryKey
    var id: ObjectId = BsonObjectId()
    var device: Device? = null
}

class Measurement : AsymmetricRealmObject {
    @PersistedName("_id")
    @PrimaryKey
    var id: ObjectId = BsonObjectId()
    var type: String = "temperature"
    var value: Float = 0.0f
    var device: Device? = null
    var backups: RealmList<BackupDevice> = realmListOf()
}

class BackupDevice() : EmbeddedRealmObject {
    constructor(name: String, serialNumber: String) : this() {
        this.name = name
        this.serialNumber = serialNumber
    }
    var name: String = ""
    var serialNumber: String = ""
}

class Device() : EmbeddedRealmObject {
    constructor(name: String, serialNumber: String) : this() {
        this.name = name
        this.serialNumber = serialNumber
    }
    var name: String = ""
    var serialNumber: String = ""
    var backupDevice: BackupDevice? = null
}

class AsymmetricA : AsymmetricRealmObject {
    @PrimaryKey
    var _id: ObjectId = ObjectId()
    var child: EmbeddedB? = null
}

class EmbeddedB : EmbeddedRealmObject {
    var child: StandardC? = null
}

class StandardC : RealmObject {
    @PrimaryKey
    var _id: ObjectId = ObjectId()
    var name: String = ""
}
