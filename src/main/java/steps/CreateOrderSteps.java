package steps;

import io.qameta.allure.Step;
import io.restassured.response.Response;
import java.util.HashMap;
import java.util.Map;

import static data.OrderData.CREATE_ORDER_PATH;
import static data.UserData.currentAccessToken;
import static io.restassured.RestAssured.given;

public class CreateOrderSteps {
    @Step("Создание заказа с авторизацией")
    public static Response createOrderWithAuth(String[] idsArray) {

        //Создаём JSON-объект: {"ingredients": [...]}
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("ingredients", idsArray);

        return given()
                .log().all()
                .header("Content-type", "application/json")
                .header("Authorization", currentAccessToken)
                .body(requestBody) // Передаём Map — Rest Assured сделает из неё правильный JSON
                .when()
                .post(CREATE_ORDER_PATH)
                .then()
                .extract().response();
    }

    @Step("Создание заказа без авторизации")
    public static Response createOrderWithoutAuth(String[] idsArray) {

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("ingredients", idsArray);

        return given()
                .log().all()
                .header("Content-type", "application/json")
                .body(requestBody)
                .when()
                .post(CREATE_ORDER_PATH)
                .then()
                .extract().response();
    }
}
