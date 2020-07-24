// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps;

import java.util.Collection;

import java.util.Comparator;
import java.util.Collection;
import java.util.Collections;
import java.util.ArrayList;
import java.util.List;

public final class FindMeetingQuery {
  public Collection<TimeRange> query(Collection<Event> events,
                                     MeetingRequest request) {
    Collection<String> mandatoryAttendees = request.getAttendees();
    Collection<String> allAttendees = new ArrayList<>(mandatoryAttendees);
    allAttendees.addAll(request.getOptionalAttendees());
    long duration = request.getDuration();
    Collection<TimeRange> withOptional = findAvailability(events,
                                                          allAttendees,
                                                          duration);
    Collection<TimeRange> withoutOptional = findAvailability(events,
                                                             mandatoryAttendees,
                                                             duration);
    if (!withOptional.isEmpty()) {
        return withOptional;
    } else {
        return withoutOptional;
    }
  }
  
  private Collection<TimeRange> findAvailability(Collection<Event> events, 
                                                 Collection<String> attendees,
                                                 long duration) {
    List<TimeRange> unavailibleTimes = findUnavailability(events, attendees);
    return findAvailabilityUsingUnavailability(unavailibleTimes, duration);
  }

  private List<TimeRange> findUnavailability(Collection<Event> events, 
                                             Collection<String> attendees) {
    List<TimeRange> unavailibleTimes = new ArrayList<>();
    for (Event e : events) {
      if (!Collections.disjoint(e.getAttendees(), attendees)) {
        unavailibleTimes.add(e.getWhen());
      }
    }
    Collections.sort(unavailibleTimes, TimeRange.ORDER_BY_START_THEN_END);
    return removeOverlapping(unavailibleTimes);
  }

  private List<TimeRange> removeOverlapping(List<TimeRange> times) {
    for (int i = 0; i < times.size() - 1; i++) {
      while (i < times.size() - 1 && times.get(i).contains(times.get(i+1))) {
        times.remove(i+1);
      }
      while (i < times.size() - 1 && times.get(i).overlaps(times.get(i+1))) {
        int addDuration = Math.abs(times.get(i).end() - times.get(i+1).end());
        times.set(i, TimeRange.fromStartDuration(times.get(i).start(),
                                                 times.get(i).duration() +
                                                 addDuration));
        times.remove(i+1);
      }
    }
    return times;
  }

  private List<TimeRange> findAvailabilityUsingUnavailability(
                            List<TimeRange> unavailibleTimes,
                            long meetingDuration) {
    List<TimeRange> availibleTimes = new ArrayList<>();
    if (unavailibleTimes.size() == 0) {
      if (meetingDuration <= TimeRange.WHOLE_DAY.duration()) {
        availibleTimes.add(TimeRange.WHOLE_DAY);
      }
    } else {
      TimeRange first = TimeRange.fromStartEnd(TimeRange.START_OF_DAY,
                                               unavailibleTimes.get(0).start(),
                                               false);
      if (first.duration() >= meetingDuration) {
        availibleTimes.add(first);
      }
      for (int i = 0; i < unavailibleTimes.size() - 1; i++) {
        TimeRange ofI = TimeRange.fromStartEnd(unavailibleTimes.get(i).end(),
                                               unavailibleTimes.get(i+1).start(),
                                               false);
        if (ofI.duration() >= meetingDuration) {
          availibleTimes.add(ofI);
        }
      }
      TimeRange last = TimeRange.fromStartEnd(
                         unavailibleTimes.get(unavailibleTimes.size()-1).end(),
                         TimeRange.END_OF_DAY, true);
      if (last.duration() >= meetingDuration) {
        availibleTimes.add(last);
      }
    }
    return availibleTimes;
  }
}