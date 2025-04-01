package ru.vscheck.monitor;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class MonitorApplicationTest {

  @Test
  void contextLoads() {
    // проверка, что контекст стартует без ошибок
  }

  @Test
  void testMainMethod_runsWithoutExceptions() {
    MonitorApplication.main(new String[] {}); // просто запускаем
  }
}
