import data.UserData;
import io.qameta.allure.Description;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.response.Response;
import org.junit.After;
import org.junit.Test;
import user.UserModel;

import static data.UserData.PASSWORD;
import static org.apache.http.HttpStatus.SC_FORBIDDEN;
import static org.apache.http.HttpStatus.SC_OK;
import static org.hamcrest.CoreMatchers.*;
import static steps.UserSteps.*;

public class CreateUserTest extends BaseApiTest {
    private Response response;

    private UserModel userUnique;

    @Test
    @DisplayName("Успешное создание уникального пользователя") //имя теста
    @Description("Пользователь будет создан, если передать 3 обязательных поля: эмейл, пароль, имя") //описание теста
    public void testCreateUniqueUser() {
        //генерируем значения ключей
        String email = faker.internet().emailAddress();
        String name = faker.name().fullName();

        //создаем объект курьера и передаем в аргументах 3 обязательных поля
        userUnique = new UserModel(email, PASSWORD, name);
        //в переменную кладем шаг теста
        response = createUniqueUser(userUnique);
        //логирование (для отладки в Allure/консоли)
        response.then().log().all();
        //тут начинается детальная проверка тела ответа
        response.then()
                .statusCode(SC_OK)
                //проверка верхнего уровня
                .body("success", equalTo(true))
                //проверка вложенного объекта "user"
                .body("user.email", equalTo(userUnique.getEmail()))
                .body("user.name", equalTo(userUnique.getName()))
                //проверка токенов (структура и наличие)
                .body("accessToken", notNullValue())
                .body("accessToken", startsWith("Bearer ")) //убеждаемся, что это Bearer токен
                .body("refreshToken", notNullValue());
    }

    @Test
    @DisplayName("Создание уже зарегистрированного пользователя")
    @Description("Пользователь не будет создан, должна вернуться ошибка от сервера")
    public void testCreateRegisteredUser() {

        String email = faker.internet().emailAddress();
        String name = faker.name().fullName();

        userUnique = new UserModel(email, PASSWORD, name);

        //шаг и проверка ответа от сервера
        createRegisteredUser(userUnique)
                .then()
                .log().all()
                .statusCode(SC_FORBIDDEN)
                .body("success", equalTo(false))
                .body("message", equalTo("User already exists"));
    }

    @Test
    @DisplayName("Создание пользователя без передачи поля email")
    @Description("Пользователь не будет создан, должна вернуться ошибка от сервера")
    public void testCreateUserWithoutEmail() {

        String name = faker.name().fullName();

        userUnique = new UserModel(null, PASSWORD, name);
        createUserWithoutOneField(userUnique)
                .then()
                .log().all()
                .statusCode(SC_FORBIDDEN)
                .body("success", equalTo(false))
                .body("message", equalTo("Email, password and name are required fields"));
    }

    @Test
    @DisplayName("Создание пользователя без передачи поля password")
    @Description("Пользователь не будет создан, должна вернуться ошибка от сервера")
    public void testCreateUserWithoutPassword() {
        String email = faker.internet().emailAddress();
        String password = null;
        String name = faker.name().fullName();

        userUnique = new UserModel(email, password, name);
        createUserWithoutOneField(userUnique)
                .then()
                .log().all()
                .statusCode(SC_FORBIDDEN)
                .body("success", equalTo(false))
                .body("message", equalTo("Email, password and name are required fields"));
    }

    @Test
    @DisplayName("Создание пользователя без передачи поля name")
    @Description("Пользователь не будет создан, должна вернуться ошибка от сервера")
    public void testCreateUserWithoutName() {
        String email = faker.internet().emailAddress();
        String name = null;

        userUnique = new UserModel(email, PASSWORD, name);
        createUserWithoutOneField(userUnique)
                .then()
                .log().all()
                .statusCode(SC_FORBIDDEN)
                .body("success", equalTo(false))
                .body("message", equalTo("Email, password and name are required fields"));
    }

    @After
    public void tearDown() {
        if (response != null) {
            String accessToken = response.jsonPath().getString("accessToken");
            UserData.currentAccessToken = accessToken;
            System.out.println("Удаляем пользователя с accessToken: " + UserData.currentAccessToken);
            deleteUser(userUnique);
        }
    }
}