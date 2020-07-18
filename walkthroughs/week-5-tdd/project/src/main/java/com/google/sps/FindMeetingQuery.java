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

import java.util.Comparator;
import java.util.Collection;
import java.util.Collections;
import java.util.ArrayList;
import java.util.List;

public final class FindMeetingQuery {
  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
    Collection<String> attendees = request.getAttendees();
    List<TimeRange> unavailibleTimes = new ArrayList<>();
    for (Event e : events) {
        if (!Collections.disjoint(e.getAttendees(), attendees)) {
            unavailibleTimes.add(e.getWhen());
        }
    }
    Collections.sort(unavailibleTimes, TimeRange.ORDER_BY_START_THEN_END);
    for (int i = 0; i < unavailibleTimes.size() - 1; i++) {
        while (i < unavailibleTimes.size() - 1 && unavailibleTimes.get(i).contains(unavailibleTimes.get(i+1))) {
            unavailibleTimes.remove(i+1);
        }
        while (i < unavailibleTimes.size() - 1 && unavailibleTimes.get(i).overlaps(unavailibleTimes.get(i+1))) {
            int addDuration = Math.abs(unavailibleTimes.get(i).end() - unavailibleTimes.get(i+1).end());
            unavailibleTimes.set(i, TimeRange.fromStartDuration(unavailibleTimes.get(i).start(), unavailibleTimes.get(i).duration() + addDuration));
            unavailibleTimes.remove(i+1);
        }
    }
    List<TimeRange> availibleTimes = new ArrayList<>();
    long meetingDuration = request.getDuration();
    if (unavailibleTimes.size() == 0) {
        if (meetingDuration <= TimeRange.WHOLE_DAY.duration()) {
            availibleTimes.add(TimeRange.WHOLE_DAY);
        }
    } else {
        TimeRange first = TimeRange.fromStartEnd(TimeRange.START_OF_DAY, unavailibleTimes.get(0).start(), false);
        if (first.duration() >= meetingDuration) {
            availibleTimes.add(first);
        }
        for (int i = 0; i < unavailibleTimes.size() - 1; i++) {
            TimeRange ofI = TimeRange.fromStartEnd(unavailibleTimes.get(i).end(), unavailibleTimes.get(i+1).start(), false);
            if (ofI.duration() >= meetingDuration) {
                availibleTimes.add(ofI);
            }
        }
        TimeRange last = TimeRange.fromStartEnd(unavailibleTimes.get(unavailibleTimes.size()-1).end(), TimeRange.END_OF_DAY, true);
        if (last.duration() >= meetingDuration) {
            availibleTimes.add(last);
        }
    }
    return availibleTimes;
    // throw new UnsupportedOperationException("TODO: Implement this method.");
  }
}
