import data.UserData;
import io.qameta.allure.Description;
import io.qameta.allure.junit4.DisplayName;
import org.junit.After;
import org.junit.Test;
import steps.LoginUserSteps;
import user.LoginUserModel;
import user.UserModel;

import static org.hamcrest.CoreMatchers.*;
import static steps.LoginUserSteps.authorizationUser;
import static steps.UserSteps.createUniqueUser;

public class LoginUserTest extends BaseApiTest {

    private LoginUserModel loginUser;

    @Test
    @DisplayName("Вход под существующим пользователем")
    @Description("Нужно передать email и password, под которыми регистрировался пользователь")
    public void testAuthorizationExistingUser() {
        //прямо здесь создаем нового пользователя
        String email = faker.internet().emailAddress();
        String password = "12345";
        String name = faker.name().fullName();

        UserModel userToCreate = new UserModel(email, password, name);
        //шаг для создания пользователя
        var response = createUniqueUser(userToCreate);

        //проверка ответа от сервера на создание нового пользователя
        response.then()
                .log().all()
                .statusCode(200)
                .body("success", equalTo(true));

        //делаем объект пользователя для авторизации
        loginUser = new LoginUserModel(email, password);

        //шаг запроса на авторизацию
        var authResponse = authorizationUser(loginUser);

        //извлекаем значение токена
        String accessToken = authResponse.jsonPath().getString("accessToken");
        UserData.currentAccessToken = accessToken;

        authResponse.then()
                .log().all()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("accessToken", startsWith("Bearer "))
                .body("refreshToken", notNullValue())
                .body("user.email", equalTo(email))
                .body("user.name", equalTo(name));

    }
}
