import io.qameta.allure.Description;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.response.Response;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import user.LoginUserModel;
import user.UserModel;
import static org.apache.http.HttpStatus.SC_OK;
import static org.apache.http.HttpStatus.SC_UNAUTHORIZED;
import static org.hamcrest.CoreMatchers.*;
import static steps.LoginUserSteps.authorizationUser;
import static steps.UserSteps.createUniqueUser;
import static steps.UserSteps.deleteUser;

public class LoginUserTest extends BaseApiTest {

    private LoginUserModel loginUser;
    private String email;
    private String password;
    private String name;
    UserModel userToCreate;
    Response response;

    @Before
    public void init(){
        //прямо здесь создаем нового пользователя
        email = faker.internet().emailAddress();
        password = "12345";
        name = faker.name().fullName();
        userToCreate = new UserModel(email, password, name);
        response = createUniqueUser(userToCreate);
    }

    @Test
    @DisplayName("Вход под существующим пользователем")
    @Description("Нужно передать email и password, под которыми регистрировался пользователь")
    public void testAuthorizationExistingUser() {
        loginUser = new LoginUserModel(email, password);
        var authResponse = authorizationUser(loginUser);
        authResponse.then()
                .log().all()
                .statusCode(SC_OK)
                .body("success", equalTo(true))
                .body("accessToken", startsWith("Bearer "))
                .body("refreshToken", notNullValue())
                .body("user.email", equalTo(email))
                .body("user.name", equalTo(name));
    }

    @Test
    @DisplayName("Вход с неверным паролем")
    @Description("Должна быть ошибка от сервера")
    public void testAuthorizationInvalidPassword() {
        String passwordInvalid = "#######";
        loginUser = new LoginUserModel(email, passwordInvalid);
        var authResponse = authorizationUser(loginUser);
        authResponse.then()
                .log().all()
                .statusCode(SC_UNAUTHORIZED)
                .body("success", equalTo(false))
                .body("message", equalTo("email or password are incorrect"));
    }

    @Test
    @DisplayName("Вход с неверным email")
    @Description("Должна быть ошибка от сервера")
    public void testAuthorizationInvalidEmail() {
        String emailInvalid = "########";
        loginUser = new LoginUserModel(emailInvalid, password);
        var authResponse = authorizationUser(loginUser);
        authResponse.then()
                .log().all()
                .statusCode(SC_UNAUTHORIZED)
                .body("success", equalTo(false))
                .body("message", equalTo("email or password are incorrect"));
    }

    @After
    public void tearDown() {
        if (response != null) {
            String accessToken = response.jsonPath().getString("accessToken");
            System.out.println("Удаляем пользователя с accessToken: " + accessToken);
            deleteUser(accessToken, userToCreate);
        }
    }
}
