import data.UserData;
import io.qameta.allure.Description;
import io.qameta.allure.junit4.DisplayName;
import org.junit.After;
import org.junit.Test;
import user.UserModel;

import static data.UserData.PASSWORD;
import static org.hamcrest.CoreMatchers.*;
import static steps.UserSteps.*;

public class CreateUserTest extends BaseApiTest {

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
        var response = createUniqueUser(userUnique);
        //логирование (для отладки в Allure/консоли)
        response.then().log().all();
        //тут начинается детальная проверка тела ответа
        response.then()
                .statusCode(200)
                //проверка верхнего уровня
                .body("success", equalTo(true))
                //проверка вложенного объекта "user"
                .body("user.email", equalTo(userUnique.getEmail()))
                .body("user.name", equalTo(userUnique.getName()))
                //проверка токенов (структура и наличие)
                .body("accessToken", notNullValue())
                .body("accessToken", startsWith("Bearer ")) //убеждаемся, что это Bearer токен
                .body("refreshToken", notNullValue());
        //извлекаем значение токена
        String accessToken = response.jsonPath().getString("accessToken");
        UserData.currentAccessToken = accessToken;
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
                .statusCode(403)
                .body("success", equalTo(false))
                .body("message", equalTo("User already exists"));
    }

    @Test
    @DisplayName("Создание пользователя без одного из обязательных полей")
    @Description("Пользователь не будет создан, должна вернуться ошибка от сервера")
    public void testCreateUserWithoutOneField() {

        String name = faker.name().fullName();

        userUnique = new UserModel(null, PASSWORD, name);
        createUserWithoutOneField(userUnique)
                .then()
                .log().all()
                .statusCode(403)
                .body("success", equalTo(false))
                .body("message", equalTo("Email, password and name are required fields"));
    }

    @After
    public void tearDown() {
        if (UserData.currentAccessToken != null) {
            System.out.println("Удаляем пользователя с accessToken: " + UserData.currentAccessToken);
            deleteUser_Create(userUnique);
        }
    }
}