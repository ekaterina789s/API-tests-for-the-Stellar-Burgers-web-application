package steps;

import io.qameta.allure.Step;
import io.restassured.response.Response;
import user.UserModel;

import static data.UserData.*;
import static io.restassured.RestAssured.given;

public class UserSteps {
    @Step("Создание нового пользователя, ручка /api/auth/register")
    public static Response createUniqueUser(UserModel user){
        return given()
                .log().all()
                .header("Content-type", "application/json")
                .body(user)
                .when()
                .post(CREATE_USER_PATH)
                .then()
                .extract().response();
    }
}
