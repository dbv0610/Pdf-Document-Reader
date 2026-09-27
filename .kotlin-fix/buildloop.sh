D="$(cd "$(dirname "$0")" && pwd)"
while [ -f $D/AGENTS_RUNNING ]; do sleep 150; $D/build.sh; done
