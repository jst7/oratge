package com.oratge.oratgeback;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
        "oratge.api-key.valor=clave-de-test",
        "aemet.api.api-key=clave-de-test"
})
class OratgeBackApplicationTests {

	@Test
	void contextLoads() {
	}

}
