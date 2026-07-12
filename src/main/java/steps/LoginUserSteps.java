package steps;

import io.qameta.allure.Step;
import io.restassured.response.Response;
import user.LoginUserModel;

import static data.UserData.*;
import static io.restassured.RestAssured.given;

public class LoginUserSteps {
    @Step("Авторизация пользователя")
    public static Response authorizationUser(LoginUserModel loginUser) {
        return given()
                .log().all()
                .header("Content-type", "application/json")
                .body(loginUser)
                .when()
                .post(AUTHORIZATION_PATH)
                .then()
                .extract().response();
    }
}
