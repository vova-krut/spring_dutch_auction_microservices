package eu.attempto.dutch_auction_microservices.dashboard_service.range;

import java.time.DayOfWeek;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public enum TimeRange {
  DAY,
  WEEK,
  MONTH,
  YEAR;

  public String getTimeRangeStr() {
    var now = ZonedDateTime.now();
    var beginningOfTimeRange =
        switch (this) {
          case DAY -> now.toLocalDate().atStartOfDay(now.getOffset());
          case WEEK -> now.toLocalDate().with(DayOfWeek.MONDAY).atStartOfDay(now.getOffset());
          case MONTH -> now.toLocalDate().withDayOfMonth(1).atStartOfDay(now.getOffset());
          case YEAR -> now.toLocalDate().withDayOfYear(1).atStartOfDay(now.getOffset());
        };

    return beginningOfTimeRange.format(DateTimeFormatter.ISO_INSTANT);
  }
}
