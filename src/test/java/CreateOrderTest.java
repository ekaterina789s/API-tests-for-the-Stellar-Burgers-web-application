import data.OrderData;
import io.qameta.allure.Description;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.response.Response;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import user.LoginUserModel;
import user.UserModel;
import java.util.List;
import static org.apache.http.HttpStatus.*;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.hasKey;
import static steps.CreateOrderSteps.createOrderWithAuth;
import static steps.CreateOrderSteps.createOrderWithoutAuth;
import static steps.IngredientsSteps.getIngredients;
import static steps.UserSteps.createUniqueUser;
import static steps.UserSteps.deleteUser;

public class CreateOrderTest extends BaseApiTest {

    private UserModel userUnique;
    private LoginUserModel loginUser;
    private String email;
    private String password;
    private String name;
    private String accessToken;

    @Before
    public void init(){
        email = faker.internet().emailAddress();
        password = "12345";
        name = faker.name().fullName();
        userUnique = new UserModel(email, password, name);

        // Создаём пользователя и сразу берём токен из ответа регистрации
        var responseCreateUser = createUniqueUser(userUnique);
        responseCreateUser.then()
                .statusCode(SC_OK)
                .body("success", equalTo(true))
                .body("accessToken", notNullValue());

        accessToken = responseCreateUser.jsonPath().getString("accessToken");
    }

    @Test
    @DisplayName("Создание заказа с ингредиентами и с авторизацией")
    @Description("Сервер должен успешно обработать запрос")
    public void testCreateOrderWithIngredientsAndAuthorization() {
        String[] idsArray = getTwoValidIngredientIds();
        var response = createOrderWithAuth(accessToken, idsArray);
        response.then()
                .log().all()
                .statusCode(SC_OK)
                .body("name", notNullValue())
                .body("order.number", notNullValue())
                .body("success", equalTo(true));
    }

    @Test
    @DisplayName("Создание заказа без ингредиентов и с авторизацией")
    @Description("Сервер должен вернуть ошибку")
    public void testCreateOrderWithoutIngredientsAndWithAuthorization() {
        String[] emptyIdsArray = {};
        var response = createOrderWithAuth(accessToken, emptyIdsArray);
        response.then()
                .log().all()
                .statusCode(SC_BAD_REQUEST)
                .body("success", equalTo(false))
                .body("message", equalTo("Ingredient ids must be provided"));
    }

    @Test
    @DisplayName("Создание заказа с ингредиентами и без авторизации")
    @Description("Сервер должен успешно обработать запрос")
    public void testCreateOrderWithIngredientsAndWithoutAuthorization(){
        String[] idsArray = getTwoValidIngredientIds();
        var response = createOrderWithoutAuth(idsArray);
        response.then()
                .log().all()
                .statusCode(SC_OK);
    }

    @Test
    @DisplayName("Создание заказа с неверным хэшем ингредиентов и с авторизацией")
    @Description("Сервер должен вернуть ошибку")
    public void testCreateOrderWithInvalidIngredientsAndWithAuthorization(){
        String[] invalidIdsArray = {OrderData.INVALID_HASH_FIRST_ING, OrderData.INVALID_HASH_SECOND_ING};
        var response = createOrderWithAuth(accessToken, invalidIdsArray);
        response.then()
                .log().all()
                .statusCode(SC_INTERNAL_SERVER_ERROR);
    }

    @After
    public void tearDown() {
        if (userUnique != null) {
            System.out.println("Удаляем пользователя: " + userUnique.getEmail());
            try {
                deleteUser(accessToken, userUnique);
            } catch (Exception e) {
                System.out.println("Не удалось удалить пользователя: " + e.getMessage());
            }
        }
}
   //Получение списка ингредиентов и возврат два валидных _id
    private String[] getTwoValidIngredientIds() {
        var response = getIngredients();
        response.then()
                .log().ifValidationFails()
                .statusCode(SC_OK)
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

        List<String> allIds = response.jsonPath().getList("data._id");
        if (allIds.size() < 2) {
            throw new IllegalStateException("Для теста нужно минимум 2 ингредиента, а получено: " + allIds.size());
        }
        return new String[]{allIds.get(0), allIds.get(1)};
    }
}
