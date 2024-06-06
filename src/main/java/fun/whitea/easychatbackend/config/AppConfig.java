package fun.whitea.easychatbackend.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Getter
public class AppConfig {

    @Value("${ws.port}")
    private Integer wsPort;

    @Value("${project.folder}")
    private String projectFolder;

    @Value("${admin.emails}")
    private String adminEmails;

}
