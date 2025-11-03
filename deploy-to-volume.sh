#!/bin/sh

PROJECT_HOME="."
module=$1

# Set source and destination directories
# SRC_DIR="$PROJECT_HOME/bundles/osgi/modules"
VOLUME_DIR="$PROJECT_HOME/liferay/deploy"

chmod 777 "$module"
cp -af "$module" "$VOLUME_DIR"/
