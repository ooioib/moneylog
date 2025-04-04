package org.codenova.moneylog.query;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CategoryExpense {
    private int categoryId;
    private int total;
    private String categoryName;

}
