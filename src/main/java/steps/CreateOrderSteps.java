package steps;

import io.qameta.allure.Step;
import io.restassured.response.Response;
import order.OrderRequest;
import static data.OrderData.CREATE_ORDER_PATH;
import static io.restassured.RestAssured.given;

public class CreateOrderSteps {
    @Step("Создание заказа с авторизацией")
    public static Response createOrderWithAuth(String token, String[] idsArray) {

        OrderRequest orderRequest = new OrderRequest(idsArray);

        return given()
                .log().all()
                .header("Content-type", "application/json")
                .header("Authorization", token)
                .body(orderRequest)
                .when()
                .post(CREATE_ORDER_PATH)
                .then()
                .extract().response();
    }

    @Step("Создание заказа без авторизации")
    public static Response createOrderWithoutAuth(String[] idsArray) {

        OrderRequest orderRequest = new OrderRequest(idsArray);

        return given()
                .log().all()
                .header("Content-type", "application/json")
                .body(orderRequest)
                .when()
                .post(CREATE_ORDER_PATH)
                .then()
                .extract().response();
    }
}
