#!/bin/bash

# This script replaces all secret tokens in the portal-ext.properties file with their actual values.
# It aims to be a rough replacement for the template driver used in docker swarm configs.

FILE="/opt/liferay/portal-ext.properties"

# Create a temp file to store the updated content
TMP_FILE=$(mktemp)

# Process the file line by line
while IFS= read -r line || [[ -n "$line" ]]; do
    # Replace all secret tokens in the line
    while [[ "$line" =~ \{\{\ secret\ \"([^\"]+)\"\ \}\} ]]; do
        SECRET_NAME="${BASH_REMATCH[1]}"
        SECRET_PATH="/run/secrets/$SECRET_NAME"

        if [[ -f "$SECRET_PATH" ]]; then
            SECRET_VALUE=$(<"$SECRET_PATH")
            # Escape special characters
            ESCAPED_SECRET=$(printf '%s\n' "$SECRET_VALUE" | sed 's/[&/\]/\\&/g')
            line="${line//\{\{ secret \"$SECRET_NAME\" \}\}/$ESCAPED_SECRET}"
        else
            echo "Warning: Secret file $SECRET_PATH not found."
            line="${line//\{\{ secret \"$SECRET_NAME\" \}\}/}"
        fi
    done

    echo "$line"
done < "$FILE" > "$TMP_FILE"

# Replace the original file with the temp file
mv "$TMP_FILE" "$FILE"
