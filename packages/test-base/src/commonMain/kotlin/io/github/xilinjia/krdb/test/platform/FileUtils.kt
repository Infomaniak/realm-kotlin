package io.github.xilinjia.krdb.test.platform

import okio.FileSystem

/**
 * Reference to Okio's [FileSystem] in a platform-agnostic way.
 */
expect val platformFileSystem: FileSystem
