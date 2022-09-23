package kr.ac.cnu.computer.foodpedia_app;

public class FoodItem {

    String foodName;
    String foodEngName;

    public FoodItem(String foodName, String foodEngName) {
        this.foodName = foodName;
        this.foodEngName = foodEngName;
    }

    public String getFoodName() {
        return foodName;
    }

    public String getFoodEngName() {
        return foodEngName;
    }

    public void setFoodName(String foodName) {
        this.foodName = foodName;
    }
    public void setFoodEngName(String foodEngName) { this.foodEngName=foodEngName;}

}
