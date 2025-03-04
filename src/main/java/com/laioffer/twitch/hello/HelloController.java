package com.laioffer.twitch.hello;


import com.github.javafaker.Faker;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


import java.util.Locale;


@RestController
public class HelloController {


    @GetMapping("/hello")
    public com.laioffer.twitch.hello.Person sayHello(@RequestParam(required = false) String locale) {
        if (locale == null) {
            locale = "en_US";
        }
        Faker faker = new Faker(new Locale(locale));
        String name = faker.name().fullName();
        String company = faker.company().name();
        String street = faker.address().streetAddress();
        String city = faker.address().city();
        String state = faker.address().state();
        String bookTitle = faker.book().title();
        String bookAuthor = faker.book().author();
        String country = faker.address().country();
        return new com.laioffer.twitch.hello.Person(
                name,
                company,
                new Address(street, city, state, country),
                new Book(bookTitle, bookAuthor)
        );
    }


}
