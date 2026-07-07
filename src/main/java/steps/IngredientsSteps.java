package steps;

import io.qameta.allure.Step;
import io.restassured.response.Response;

import static data.OrderData.INGREDIENTS_PATH;
import static io.restassured.RestAssured.given;

public class IngredientsSteps {
    @Step("Получение данных об ингредиентах")
    public static Response getIngredients() {
        return given()
                .log().all()
                .header("Content-type", "application/json")
                .when()
                .get(INGREDIENTS_PATH)
                .then()
                .extract().response();
    }
}
