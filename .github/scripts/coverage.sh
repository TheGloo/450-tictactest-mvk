#!/bin/sh
# Prints the total coverage of a JaCoCo CSV report in percent, rounded to two
# decimals (e.g. "77.78"). Used by the CI summary, the GitHub Pages time series
# and the coverage gate, so all three report the very same number.
#
# usage: coverage.sh [report.csv] [METRIC]
#   report.csv  default: build/reports/jacoco/test/jacocoTestReport.csv
#   METRIC      LINE (default), BRANCH, INSTRUCTION, METHOD or COMPLEXITY
set -eu

csv="${1:-build/reports/jacoco/test/jacocoTestReport.csv}"
metric="${2:-LINE}"

if [ ! -f "$csv" ]; then
	echo "coverage.sh: JaCoCo report '$csv' not found - did './gradlew test' run?" >&2
	exit 1
fi

# The CSV has one row per class; the header names the columns, e.g.
# ...,LINE_MISSED,LINE_COVERED,... - sum both columns over all classes.
awk -F, -v metric="$metric" '
	NR == 1 {
		for (i = 1; i <= NF; i++) {
			if ($i == metric "_MISSED") missedCol = i
			if ($i == metric "_COVERED") coveredCol = i
		}
		if (!missedCol || !coveredCol) {
			print "coverage.sh: unknown metric " metric > "/dev/stderr"
			failed = 1
			exit 2
		}
		next
	}
	{ missed += $missedCol; covered += $coveredCol }
	END {
		if (failed) exit 2
		total = missed + covered
		printf "%.2f\n", total ? 100 * covered / total : 0
	}
' "$csv"
