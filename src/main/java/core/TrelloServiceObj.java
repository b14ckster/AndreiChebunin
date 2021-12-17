package core;

import static constants.BoardParameters.NAME;
import static io.restassured.http.ContentType.TEXT;
import static io.restassured.http.ContentType.URLENC;
import static org.hamcrest.Matchers.lessThan;

import beans.TrelloBoard;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import constants.ResponseStatus;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.http.Method;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.apache.http.HttpStatus;

public class TrelloServiceObj {
    public static final URI TRELLO_URL = URI.create("https://api.trello.com");
    public static final URI BOARD_URI = URI.create("/1/boards/");
    private static final String PATH_TO_PROPERTIES = "src/test/resources/keyAndToken.properties";
    private static String URL;

    private static Properties properties;
    private Map<String, String> parameters;
    private Method requestMethod;

    private TrelloServiceObj(Map<String, String> parameters, Method method) {
        this.parameters = parameters;
        this.requestMethod = method;

        properties = new Properties();
        try {
            FileInputStream file = new FileInputStream(PATH_TO_PROPERTIES);
            properties.load(file);
            file.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static ApiRequestBuilder requestBuilder() {
        return new ApiRequestBuilder();
    }

    public static class ApiRequestBuilder {
        private Map<String, String> parameters = new HashMap<>();
        private Method requestMethod = Method.GET;

        public ApiRequestBuilder setMethod(Method method) {
            this.requestMethod = method;
            return this;
        }

        public ApiRequestBuilder setName(String name) {
            parameters.put(NAME.getParameterName(), name);
            return this;
        }

        public ApiRequestBuilder setParameter(String parameterName, String parameter) {
            parameters.put(parameterName, parameter);
            return this;
        }

        public TrelloServiceObj buildRequest() {
            return new TrelloServiceObj(parameters, requestMethod);
        }
    }

    private static RequestSpecification baseRequestSpecification() {
        return new RequestSpecBuilder()
            .setAccept(ContentType.JSON)
            .setBaseUri(TRELLO_URL)
            .addParam("key", properties.getProperty("key"))
            .addParam("token", properties.getProperty("token"))
            .build();
    }

    public TrelloBoard boardRequest(int status) {
        Response response = RestAssured
            .with()
            .spec(baseRequestSpecification())
            .contentType(URLENC.withCharset(StandardCharsets.UTF_8))
            .log().all()
            .queryParams(parameters)
            .request(requestMethod, URL)
            .prettyPeek();

        switch (status) {
            case (ResponseStatus.GOOD_RESPONSE):
                response.then().assertThat().spec(TrelloServiceObj.goodResponseSpecification());
                break;
            case (ResponseStatus.BAD_RESPONSE):
                response.then().assertThat().spec(TrelloServiceObj.badResponseSpecification());
                break;
            case (ResponseStatus.NOT_FOUND_RESPONSE):
                response.then().assertThat().spec(TrelloServiceObj.notFoundResponse());
                break;
        }

        return formBoardFromResponse(response);
    }

    public static TrelloBoard formBoardFromResponse(Response response) {
        if (HttpStatus.SC_OK == response.then().extract().statusCode()) {
            return new Gson().
                fromJson(response.asString().trim(),
                    new TypeToken<TrelloBoard>() {
                    }.getType());
        } else {
            return null;
        }
    }

    public static TrelloBoard createBoard(String name, int status) {
        URL = TRELLO_URL.toString() + BOARD_URI.toString();
        return requestBuilder()
            .setMethod(Method.POST)
            .setName(name)
            .buildRequest()
            .boardRequest(status);
    }

    public static void deleteBoard(String id, int status) {
        URL = TRELLO_URL.toString() + BOARD_URI.toString() + id;
        requestBuilder()
            .setMethod(Method.DELETE)
            .buildRequest()
            .boardRequest(status);
    }

    public static TrelloBoard getBoard(String id, int status) {
        URL = TRELLO_URL.toString() + BOARD_URI.toString() + id;
        return requestBuilder()
            .setMethod(Method.GET)
            .buildRequest()
            .boardRequest(status);
    }

    public static TrelloBoard updateBoard(String id, String paramName, String param, int status) {
        URL = TRELLO_URL.toString() + BOARD_URI.toString() + id;
        return requestBuilder()
            .setMethod(Method.PUT)
            .setParameter(paramName, param)
            .buildRequest()
            .boardRequest(status);
    }

    private static ResponseSpecification notFoundResponse() {
        return new ResponseSpecBuilder()
            .expectContentType(TEXT)
            .expectResponseTime(lessThan(10000L))
            .expectStatusCode(HttpStatus.SC_NOT_FOUND)
            .build();
    }

    public static ResponseSpecification goodResponseSpecification() {
        return new ResponseSpecBuilder()
            .expectContentType(ContentType.JSON)
            .expectResponseTime(lessThan(10000L))
            .expectStatusCode(HttpStatus.SC_OK)
            .build();
    }

    public static ResponseSpecification badResponseSpecification() {
        return new ResponseSpecBuilder()
            .expectContentType(ContentType.TEXT)
            .expectResponseTime(lessThan(10000L))
            .expectStatusCode(HttpStatus.SC_BAD_REQUEST)
            .build();
    }
}
