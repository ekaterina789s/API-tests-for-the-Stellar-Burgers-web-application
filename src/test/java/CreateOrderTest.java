import data.UserData;
import io.qameta.allure.Description;
import io.qameta.allure.junit4.DisplayName;
import org.junit.After;
import org.junit.Test;
import user.LoginUserModel;
import user.UserModel;
import java.util.List;

import static data.OrderData.INVALID_HASH_FIRST_ING;
import static data.OrderData.INVALID_HASH_SECOND_ING;
import static data.UserData.PASSWORD;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.hasKey;
import static steps.CreateOrderSteps.createOrderWithAuth;
import static steps.CreateOrderSteps.createOrderWithoutAuth;
import static steps.IngredientsSteps.getIngredients;
import static steps.LoginUserSteps.authorizationUser;
import static steps.UserSteps.createUniqueUser;
import static steps.UserSteps.deleteUserCreate;

public class CreateOrderTest extends BaseApiTest {

    private UserModel userUnique;

    @Test
    @DisplayName("Создание заказа с ингредиентами и с авторизацией")
    @Description("Сервер должен успешно обработать запрос")
    public void testCreateOrderWithIngredientsAndAuthorization() {
        //сначала нужно создать пользователя
        String email = faker.internet().emailAddress();
        String name = faker.name().fullName();

        userUnique = new UserModel(email, PASSWORD, name);
        var responseCreateUser = createUniqueUser(userUnique);
        responseCreateUser.then()
                .statusCode(200)
                .body("success", equalTo(true));

        //авторизуем пользователя
        LoginUserModel loginUser_forCreateOrder = new LoginUserModel(userUnique.getEmail(), PASSWORD);

        var responseLoginUser = authorizationUser(loginUser_forCreateOrder);
        responseLoginUser.then()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("accessToken", startsWith("Bearer "));


        String accessToken = responseLoginUser.jsonPath().getString("accessToken"); //извлекаем значение токена
        UserData.currentAccessToken = accessToken;

        //получаем хэш ингредиентов
        var responseIngredients = getIngredients();

        responseIngredients.then()
                .log().all()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("data", everyItem(hasKey("_id")))
                .body("data", everyItem(hasKey("name")))
                .body("data", everyItem(hasKey("type")))
                .body("data", everyItem(hasKey("proteins")))
                .body("data", everyItem(hasKey("fat")))
                .body("data", everyItem(hasKey("carbohydrates")))
                .body("data", everyItem(hasKey("calories")))
                .body("data", everyItem(hasKey("price")))
                .body("data", everyItem(hasKey("image")))
                .body("data", everyItem(hasKey("image_mobile")))
                .body("data", everyItem(hasKey("image_large")))
                .body("data", everyItem(hasKey("__v")));

        //помещаем хэши в список
        List<String> allIds = responseIngredients.jsonPath().getList("data._id");

        if (allIds.size() < 2) {
            throw new IllegalStateException("Для теста нужно минимум 2 ингредиента, а получено: " + allIds.size());
        }

        String hash_firstIngredient = allIds.get(0);
        String hash_secondIngredient = allIds.get(1);

        //помещаем в массив переменные с хэшем
        String[] idsArray = {hash_firstIngredient, hash_secondIngredient};

        //создаем заказ
        var responseCreateOrder = createOrderWithAuth(idsArray);

        responseCreateOrder.then()
                .log().all()
                .statusCode(200)
                .body("name", notNullValue())
                .body("order.number", notNullValue())
                .body("success", equalTo(true));
    }

    @Test
    @DisplayName("Создание заказа без ингредиентов и с авторизацией")
    @Description("Сервер должен вернуть ошибку")
    public void testCreateOrderWithoutIngredientsAndWithAuthorization() {
        //создаем пользователя
        String email = faker.internet().emailAddress();
        String name = faker.name().fullName();

        userUnique = new UserModel(email, PASSWORD, name);
        var responseCreateUser = createUniqueUser(userUnique);
        responseCreateUser.then()
                .statusCode(200)
                .body("success", equalTo(true));

        //авторизуем пользователя
        LoginUserModel loginUser_forCreateOrder = new LoginUserModel(userUnique.getEmail(), PASSWORD);

        var responseLoginUser = authorizationUser(loginUser_forCreateOrder);
        responseLoginUser.then()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("accessToken", startsWith("Bearer "));


        String accessToken = responseLoginUser.jsonPath().getString("accessToken"); //извлекаем значение токена
        UserData.currentAccessToken = accessToken;

        //создаем пустой массив
        String[] idsArray = new String[0];
        System.out.println("Отправляем заказ с пустым списком ингредиентов (size = " + idsArray.length + ")");

        //создаем заказ
        var responseCreateOrder = createOrderWithAuth(idsArray);

        responseCreateOrder.then()
                .log().all()
                .statusCode(400)
                .body("success", equalTo(false))
                .body("message", equalTo("Ingredient ids must be provided"));
    }

    @Test
    @DisplayName("Создание заказа с ингредиентами и без авторизацией")
    @Description("Сервер должен вернуть ошибку")
    public void testCreateOrderWithIngredientsAndWithoutAuthorization(){
        //создаем пользователя
        String email = faker.internet().emailAddress();
        String password = "12345";
        String name = faker.name().fullName();

        userUnique = new UserModel(email, password, name);

        var response = createUniqueUser(userUnique);

        response.then()
                .log().all()
                .statusCode(200)
                .body("success", equalTo(true));

        //получаем хэш ингредиентов
        var responseIngredients = getIngredients();

        responseIngredients.then()
                .log().all()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("data", everyItem(hasKey("_id")));

        //помещаем хэши в список
        List<String> allIds = responseIngredients.jsonPath().getList("data._id");

        String hash_firstIngredient = allIds.get(0);
        String hash_secondIngredient = allIds.get(1);

        //помещаем в массив переменные с хэшем
        String[] idsArray = {hash_firstIngredient, hash_secondIngredient};

        createOrderWithoutAuth(idsArray)
                .then()
                .log().all()
                .statusCode(200);
    }

    @Test
    @DisplayName("Создание заказа с неверным хэшем ингредиентов и с авторизацией")
    @Description("Сервер должен вернуть ошибку")
    public void testCreateOrderWithInvalidIngredientsAndWithAuthorization(){
        //создаем пользователя
        String email = faker.internet().emailAddress();
        String name = faker.name().fullName();

        userUnique = new UserModel(email, PASSWORD, name);
        var responseCreateUser = createUniqueUser(userUnique);
        responseCreateUser.then()
                .statusCode(200)
                .body("success", equalTo(true));

        //авторизуем пользователя
        LoginUserModel loginUser_forCreateOrder = new LoginUserModel(userUnique.getEmail(), PASSWORD);

        var responseLoginUser = authorizationUser(loginUser_forCreateOrder);
        responseLoginUser.then()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("accessToken", startsWith("Bearer "));


        String accessToken = responseLoginUser.jsonPath().getString("accessToken"); //извлекаем значение токена
        UserData.currentAccessToken = accessToken;

        //помещаем в массив неверный хэш ингредиентов
        String[] idsArray = {INVALID_HASH_FIRST_ING, INVALID_HASH_SECOND_ING};

        //создаем заказ
        createOrderWithAuth(idsArray)
                .then()
                .log().all()
                .statusCode(500);
    }

    @After
    public void tearDown() {
        if (UserData.currentAccessToken != null) {
            System.out.println("Удаляем пользователя с accessToken: " + UserData.currentAccessToken);
            deleteUserCreate(userUnique);
        }
    }
}
