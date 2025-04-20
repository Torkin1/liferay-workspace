#!/bin/sh

PROJECT_HOME="."

# Set source and destination directories
SRC_DIR="$PROJECT_HOME/bundles/osgi/modules"
VOLUME_DIR="$PROJECT_HOME/liferay/deploy"

# Move all .jar files from source to destination, overwriting existing files
cp -f "$SRC_DIR"/*.jar "$VOLUME_DIR"/
