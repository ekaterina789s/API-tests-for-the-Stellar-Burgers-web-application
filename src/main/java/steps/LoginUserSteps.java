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

    @Step("Удаление пользователя по accessToken, который приходит при авторизации пользователя, ручка /api/auth/user")
    public static Response deleteUserAuth(LoginUserModel loginUser){
        return given()
                .when()
                .header("Authorization", "Bearer " + currentAccessToken)
                .delete(DELETE_USER_PATH);
    }
}
