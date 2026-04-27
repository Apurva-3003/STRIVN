package com.example.strivn.data.models

import java.time.LocalDate
import java.time.temporal.IsoFields

/**
 * ISO week identity ([IsoFields.WEEK_BASED_YEAR] + [IsoFields.WEEK_OF_WEEK_BASED_YEAR]).
 */
data class YearWeek(val year: Int, val week: Int) : Comparable<YearWeek> {
    override fun compareTo(other: YearWeek): Int =
        compareValuesBy(this, other, { it.year }, { it.week })
}

fun LocalDate.toYearWeek(): YearWeek =
    YearWeek(
        year = get(IsoFields.WEEK_BASED_YEAR),
        week = get(IsoFields.WEEK_OF_WEEK_BASED_YEAR),
    )
