package com.ji.shoppingreminder.database;

import java.util.ArrayList;
import java.util.List;

public class CategoryLists {

    public List<String> foodStore = new ArrayList<String>(){
        {
            add("SUPERMARKET");
            add("RESTAURANT");
        }
    };

    public List<String> groceryStore = new ArrayList<String>(){
        {
            add("GROCERY_OR_SUPERMARKET ");
            add("SUPREMARKET");
            add("PHARMACY");
            add("UNIVERSITY");
        }
    };

    public List<String> clothingStore = new ArrayList<String>(){
        {
            add("CLOTHING_STORE");
            add("SHOPPING_MALL");
            add("SHOE_STORE");
        }
    };
}
