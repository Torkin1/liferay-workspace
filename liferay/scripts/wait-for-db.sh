#!/usr/bin/sh

wait_for_it=/mnt/liferay/scripts/dependencies/wait-for-it.sh

set -e

# Wait for MySQL to be ready in 5 minutes at max
sh -c "${wait_for_it} mysql:3306 -t 3000"
