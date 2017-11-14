package tk.wizardmerlin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportResource;
import tk.wizardmerlin.operations.IpRotator;

@SpringBootApplication
@ImportResource("classpath:app-config.xml")
public class Application implements CommandLineRunner {
    @Autowired
    private IpRotator ipRotator;

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Override
    public void run(String... args) {
        ipRotator.rotateIp();
    }


}
