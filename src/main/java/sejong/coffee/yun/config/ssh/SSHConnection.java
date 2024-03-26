package sejong.coffee.yun.config.ssh;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.util.Properties;

@Component
@Slf4j
public class SSHConnection {
    private final String host;
    private final Integer port;
    private final String sshUser;
    private final String sshPw;

    public SSHConnection(@Value("${ssh.host}") final String host,
                         @Value("${ssh.port}") final Integer port,
                         @Value("${ssh.user}") final String sshUser,
                         @Value("${ssh.password}") final String sshPw) {
        this.host = host;
        this.port = port;
        this.sshUser = sshUser;
        this.sshPw = sshPw;
    }

    private static final String LOCALHOST = "localhost";
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
            session = jsch.getSession(sshUser, host, port);
            session.setPassword(sshPw);
            session.setConfig(config);

            session.connect();

            int localPortMaster = 3306;
            int localPortSlave1 = 3307;
            int localPortSlave2 = 3308;
            int localPortPostgres = 5432;
            int localRedis = 6379;

            session.setPortForwardingL(localPortMaster, LOCALHOST, 3306);// MySQL Master
            session.setPortForwardingL(localPortSlave1, LOCALHOST, 3306);// MySQL Slave1
            session.setPortForwardingL(localPortSlave2, LOCALHOST, 3306);// MySQL Slave2
            session.setPortForwardingL(localPortPostgres, LOCALHOST, 5432);// Postgres
            session.setPortForwardingL(localRedis, LOCALHOST, 6379);// Redis

            log.info("SSH 연결 성공");

        } catch (JSchException e) {
            log.info("SSH 연결 실패: " + e.getMessage());
            e.printStackTrace();
        }
    }
}