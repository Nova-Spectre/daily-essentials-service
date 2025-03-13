package com.groceryapp.constant.common;

import java.util.Set;

public class Constants {
    public static final Set<String> VALID_SORT_FIELDS = Set.of("price", "quantity", "itemqty");
    public static final Set<String> VALID_SORT_DIRECTIONS = Set.of("asc", "desc");
    public static final int MIN_PAGE = 0;
    public static final int MIN_SIZE = 1;
    public static final int MAX_SIZE = 100;
    public static final double MIN_ALLOWED_PRICE = 0.0;
    public static final int MAX_LIST_SIZE = 20;
    public static final String DEFAULT_SORT_FIELD = "price";
    public static final String DEFAULT_SORT_DIRECTION = "asc";
    public static final String QUANTITY="quantity";
}