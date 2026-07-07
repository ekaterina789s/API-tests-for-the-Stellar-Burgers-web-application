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

    @Step("Создание уже зарегистрированного пользователя, ручка /api/auth/register")
    public static Response createRegisteredUser(UserModel user){
        return given()
                .log().all()
                .header("Content-type", "application/json")
                .header("Authorization", "Bearer " + currentAccessToken)
                .body(user)
                .when()
                .post(CREATE_USER_PATH)
                .then()
                .extract().response();

    }

    @Step("Создание пользователя без одного из обязательных полей, ручка /api/auth/register")
    public static Response createUserWithoutOneField(UserModel user){
        return given()
                .log().all()
                .header("Content-type", "application/json")
                .body(user)
                .when()
                .post(CREATE_USER_PATH)
                .then()
                .extract().response();
    }

    @Step("Удаление пользователя по accessToken, который приходит при создании пользователя, ручка /api/auth/user")
    public static Response deleteUser_Create(UserModel user){
        return given()
                .when()
                .header("Authorization", "Bearer " + currentAccessToken)
                .delete(DELETE_USER_PATH);
    }
}
