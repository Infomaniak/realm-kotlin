#!/bin/bash

# Define the old and new package names with escaped periods
OLD_PACKAGE="io.realm.kotlin"
NEW_PACKAGE="io.github.xilinjia.krdb"
OLD_PACKAGE_PATH="io/realm/kotlin"
NEW_PACKAGE_PATH="io/github/xilinjia/krdb"

# Escape the periods for sed replacement
ESCAPED_OLD_PACKAGE=$(echo "$OLD_PACKAGE" | sed 's/\./\\./g')
ESCAPED_NEW_PACKAGE=$(echo "$NEW_PACKAGE" | sed 's/\./\\./g')

ESCAPED_OLD_PACKAGE_PATH=$(echo "$OLD_PACKAGE_PATH" | sed 's/\//\\\//g')
ESCAPED_NEW_PACKAGE_PATH=$(echo "$NEW_PACKAGE_PATH" | sed 's/\//\\\//g')

echo "🔍 Searching for references to $OLD_PACKAGE and replacing with $NEW_PACKAGE in all files..."

# 1️⃣ Replace package references in relevant source files, including .json, .ir, .hpp, .cpp, .kts, etc.
find . -type f \( -name "*.kt" -o -name "*.java" -o -name "*.xml" -o -name "*.json" -o -name "*.ir" -o -name "build.gradle*" -o -name "settings.gradle*" -o -name "*.pro" -o -name "*.hpp" -o -name "*.cpp" -o -name "*.kts" \) \
    -exec sed -i.bak "s/$ESCAPED_OLD_PACKAGE/$ESCAPED_NEW_PACKAGE/g" {} +

# Remove backup files created by macOS `sed`
find . -type f -name "*.bak" -exec rm {} +

echo "✅ Package references updated."

# 2️⃣ Rename directories across all Kotlin Multiplatform (KMP) source sets
echo "📂 Renaming package directories in all source sets..."

find . -type d -path "*/src/*/kotlin/$OLD_PACKAGE_PATH" | while read -r dir; do
    NEW_DIR=$(echo "$dir" | sed "s|$OLD_PACKAGE_PATH|$NEW_PACKAGE_PATH|g")

    # Ensure the new directory exists
    mkdir -p "$NEW_DIR"

    # Move all files from the old package directory to the new one
    mv "$dir"/* "$NEW_DIR/" 2>/dev/null

    echo "✅ Renamed: $dir → $NEW_DIR"
done

# 3️⃣ Remove any remaining empty io/realm directories
echo "🗑️ Cleaning up empty directories..."
find . -type d -empty -path "*/io/realm*" -exec rmdir {} +

# 4️⃣ (Optional) Verify and cleanup files after renaming, such as .json, .ir, .hpp, .cpp files
echo "🔍 Verifying changes in .json, .ir, .hpp, .cpp, .kts files..."
find . -type f \( -name "*.json" -o -name "*.ir" -o -name "*.hpp" -o -name "*.cpp" -o -name "*.kts" \) -exec grep -l "$OLD_PACKAGE" {} \; | while read -r file; do
    echo "✅ Updating $file"
    # Replace both old package and old path references
    sed -i "s/$ESCAPED_OLD_PACKAGE/$ESCAPED_NEW_PACKAGE/g" "$file"
    sed -i "s/$ESCAPED_OLD_PACKAGE_PATH/$ESCAPED_NEW_PACKAGE_PATH/g" "$file"  # Also replace package path in .hpp/.cpp/.json/.ir/.kts etc.
done

# 5️⃣ Running Gradle sync to make sure everything is fine
echo "🔄 Running Gradle sync..."
./gradlew clean

echo "🚀 Done! Your project is now using $NEW_PACKAGE, and old directories are removed."
