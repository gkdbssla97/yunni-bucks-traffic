package sejong.coffee.yun.config.ssh;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.util.Properties;

@Component
@Slf4j
public class SSHConnection {
    private final static String HOST = "101.101.160.207";
    private final static Integer PORT = 1024;
    private final static String SSH_USER = "root";
    private final static String SSH_PW = "A4b@3T!78mr";

    private Session session;

    @PreDestroy
    public void closeSSH() {
        if (session != null) {
            session.disconnect();
        }
    }

    public void buildSshConnection() {
        try {
            Properties config = new Properties();
            config.put("StrictHostKeyChecking", "no");
            JSch jsch = new JSch();
            session = jsch.getSession(SSH_USER, HOST, PORT);
            session.setPassword(SSH_PW);
            session.setConfig(config);

            session.connect();

            int localPortMaster = 3306;
            int localPortSlave1 = 3307;
            int localPortSlave2 = 3308;
            int localPortPostgres = 5432;
            int localRedis = 6379;

            session.setPortForwardingL(localPortMaster, "localhost", 3306);// MySQL Master
            session.setPortForwardingL(localPortSlave1, "localhost", 3306);// MySQL Slave1
            session.setPortForwardingL(localPortSlave2, "localhost", 3306);// MySQL Slave2
            session.setPortForwardingL(localPortPostgres, "localhost", 5432);// PostgreSQL
            session.setPortForwardingL(localRedis, "localhost", 6379);// Redis

            log.info("SSH 연결 성공");

        } catch (JSchException e) {
            log.info("SSH 연결 실패: " + e.getMessage());
            e.printStackTrace();
        }
    }
}