# We cannot discard unused symbols for the non-test configurations as it might  all symbols not used
-dontoptimize
-dontshrink

## Required to make assertions on incorrect type messages in dynamic realm object tests pass
-keep class io.github.xilinjia.krdb.types.BaseRealmObject
-keep class io.github.xilinjia.krdb.types.RealmUUID

## Required to make introspection by reflection in NullabilityTests work
-keep class io.github.xilinjia.krdb.types.MutableRealmInt
-keep class io.github.xilinjia.krdb.entities.Nullability {
   *;
}

## Required to make introspection by reflection in PrimaryKeyTests work
-keepclassmembers class io.github.xilinjia.krdb.entities.primarykey.* {
  *;
}
