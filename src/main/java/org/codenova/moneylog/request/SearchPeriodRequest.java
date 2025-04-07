package org.codenova.moneylog.request;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class SearchPeriodRequest {
    private LocalDate startDate;
    private LocalDate endDate;

}
