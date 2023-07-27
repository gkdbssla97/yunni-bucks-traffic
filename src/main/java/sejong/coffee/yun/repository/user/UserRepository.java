package sejong.coffee.yun.repository.user;

import org.springframework.stereotype.Repository;
import sejong.coffee.yun.domain.user.Member;

import java.util.List;

public interface UserRepository {

    Member save(Member member);
    Member findById(Long id);
    List<Member> findAll();
    boolean exist(Long id);
    void delete(Long id);
    void updateName(Long id, String name);
    void updatePassword(Long id, String password);
    void updateEmail(Long id, String email);
}
